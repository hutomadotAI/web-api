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
    state_data = await loop.run_in_executor(None, functools.partial(load_sync, state_file))
    return state_data


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
            self.logger.warning("Creating save directory {}".format(save_directory))
            save_directory.mkdir(parents=True)

        with state_file.open("wb") as file_handle:
            dill.dump(self, file_handle)

    async def save(self, state_file: Path):
        loop = asyncio.get_running_loop()
        await loop.run_in_executor(None, functools.partial(self.save_sync, state_file))

    def report(self):
        if self.bots is None:
            bot_list = []
        else:
            bot_list = [{
                "ai_id": bot.ai_id,
                "status": bot.result.name
            } for bot in self.bots]
        report_content = {
            "status": self.status.name,
            "last_updated": str(self.last_updated),
            "bots": bot_list
        }
        return report_content
