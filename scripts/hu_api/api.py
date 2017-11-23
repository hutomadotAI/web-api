import hashlib
import json
import urllib.parse
import uuid
from json import JSONDecodeError

import requests


# Response wrapper
class ApiResponse:
    def __init__(self, callResult):
        self.success = False
        self.status_code = callResult.status_code
        try:
            # decode from json if possible
            result = callResult.json()
            # if there is a status then the response came from the API server
            if result['status']:
                # so store a copy of the json
                self.response = result
                # copy the json result code
                code = result['status']['code']
                # assemble a description of the result
                self.text = str(code) + ': ' + result['status']['info']
                # if we got a 2XX response then the the json object has the answer data
                if code // 100 == 2:
                    self.success = True
            else:
                # otherwise, assemble a description from the HTTP results
                self.text = 'Error ' + str(callResult.status_code) + ': ' + callResult.reason
        except JSONDecodeError:
            self.text = 'Error ' + str(callResult.status_code) + ': ' + callResult.reason


class ApiRequester:
    def __init__(self, url_root, auth, proxies=None):
        self.url_root = url_root
        self.auth_headers = {'Authorization': "Bearer " + auth}
        if proxies is None:
            proxies = {}
        self.proxies = proxies

    def get(self, path, query=None, headers=None):
        if headers is None:
            headers = {}
        headers.update(self.auth_headers)
        response = ApiResponse(
            requests.get(self.__get_url(path), params=query, headers=headers, proxies=self.proxies))
        return response

    def put(self, path, body=None, query=None, body_is_json=True, headers=None):
        headers = self.__prepare_headers(headers, body, body_is_json)
        response = ApiResponse(
            requests.put(self.__get_url(path), params=query, headers=headers, data=body, proxies=self.proxies))
        return response

    def post(self, path, body=None, query=None, body_is_json=True, headers=None, files=None):
        headers = self.__prepare_headers(headers, body, body_is_json)
        response = ApiResponse(
            requests.post(self.__get_url(path), params=query, headers=headers, data=body, proxies=self.proxies,
                          files=files))
        return response

    def delete(self, path, headers=None):
        if headers is None:
            headers = {}
        headers.update(self.auth_headers)
        response = ApiResponse(
            requests.delete(self.__get_url(path), headers=headers, proxies=self.proxies))
        return response

    def __prepare_headers(self, headers, body, body_is_json):
        if headers is None:
            headers = {}
        headers.update(self.auth_headers)
        if body is not None and body_is_json:
            headers.update({'Content-Type': 'application/json'})
        return headers

    def __get_url(self, path):
        url = self.url_root + "/" + path
        return url


def get_auth_code(url_root, proxies={}):
    user = str(uuid.uuid4())
    email = "abc@def.com"
    # This is probably not correct, but PHP has fixed random string!
    password = str(uuid.uuid4())
    salt = str(uuid.uuid4())
    to_hash = password.encode() + salt.encode()
    password_salted = hashlib.sha256(to_hash).hexdigest()
    query = {'role': 'ROLE_FREE',
             'username': user,
             'email': email,
             'password': password_salted,
             'first_name': 'api_script'
             }
    headers = {
        'Authorization': "Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8Y4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c"}
    url = url_root + "/admin"
    response = requests.post(url, params=query, headers=headers, proxies=proxies)
    answer = ApiResponse(response)
    return answer


def create_ai(api: ApiRequester, name, description, is_private=0, confidence=0, voice=0, locale="en-US",
              timezone="Europe/London"):
    body = {'name': name, 'description': description, 'is_private': is_private, 'confidence': confidence,
            'voice': voice,
            'locale': locale, 'timezone': timezone}
    return api.post("ai", body, body_is_json=False)


def delete_ai(api: ApiRequester, aiid):
    return api.delete("ai/" + aiid)


def get_ai(api: ApiRequester, aiid=''):
    return api.get("ai/" + aiid)


def find_ais(api: ApiRequester, name_prefix):
    find = get_ai(api)
    if find.success:
        if "ai_list" in find.response:
            return [ai["aiid"] for ai in find.response["ai_list"] if ai["name"].startswith(name_prefix)]
    return []


def find_ai(api: ApiRequester, name):
    find = get_ai(api)
    if find.success:
        if "ai_list" in find.response:
            for ai in find.response["ai_list"]:
                if name == ai["name"]:
                    return (ai["aiid"], ai)
    return ("", find.text)


def delete_all_ais(api: ApiRequester):
    listAll = get_ai(api)
    if listAll.success:
        if "ai_list" in listAll.response:
            for ai in listAll.response["ai_list"]:
                print(ai["name"], ai["aiid"])
                delete_ai(api, aiid=ai["aiid"])


def edit_entity(api: ApiRequester, entity):
    query = {"entity_name": entity["entity_name"]}

    return api.post("entity", query=query, body=json.dumps(entity))


def create_entity(name, values):
    return {"entity_name": name, "entity_values": values}


def edit_intent(api: ApiRequester, aiid, intent):
    query = {"intent_name": intent["intent_name"]}
    return api.post("intent/" + aiid, query=query, body=json.dumps(intent))


def create_intent(name, variables, user_says, responses, topic_in="", topic_out=""):
    return {"intent_name": name, "topic_in": topic_in, "topic_out": topic_out, "variables": variables,
            "user_says": user_says, "responses": responses}


def create_intent_variable(entityName, required, n_prompts, value, prompts):
    return {"entity_name": entityName, "required": required, "n_prompts": n_prompts,
            "value": value, "prompts": prompts}


def upload_training(api: ApiRequester, aiid, file):
    files = {'file': open(file, 'rb')}
    query = {"source_type": 0}
    return api.post("ai/" + aiid + "/training", query=query, files=files)


def start_training(api: ApiRequester, aiid):
    return api.put("ai/" + aiid + "/training/start")


def stop_training(api: ApiRequester, aiid):
    return api.put("ai/" + aiid + "/training/stop")


def chat(api: ApiRequester, aiid, say_what, chat_id=""):
    query = {'q': say_what}
    if chat_id != "":
        query['chatId'] = chat_id
    return api.get("ai/" + aiid + "/chat", query=query)


def load_test_chat(api: ApiRequester, aiid, say_what, chat_id=""):
    query = {'q': say_what}
    if chat_id != "":
        query['chatId'] = chat_id
    return api.get("ai/load/" + aiid + "/chat", query=query)
