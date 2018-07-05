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
    if (url_parsed.scheme != 'http' and url_parsed.scheme != 'https'):
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

        if command == "create-ai":
            name = args.name
            description = args.description
            print("Create AI name:'{}', desc:'{}'".format(name, description))
            answer = hu_api.api.create_ai(requester, name, description)
            print(answer.text)
            print(answer.response)
        elif command == "get-ai":
            ai_id = args.ai_id
            print("Get AI for ID {}".format(ai_id))
            answer = hu_api.api.get_ai(requester, ai_id)
            print(answer.text)
            print(answer.response)
        elif command == "find-ais":
            ai_search = args.expression
            print("Find AIs '{}'".format(ai_search))
            matches = hu_api.api.find_ais(requester, ai_search)
            for match in matches:
                print(match)
        elif command == "train-upload":
            ai_id = args.ai_id
            training_file = args.training_file
            print("Uploading training to '{}'".format(ai_id))
            answer = hu_api.api.upload_training(requester, ai_id, training_file)
            print(answer.text)
            print(answer.response)
        elif command == "train-start":
            ai_id = args.ai_id
            print("Start training for '{}'".format(ai_id))
            answer = hu_api.api.start_training(requester, ai_id)
            print(answer.text)
            print(answer.response)
        elif command == "train-stop":
            ai_id = args.ai_id
            print("Stop training for '{}'".format(ai_id))
            answer = hu_api.api.stop_training(requester, ai_id)
            print(answer.text)
            print(answer.response)
        elif command == "chat":
            ai_id = args.ai_id
            chat_in = args.chat_input
            print("Chat with AI {} with input '{}'".format(ai_id, chat_in))
            answer = hu_api.api.chat(requester, ai_id, chat_in, args.id)
            print(answer.text)
            print(answer.response)
        elif command == "delete-ai":
            ai_id = args.ai_id
            print("Delete AI '{}'".format(ai_id))
            answer = hu_api.api.delete_ai(requester, ai_id)
            print(answer.text)
            print(answer.response)
        elif command == "delete-all-ais":
            print("Delete all AIs")
            hu_api.api.delete_all_ais(requester)
        elif command == "update-training":
            ai_id = args.ai_id
            print("Update training")
            hu_api.api.update_training(requester, ai_id)
        else:
            arg_error(parser, "command '{}' is not recognized".format(command))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Hutoma API test command-line')
    parser.add_argument('--url', help='URL to API. Can also be set using the HUTOMA_API_CLI_URL environment variable.')
    parser.add_argument(
        '--token', help='Token to use for auth. Can also be set using the HUTOMA_API_CLI_TOKEN environment variable.')
    parser.add_argument('--proxies', help='proxies')
    subparsers = parser.add_subparsers(help='available commands', dest='command')

    # get token
    parser_get_token = subparsers.add_parser('get-token', help='Creates user and displays token for it')
    # find AIs
    parser_find_ais = subparsers.add_parser('find-ais', help='List AIs for the user, can be used to search')
    parser_find_ais.add_argument('--expression', help='Search expression', default='')
    # get AI
    parser_get_ai = subparsers.add_parser('get-ai', help='Gets status of an AI')
    parser_get_ai.add_argument('ai_id', help='AI ID')

    # create AI
    parser_create_ai = subparsers.add_parser('create-ai', help='Create an AI')
    parser_create_ai.add_argument('name', help='AI name')
    parser_create_ai.add_argument('description', help='AI name')
    # train upload
    parser_train_upload = subparsers.add_parser('train-upload', help='Upload training file to an AI')
    parser_train_upload.add_argument('ai_id', help='AI ID')
    parser_train_upload.add_argument('training_file', help='Training file to upload')
    # train start
    parser_train_start = subparsers.add_parser('train-start', help='Start AI training')
    parser_train_start.add_argument('ai_id', help='AI ID')
    # train stop
    parser_train_stop = subparsers.add_parser('train-stop', help='Stop AI training')
    parser_train_stop.add_argument('ai_id', help='AI ID')
    # chat
    parser_chat = subparsers.add_parser('chat', help='Chat with an AI')
    parser_chat.add_argument('ai_id', help='AI ID')
    parser_chat.add_argument('chat_input', help='Chat input')
    parser_chat.add_argument('--id', help='Chat ID')
    
    # update training
    parser_update_training = subparsers.add_parser('update-training', help='Update training - causes chat-cores to be retrained')
    parser_update_training.add_argument('ai_id', help='AI ID')
    # delete AI
    parser_delete = subparsers.add_parser('delete-ai', help='Delete an AI')
    parser_delete.add_argument('ai_id', help='AI ID')
    # delete all
    parser_delete_all = subparsers.add_parser('delete-all-ais', help='Delete all AIs')

    args = parser.parse_args()
    main(args, parser)
