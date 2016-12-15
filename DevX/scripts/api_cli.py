import argparse
import urllib.parse
import hu_api.api


def main(args):
    url_raw = args.url
    url_parsed = urllib.parse.urlparse(url_raw)
    if url_parsed.scheme != 'http':
        url_parsed = urllib.parse.urlparse('http://' + url_raw)
    url = url_parsed.geturl()
    command = args.command.lower()
    if command == "get-token":
        print("Getting auth token for a new user with random credentials")
        answer = hu_api.api.get_auth_code(url)
        print(answer.text)
        print(answer.response)
    else:
        requester = hu_api.api.ApiRequester(url, args.token, args.proxies)
        other_args = args.command_args

        if command == "create-ai":
            name = other_args[0]
            description = other_args[1]
            print("Create AI name:'{}', desc:'{}'".format(name, description))
            answer = hu_api.api.create_ai(requester, name, description)
            print(answer.text)
            print(answer.response)
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


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Hutoma API test command-line')
    parser.add_argument('command',
                        help="Command to run on API. Valid values are: get-token, create-ai, train-upload, " +
                             "train-start, train-stop")
    parser.add_argument('command_args', nargs="*",
                        help="""Other command args
""")
    parser.add_argument('--url', help='URL to API', default="localhost:11080/v1")
    parser.add_argument(
        '--token', help='Token to use for auth',
        default='eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkZQUS1ODlDQDXTPDtCRdExNDM91Ec8tEXeMkA2MDiyRDS8PEVKVaAAAAAP__.GzTtyLSrgy-muIoucnY2OLtM9kz0Q2I1gNFHOsA4SGA')
    parser.add_argument('--proxies', help='proxies')

    args = parser.parse_args()
    main(args)
