import asyncio
import datetime
import logging

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
        if (self.migration_state.status == state.Status.RUNNING or
                self.migration_state.status == state.Status.INITIALIZING):
            raise LogicError("Can't retrain in state {}".format(
                self.migration_state.status.name))

        self.migration_state.reset()
        self.migration_state.status = state.Status.INITIALIZING
        try:
            results = self._call_stored_procedure(self, "getAisForRetraining")

            # TODO: make this settable
            priority_dev_ids = ["ca11ab1e-1111-1111-1111-111111111111"]

            bots = []
            for iid, ai_id, dev_id, publishing_state in results:
                is_published = publishing_state == 2
                retrain_entry = state.RetrainBotEntry(iid, ai_id, dev_id,
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
            self.migration_task = None
            raise

    async def retrain_cancel(self):
        if self.migration_state.status == state.Status.INACTIVE:
            self.logger.info(
                "Cancel received in INACTIVE state, doing nothing")
            return

        if self.migration_task is not None:
            self.migration_task.cancel()

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
                await self._api_wait_for_training(session, bots_in_training, 0)

                self.migration_state.status = state.Status.INACTIVE
                await self.migration_state.save(self.state_file)
                migration_time = datetime.datetime.utcnow(
                ) - self.migration_state.start_time
                report = self.migration_state.report()
                completed_bots = report["summary"]["completed"]
                errored_bots = report["summary"]["errored"]
                self.logger.info(
                    "[METRIC][MIGRATION.RETRAIN_ALL.COMPLETED] Migration complete: took %s, "
                    + "completed %d, errored %d",
                    migration_time,
                    completed_bots,
                    errored_bots,
                    extra={
                        "migration_time": migration_time,
                        "completed": completed_bots,
                        "errored": errored_bots
                    })
        except asyncio.CancelledError:
            self.logger.warning(
                "[METRIC][MIGRATION.RETRAIN_ALL.CANCELLED] Retraining cancelled"
            )
            self.migration_state.status = state.Status.CANCELLED
            await self.migration_state.save(self.state_file)
        except Exception:
            self.logger.exception(
                "[METRIC][MIGRATION.RETRAIN_ALL.ERROR] Error in _retrain_worker"
            )
            self.migration_state.status = state.Status.ERRORED
            await self.migration_state.save(self.state_file)
            raise
        finally:
            self.migration_task = None

    async def _process_bot(self, session, bots_in_training, bot):
        self.logger.info("Processing {}".format(bot))
        if bot.result is state.RetrainResult.COMPLETE:
            self.logger.info("Bot %s already trained", bot.ai_id)
        elif bot.result is state.RetrainResult.ERROR:
            self.logger.info("Bot %s already errored", bot.ai_id)
        elif bot.result is state.RetrainResult.TRAINING:
            await self._api_status(session, bot)
            # recheck status as it might have changed
            if bot.result is state.RetrainResult.TRAINING:
                bots_in_training.append((bot, None))
                self.logger.info("Bot %s is training", bot.ai_id)
            elif bot.result is state.RetrainResult.COMPLETE:
                self.logger.info("Bot %s was training, but since completed",
                                 bot.ai_id)
            elif bot.result is state.RetrainResult.ERROR:
                self.logger.info("Bot %s was training, but since errored",
                                 bot.ai_id)

        elif bot.result is state.RetrainResult.PENDING:
            bot.result = state.RetrainResult.TRAINING
            await self._api_retrain(session, bot)
            if bot.result is state.RetrainResult.TRAINING:
                bots_in_training.append((bot, datetime.datetime.utcnow()))

        await self.migration_state.save(self.state_file)
        await self._api_wait_for_training(session, bots_in_training,
                                          MAXIMUM_BOTS_IN_TRAINING - 1)

    async def _api_wait_for_training(self, session, bots_in_training,
                                     max_count):
        last_log = datetime.datetime.utcnow()
        while len(bots_in_training) > max_count:
            if ((datetime.datetime.utcnow() - last_log).total_seconds() > 20.0):
                bot_ids = [bot[0].ai_id for bot in bots_in_training]
                self.logger.info("Waiting for bots %s to complete", bot_ids)
                last_log = datetime.datetime.utcnow()
            await asyncio.sleep(1)
            bots_in_training_copy = bots_in_training.copy()
            for entry in bots_in_training_copy:
                bot = entry[0]
                start_time = entry[1]
                await self._api_status(session, bot)
                if (bot.result is state.RetrainResult.COMPLETE or
                        bot.result is state.RetrainResult.ERROR):
                    bots_in_training.remove(entry)
                    if start_time:
                        training_duration = datetime.datetime.utcnow(
                        ) - start_time
                    else:
                        training_duration = None
                    bot.training_duration = training_duration
                    self.logger.info(
                        "[METRIC][MIGRATION.BOT.%s] Training complete for %s, took %s",
                        bot.result.name, bot.ai_id, training_duration)

    async def _api_retrain(self, session, bot):
        url = "{}/admin/migration/{}/{}".format(self.api_url, bot.dev_id,
                                                bot.ai_id)
        self.logger.info("Sending retrain command for %s", bot.ai_id)
        headers = {'Authorization': "Bearer {}".format(self.api_key)}
        async with session.post(url, headers=headers) as resp:
            if resp.status == 200:
                self.logger.debug("POST to %s successful", url)
            else:
                bot.error_message = "Retrained returned {}".format(resp.status)
                bot.error_detail = await resp.text()
                bot.result = state.RetrainResult.ERROR
                self.logger.warning(
                    "Retraining command to %s failed with code %d", url,
                    resp.status)

    async def _api_status(self, session, bot):
        url = "{}/admin/migration/{}/{}".format(self.api_url, bot.dev_id,
                                                bot.ai_id)
        headers = {'Authorization': "Bearer {}".format(self.api_key)}
        async with session.get(url, headers=headers) as resp:
            if resp.status == 200:
                resp_json = await resp.json()
                status = resp_json["status"]["info"]
                self.logger.debug("GET to %s successful", url)
                if status == "ai_training_complete":
                    bot.result = state.RetrainResult.COMPLETE
                elif status == "ai_error":
                    bot.result = state.RetrainResult.ERROR
                    bot.error_message = "Bot training error status"
            else:
                self.logger.warning(
                    "API status command to %s failed with code %d", url,
                    resp.status)
