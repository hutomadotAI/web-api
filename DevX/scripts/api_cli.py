import argparse
import os
import urllib.parse
import hu_api.api


def arg_error(parser, error_text):
    print("\n**** ERROR: {}! ****\n".format(error_text))
    parser.print_help()
    exit(-1)


def main(args, parser):
    url_raw = args.url
    if url_raw is None or len(url_raw) == 0:
        url_raw = os.environ.get('HUTOMA_API_CLI_URL')

    if url_raw is None or len(url_raw) == 0:
        arg_error(parser, "URL is not set")

    url_parsed = urllib.parse.urlparse(url_raw)
    if url_parsed.scheme != 'http':
        url_parsed = urllib.parse.urlparse('http://' + url_raw)
    url = url_parsed.geturl()
    print("API URL is", url)
    command = args.command.lower()
    if command == "get-token":
        print("Getting auth token for a new user with random credentials")
        answer = hu_api.api.get_auth_code(url)
        print(answer.text)
        print(answer.response)
    else:
        token = args.token
        if token is None or len(token) == 0:
            token = os.environ.get('HUTOMA_API_CLI_TOKEN')

        if token is None or len(token) == 0:
            arg_error(parser, "TOKEN is not set")

        requester = hu_api.api.ApiRequester(url, token, args.proxies)
        other_args = args.command_args

        if command == "create-ai":
            name = other_args[0]
            description = other_args[1]
            print("Create AI name:'{}', desc:'{}'".format(name, description))
            answer = hu_api.api.create_ai(requester, name, description)
            print(answer.text)
            print(answer.response)
        elif command == "get-ai":
            ai_id = ''
            if len(other_args) > 0:
                ai_id = other_args[0]
            print("Get AI for ID {}".format(ai_id))
            answer = hu_api.api.get_ai(requester, ai_id)
            print(answer.text)
            print(answer.response)
        elif command == "find-ais":
            ai_search = ''
            if len(other_args) > 0:
                ai_search = other_args[0]
            print("Find AIs '{}'".format(ai_search))
            matches = hu_api.api.find_ais(requester, ai_search)
            for match in matches:
                print(match)
        elif command == "train-upload":
            ai_id = other_args[0]
            training_file = other_args[1]
            answer = hu_api.api.upload_training(requester, ai_id, training_file)
            print(answer.text)
            print(answer.response)
        elif command == "train-start":
            ai_id = other_args[0]
            answer = hu_api.api.start_training(requester, ai_id)
            print(answer.text)
            print(answer.response)
        elif command == "train-stop":
            ai_id = other_args[0]
            answer = hu_api.api.stop_training(requester, ai_id)
            print(answer.text)
            print(answer.response)
        else:
            arg_error(parser, "command '{}' is not recognized".format(command))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Hutoma API test command-line')
    parser.add_argument('command',
                        help="Command to run on API. Valid values are: get-token, find-ais, get-ai, create-ai, " +
                             "train-upload, train-start, train-stop")
    parser.add_argument('command_args', nargs="*",
                        help="""Other command args
""")
    parser.add_argument('--url', help='URL to API. Can also be set using the HUTOMA_API_CLI_URL environment variable.')
    parser.add_argument(
        '--token', help='Token to use for auth. Can also be set using the HUTOMA_API_CLI_TOKEN environment variable.')
    parser.add_argument('--proxies', help='proxies')

    args = parser.parse_args()
    main(args, parser)
