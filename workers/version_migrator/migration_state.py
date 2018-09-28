import asyncio
import logging
import functools
from pathlib import Path
import datetime
import dill
import enum

FILE_VERSION = 1


def _get_logger():
    logger = logging.getLogger('migration_state')
    return logger


class Status(enum.Enum):
    INACTIVE = enum.auto()
    INITIALIZING = enum.auto()
    RUNNING = enum.auto()
    CANCELLED = enum.auto()
    ERRORED = enum.auto()


class MigrationStateException(Exception):
    pass


def load_sync(state_file: Path):
    if not state_file.exists():
        raise MigrationStateException(
            "File {} doesn't exist, cannot load".format(state_file))

    with state_file.open("rb") as file_handle:
        state_data = dill.load(file_handle)
    return state_data


async def load(state_file: Path):
    loop = asyncio.get_running_loop()
    state_data = await loop.run_in_executor(
        None, functools.partial(load_sync, state_file))
    return state_data


class RetrainResult(enum.Enum):
    PENDING = enum.auto()
    TRAINING = enum.auto()
    COMPLETE = enum.auto()
    ERROR = enum.auto()


class RetrainBotEntry:
    def __init__(self, iid, ai_id, dev_id, is_published, is_priority=False):
        self.iid = iid
        self.ai_id = ai_id
        self.dev_id = dev_id
        self.is_published = is_published
        self.is_priority = is_priority
        self.result = RetrainResult.PENDING
        self.error_message = None
        self.error_detail = None
        self.training_duration = None

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


class MigrationState:
    def __init__(self):
        self.reset()
        self.logger = _get_logger()

    def reset(self):
        self.status = Status.INACTIVE
        self.file_version = FILE_VERSION
        self.start_time = datetime.datetime.utcnow()
        self.last_updated = datetime.datetime.utcnow()
        self.bots = None

    def save_sync(self, state_file: Path):
        self.last_updated = datetime.datetime.utcnow()
        save_directory = state_file.parent
        if not save_directory.exists():
            self.logger.warning(
                "Creating save directory {}".format(save_directory))
            save_directory.mkdir(parents=True)

        with state_file.open("wb") as file_handle:
            dill.dump(self, file_handle)

    async def save(self, state_file: Path):
        loop = asyncio.get_running_loop()
        await loop.run_in_executor(
            None, functools.partial(self.save_sync, state_file))

    def report(self):
        bot_list = []
        completed_count = 0
        error_count = 0
        training_count = 0
        total_count = 0
        pending_count = 0

        duration = None
        if self.status is Status.RUNNING and self.start_time is not None:
            duration = datetime.datetime.utcnow() - self.start_time

        if self.bots is not None:
            for bot in self.bots:
                total_count += 1
                bot_status = {
                    "ai_id": bot.ai_id,
                    "status": bot.result.name
                }
                if bot.result is RetrainResult.COMPLETE:
                    completed_count += 1
                    bot_status["training_duration"] = str(bot.training_duration)
                elif bot.result is RetrainResult.ERROR:
                    error_count += 1
                    bot_status["error_message"] = bot.error_message
                    bot_status["error_detail"] = bot.error_detail
                elif bot.result is RetrainResult.TRAINING:
                    training_count += 1
                elif bot.result is RetrainResult.PENDING:
                    pending_count += 1
                bot_list.append(bot_status)

        summary = {
                "total": total_count,
                "completed": completed_count,
                "training": training_count,
                "pending": pending_count,
                "errored": error_count,
                "last_updated": str(self.last_updated)
            }
        if duration is not None:
            summary["duration"] = str(duration)

        report_content = {
            "status": self.status.name,
            "summary": summary,
            "bots": bot_list
        }
        return report_content
