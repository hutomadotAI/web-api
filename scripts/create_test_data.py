import random
from pathlib import Path

import time

import hu_api.api
from performance.common import read_lines, write_file_lines, de_rate_limit, check_api_available
from performance.performance_config import Config, make_config

def main():

    def delete_test_ais(ai_defs):
        for aiid in hu_api.api.find_ais(requester, ai_defs.ai_prefix):
            print('Deleting AI {0}'.format(aiid))
            response = de_rate_limit(hu_api.api.delete_ai, requester, aiid)
            if not response.success:
                print('Failed to delete: {0}'.format(response.text))

    def create_upload_ai(size, tag, filename, ai_prefix):
        name = "{0}-{1}".format(ai_prefix, str(tag))

        print('Creating {0} AI '.format(name))
        creation = de_rate_limit(hu_api.api.create_ai, requester, name, "Testing with {0} lines of training data".format(size))
        if not creation.success:
            print("Error creating AI: {0}".format(creation.text))
        aiid = creation.response["aiid"]

        print('Uploading new data file')
        upload = de_rate_limit(hu_api.api.upload_training, requester, aiid, filename)
        if not upload.success:
            print("Error uploading training data: {0}".format(upload.text))

        return aiid

    def create_aiml_ai(tag, ai_prefix, bot_id):
        name = "{0}-AIML-{1}".format(ai_prefix, str(tag))

        print('Creating {0} AI '.format(name))
        creation = de_rate_limit(hu_api.api.create_ai, requester, name, "Test AIML bot {}".format(tag))
        if not creation.success:
            print("Error creating AI: {0}".format(creation.text))
            exit(0)
        aiid = creation.response["aiid"]

        print('Linking skill to aiml bot')
        link_up = de_rate_limit(hu_api.api.link_bot_to_ai, requester, aiid, bot_id)
        if not link_up:
            print("Error linking new bot to skill")
            exit(0)
        else:
            if link_up.status_code and link_up.status_code == 200:
                pass
            else:
                print("Error linking new bot {}".format(link_up.text if link_up.text else link_up))
                exit(0)

        return aiid

    def create_intent_only_ai(tag, ai_prefix):
        name = "{0}-Intent-{1}".format(ai_prefix, str(tag))

        print('Creating {0} AI '.format(name))
        creation = de_rate_limit(hu_api.api.create_ai, requester, name, "Test Intent bot {}".format(tag))
        if not creation.success:
            print("Error creating AI: {0}".format(creation.text))
            exit(0)
        aiid = creation.response["aiid"]

        print('Creating intent')

        de_rate_limit(hu_api.api.edit_entity, requester,
                      hu_api.api.create_entity("Colour", ["red", "green", "blue"]))

        intent_variable = hu_api.api.create_intent_variable("Colour", True, 1, "intent", ["What colour?"])
        for index in range(10):
            intent_name = "intent_{}".format(str(index))
            intent = hu_api.api.create_intent(intent_name,
                                              [intent_variable],
                                              ["intent_trigger_{}".format(str(index))],
                                              ["triggered {}".format(intent_name)])
            intent_create = de_rate_limit(hu_api.api.edit_intent, requester, aiid, intent)
            if not intent_create:
                print("Error creating intent")
                exit(0)
            else:
                if intent_create.status_code and (intent_create.status_code // 100) == 2:
                    pass
                else:
                    print("Error creating intent {}".format(intent_create.text if intent_create.text else intent_create))
                    exit(0)

        return aiid

    def train_start_ai(aiid, wait_for_ready=True):

        print('Starting training')

        while wait_for_ready:
            status_request = de_rate_limit(hu_api.api.get_ai, requester, aiid)
            if status_request.response["ai_status"] == "ai_ready_to_train":
                break
            else:
                print("Waiting for status to flip ...")
                time.sleep(1)

        train = de_rate_limit(hu_api.api.start_training, requester, aiid)
        if not train.success:
            print("Error starting training: {0}".format(train.text))

    def train_update_ai(aiid):

        print('Updating training')

        train = de_rate_limit(hu_api.api.update_training, requester, aiid)
        if not train.success:
            print("Error updating training: {0}".format(train.text))

    def create_sentence(source_word_list, common_words):
        big_word_count = random.randint(1, 3)
        small_word_count = 2 * big_word_count + random.randint(-1, 3)
        using_big_words = random.sample(source_word_list, big_word_count)
        for word in using_big_words:
            source_word_list.remove(word)
        question = using_big_words + random.sample(common_words, small_word_count)
        random.shuffle(question)
        return ' '.join(question)

    def load_word_list():
        # load the full word list
        filtered = set([line.strip() for line in read_lines("wordlist.txt")
                        if "'" not in line and line == line.lower()])

        # remove common words
        for word in config.common_words:
            filtered.discard(word)

        print('Unique words ', len(filtered))
        as_list = sorted(list(filtered))
        return as_list

    def create_test_bot_lines(word_list, line_count):
        training_data = []
        for a in range(line_count // 2):
            question = create_sentence(word_list, config.common_words)
            answer = create_sentence(word_list, config.common_words)
            training_data.append((question, answer))
        return training_data

    def create_new_ai_files(ai_defs):
        filtered = load_word_list()
        for size, tag, filename in ai_defs.defs:
            create_new_ai_pairs(filtered, size, filename, ai_defs.response_prefix + str(tag))

    def create_new_ai_pairs(word_list, line_count, filename, response_prefix):
        training_data = create_test_bot_lines(word_list, line_count);
        print("Generated {0} pairs, {1} words left over: into {2}"
              .format(len(training_data), len(word_list), filename))

        text = []
        for q, a in training_data:
            text.append(q)
            text.append("{0}: {1}".format(response_prefix, a))
            text.append('')

        write_file_lines(filename, text)

    def create_and_train_new_ais(ai_defs):
        aiids = []
        for size, tag, filename in ai_defs.defs:
            aiids.append(create_upload_ai(size, tag, filename, ai_defs.ai_prefix))
        for aiid in aiids:
            train_start_ai(aiid)

    def have_we_got_the_files(ai_defs):
        missing_files = False
        for size, tag, filename in ai_defs.defs:
            if not Path(filename).is_file():
                missing_files = True
                print("Missing {0}; recreating all data files".format(filename))
        return not missing_files

    # make this process predictable
    random.seed(31415926)

    requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])

    check_api_available(requester)

    load_test_defs = config.load_test_ais
    chat_test_defs = config.chat_test_ais

    if not have_we_got_the_files(chat_test_defs):
        create_new_ai_files(chat_test_defs)

    # delete all the old load test AIs
    delete_test_ais(load_test_defs)
    delete_test_ais(chat_test_defs)

    create_aiml_ai("chitchat", chat_test_defs.ai_prefix, config.aiml["chitchat"])
    intent_only_aiid = create_intent_only_ai("intent", chat_test_defs.ai_prefix)
    create_and_train_new_ais(chat_test_defs)

    train_update_ai(intent_only_aiid)
    train_start_ai(intent_only_aiid)


if __name__ == "__main__":

    config = make_config()
    main()
