import argparse

import logging
import logging.config
import os
import tempfile

from aiohttp import web

from asyncio_utils.aiohttp_wrapped_caller import ExceptionWrappedCaller

import migration_logic as logic
import yaml


def _get_logger():
    logger = logging.getLogger('migration_server')
    return logger


LOGGING_CONFIG_TEXT = """
version: 1
root:
  level: DEBUG
  handlers: ['console' ,'elastic']
formatters:
  default:
    format: "%(asctime)s.%(msecs)03d|%(levelname)s|%(name)s|%(message)s"
    datefmt: "%Y%m%d_%H%M%S"
handlers:
  console:
    class: logging.StreamHandler
    level: INFO
    stream: ext://sys.stdout
    formatter: default
  elastic:
    class: hu_logging.HuLogHandler
    level: INFO
    log_path: /tmp/hu_log
    log_tag: VERSION_MIGRATOR
    es_log_index: version_migrator_v1
    multi_process: False
"""


class VersionMigratorHttp:
    def __init__(self, version_migrator_logic):
        self.version_migrator_logic = version_migrator_logic
        self.logger = _get_logger()

    async def on_startup(self, app):
        self.logger.info("Running on_startup")
        await self.version_migrator_logic.on_startup()

    async def http_health(self, req: web.Request):
        resp = web.Response()
        return resp

    async def http_retrain_all(self, req: web.Request):
        try:
            await self.version_migrator_logic.start_retrain_all()
        except logic.LogicError as e:
            raise web.HTTPBadRequest(body=str(e))
        resp = web.Response()
        return resp

    async def http_status(self, req: web.Request):
        status = await self.version_migrator_logic.get_status()
        resp = web.web_response.json_response(status)
        return resp


def get_from_args_or_env(value_specified, env_name, default):
    if value_specified is not None:
        value = value_specified
    else:
        value = os.getenv(env_name, default)
    return value


def startup(parser, args):
    logging_config = yaml.load(LOGGING_CONFIG_TEXT)
    logging_config['handlers']['elastic']['elastic_search_url'] = \
        os.getenv('LOGGING_ES_URL', None)
    logging.config.dictConfig(logging_config)

    server_port = get_from_args_or_env(args.port, "MIGRATOR_PORT", 9090)
    db_host = get_from_args_or_env(args.db_host, "MIGRATOR_DB_HOST",
                                   "localhost")
    db_port = get_from_args_or_env(args.db_port, "MIGRATOR_DB_PORT", 3306)
    db_user = get_from_args_or_env(args.db_user, "MIGRATOR_DB_USER", "root")
    db_password = get_from_args_or_env(args.db_password,
                                       "MIGRATOR_DB_PASSWORD", "password")
    api_url = get_from_args_or_env(args.api_url, "MIGRATOR_API_URL", None)
    api_key = get_from_args_or_env(args.api_key, "MIGRATOR_API_KEY", None)
    default_storage = tempfile.gettempdir() + "/hu_migration"
    storage_directory = get_from_args_or_env(
        args.storage_directory, "MIGRATOR_STORAGE_DIRECTORY", default_storage)

    if not api_url:
        print("API URL not set")
        parser.print_help()
        return

    if not api_key:
        print("API key not set")
        parser.print_help()
        return

    print("*** Version migrator")
    print("DB: {}:{}, connecting as {}".format(db_host, db_port, db_user))
    print("API: {}".format(api_url))
    print("Storage directory: {}".format(storage_directory))

    version_migrator_logic = logic.VersionMigratorLogic(
        db_host, db_port, db_user, db_password, api_url, api_key, storage_directory)
    app = web.Application()
    version_migrator_http = VersionMigratorHttp(version_migrator_logic)
    app.on_startup.append(version_migrator_http.on_startup)
    app.router.add_get(
        "/health", ExceptionWrappedCaller(version_migrator_http.http_health))
    app.router.add_post(
        "/retrain_all",
        ExceptionWrappedCaller(version_migrator_http.http_retrain_all))
    app.router.add_get(
        "/status", ExceptionWrappedCaller(version_migrator_http.http_status))
    web.run_app(app, port=server_port)


def main():
    """Main function"""
    parser = argparse.ArgumentParser(description="Version migrator")
    parser.add_argument(
        '--port',
        type=int,
        help="Server port, if missing read from MIGRATOR_PORT variable")
    parser.add_argument(
        '--db-host',
        help=
        'MySql host machine, if missing read from MIGRATOR_DB_HOST variable')
    parser.add_argument(
        '--db-port',
        help='MySql port, if missing read from MIGRATOR_DB_HOST variable',
        type=int)
    parser.add_argument(
        '--db-user',
        help='MySql user, if missing read from MIGRATOR_DB_USER variable')
    parser.add_argument(
        '--db-password',
        help=
        'MySql password, if missing read from MIGRATOR_DB_PASSWORD variable')
    parser.add_argument(
        '--api-url',
        help=
        'Internal API endpoint, e.g. https://api.hutoma.ai/v1, if missing read from MIGRATOR_API_URL variable'
    )
    parser.add_argument(
        '--api-key',
        help=
        'API key for authentication against API, if missing read from MIGRATOR_API_KEY variable'
    )
    parser.add_argument(
        '--storage-directory',
        help='Location to store data for ongoing operations')
    args = parser.parse_args()
    startup(parser, args)


if __name__ == '__main__':
    main()
