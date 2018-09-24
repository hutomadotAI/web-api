import asyncio
import datetime
import logging
import enum
from pathlib import Path

import aiohttp
import mysql.connector

import migration_state as state

MIGRATION_STATE_FILE = "migration_state.pkl"
MAXIMUM_BOTS_IN_TRAINING = 2


def _get_logger():
    logger = logging.getLogger('migration_logic')
    return logger


class LogicError(Exception):
    pass


class ApiError(Exception):
    pass


class RetrainResult(enum.Enum):
    PENDING = enum.auto()
    TRAINING = enum.auto()
    COMPLETE = enum.auto()


class RetrainBotEntry:
    def __init__(self, iid, ai_id, dev_id, is_published, is_priority=False):
        self.iid = iid
        self.ai_id = ai_id
        self.dev_id = dev_id
        self.is_published = is_published
        self.is_priority = is_priority
        self.result = RetrainResult.PENDING

    def __eq__(self, other):
        # ID is enough to ensure we are referring to same object
        return self.iid == other.iid

    def __lt__(self, other):
        # To sort into priority, deal with publishing first
        if self.is_published != other.is_published:
            return self.is_published > other.is_published
        # then Priority
        if self.is_priority != other.is_priority:
            return self.is_priority > other.is_priority
        # then reverse iid
        return self.iid > other.iid

    def __repr__(self):
        s = "RetrainBotEntry(iid={}, ai_id='{}', dev_id='{}', is_published={}, is_priority={})"\
            .format(self.iid, self.ai_id, self.dev_id, self.is_published, self.is_priority)
        return s


class VersionMigratorLogic:
    def __init__(self, db_host, db_port, db_user, db_password, api_url,
                 api_key, storage_directory):
        self.api_url = api_url
        self.api_key = api_key
        self.db_config = {
            'user': db_user,
            'password': db_password,
            'host': db_host,
            'port': db_port,
            'database': 'hutoma',
            'raise_on_warnings': False,
            'buffered': True
        }
        self.storage_directory = Path(storage_directory)

        self.state_file = self.storage_directory / MIGRATION_STATE_FILE
        self.logger = _get_logger()
        self.migration_state = state.MigrationState()
        self.migration_task = None

    async def on_startup(self):
        self.logger.info("Running on_startup")
        try:
            self.migration_state = await state.load(self.state_file)
            if self.migration_state.status == state.Status.INITIALIZING:
                self.logger.warning("Resetting INITIALIZING state to INACTIVE")
                self.migration_state.status = state.Status.INACTIVE
            elif self.migration_state.status == state.Status.RUNNING:
                self.logger.warning(
                    "Restarting existing migration after restart")
                self.migration_task = asyncio.create_task(
                    self._retrain_worker())

        except state.MigrationStateException:
            self.logger.warning("No valid existing migration state")

    async def get_status(self):
        if self.migration_state is None:
            return {"status": state.Status.INACTIVE.value}
        else:
            return self.migration_state.report()

    async def start_retrain_all(self):
        if (self.migration_state.status != state.Status.INACTIVE):
            raise LogicError("Can't retrain in state {}".format(
                self.migration_state.status.value))

        self.migration_state.reset()
        self.migration_state.status = state.Status.INITIALIZING
        try:
            results = self._call_stored_procedure(self, "getAisForRetraining")

            # TODO: make this settable
            priority_dev_ids = ["ca11ab1e-1111-1111-1111-111111111111"]

            bots = []
            for iid, ai_id, dev_id, publishing_state in results:
                is_published = publishing_state == 2
                retrain_entry = RetrainBotEntry(iid, ai_id, dev_id,
                                                is_published)
                if retrain_entry.dev_id in priority_dev_ids:
                    retrain_entry.is_priority = True
                bots.append(retrain_entry)

            number_bots = len(bots)
            if number_bots == 0:
                raise LogicError("No bots to retrain")
            self.logger.info(
                "Starting migration of {} bots".format(number_bots),
                extra={"number_bots": number_bots})

            # sort the bots, using in-built comparator logic
            bots_sorted = sorted(bots)
            self.migration_state.bots = bots_sorted

            # schedule the retraining in asyncio
            # will share same loop, so should avoid CPU bound work
            # or long non-asyncio IO
            self.migration_task = asyncio.create_task(self._retrain_worker())

        except Exception:
            self.migration_state.reset()
            raise

    def _call_stored_procedure(self, cursor, sp_name, *args):
        cnx = mysql.connector.connect(**self.db_config)
        cursor = cnx.cursor()
        self.logger.info("Connected to DB, calling {}".format(sp_name))
        try:
            cursor.callproc(sp_name, args)
            for result in cursor.stored_results():
                results = result.fetchall()
            return results
        finally:
            cursor.close()
            cnx.close()
            self.logger.info("DB connection closed")

    async def _retrain_worker(self):
        try:
            bots_in_training = []
            async with aiohttp.ClientSession() as session:
                self.logger.info("In retrain_worker async")
                self.migration_state.status = state.Status.RUNNING
                await self.migration_state.save(self.state_file)

                for bot in self.migration_state.bots:
                    await self._process_bot(session, bots_in_training, bot)
                self.migration_state.status = state.Status.INACTIVE
                await self.migration_state.save(self.state_file)
                migration_time = datetime.datetime.utcnow(
                ) - self.migration_state.start_time
                await self._api_wait_for_training(session, bots_in_training, 0)
                self.logger.info(
                    "[METRIC][MIGRATION.RETRAIN_ALL.COMPLETED] Migration complete: took %s",
                    migration_time,
                    extra={"migration_time": migration_time})
        except Exception:
            self.logger.exception(
                "[METRIC][MIGRATION.RETRAIN_ALL.ERROR] Error in _retrain_worker")
            self.migration_state.reset()
            await self.migration_state.save(self.state_file)
            raise

    async def _process_bot(self, session, bots_in_training, bot):
        self.logger.info("Processing {}".format(bot))
        if bot.result is RetrainResult.COMPLETE:
            self.logger.info("Bot %s already trained", bot.ai_id)
        elif bot.result is RetrainResult.TRAINING:
            await self._api_status(session, bot)
            # recheck status as it might have changed
            if bot.result is RetrainResult.TRAINING:
                bots_in_training.append((bot, None))
                self.logger.info("Bot %s is training", bot.ai_id)
            elif bot.result is RetrainResult.COMPLETE:
                self.logger.info("Bot %s was training, but since completed",
                                 bot.ai_id)

        elif bot.result is RetrainResult.PENDING:
            await self._api_retrain(session, bot)
            bots_in_training.append((bot, datetime.datetime.utcnow()))
            bot.result = RetrainResult.TRAINING

        await self.migration_state.save(self.state_file)
        await self._api_wait_for_training(session, bots_in_training,
                                          MAXIMUM_BOTS_IN_TRAINING - 1)

    async def _api_wait_for_training(self, session, bots_in_training,
                                     max_count):
        while len(bots_in_training) > max_count:
            await asyncio.sleep(1)
            bots_in_training_copy = bots_in_training.copy()
            for entry in bots_in_training_copy:
                bot = entry[0]
                start_time = entry[1]
                await self._api_status(session, bot)
                if bot.result is RetrainResult.COMPLETE:
                    bots_in_training.remove(entry)
                    if start_time:
                        training_duration = datetime.datetime.utcnow() - start_time
                    else:
                        training_duration = None
                    self.logger.info(
                        "[METRIC][MIGRATION.BOT.COMPLETED] Training complete for %s, took %s",
                        bot.ai_id, training_duration)

    async def _api_retrain(self, session, bot):
        url = "{}/admin/migration/{}/{}".format(self.api_url, bot.dev_id,
                                                bot.ai_id)
        self.logger.info("Sending retrain command for %s", bot.ai_id)
        headers = {'Authorization': "Bearer {}".format(self.api_key)}
        try:
            async with session.post(url, headers=headers) as resp:
                if resp.status != 200:
                    raise ApiError("POST to {} failed with code {}".format(
                        url, resp.status))
                self.logger.debug("POST to %s successful", url)
        except ApiError:
            raise

    async def _api_status(self, session, bot):
        url = "{}/admin/migration/{}/{}".format(self.api_url, bot.dev_id,
                                                bot.ai_id)
        headers = {'Authorization': "Bearer {}".format(self.api_key)}
        try:
            async with session.get(url, headers=headers) as resp:
                if resp.status != 200:
                    raise ApiError("GET to {} failed with code {}".format(
                        url, resp.status))
                resp_json = await resp.json()
                status = resp_json["status"]["info"]
                self.logger.debug("GET to %s successful, status is %d", url,
                                  status)
                if status == "ai_training_complete":
                    bot.result = RetrainResult.COMPLETE
        except ApiError:
            raise
