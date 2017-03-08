import random
from pathlib import Path

import hu_api.api
from performance.common import get_ai_name, read_lines, write_file_lines, de_rate_limit, check_api_available
from performance.performance_config import Config, make_config

def main():
    def create_sentence(source_word_set, common_words, used_words):
        big_word_count = random.randint(1, 3)
        small_word_count = 2 * big_word_count + random.randint(-1, 3)
        using_big_words = random.sample(source_word_set, big_word_count)
        used_words += set(using_big_words)
        source_word_set -= set(using_big_words)
        question = using_big_words + random.sample(common_words, small_word_count)
        random.shuffle(question)
        return ' '.join(question)


    def delete_load_test_ais():
        for aiid in hu_api.api.find_ais(requester, "Load-Test"):
            print('Deleting AI {0}'.format(aiid))
            response = de_rate_limit(hu_api.api.delete_ai, requester, aiid)
            if not response.success:
                print('Failed to delete: {0}'.format(response.text))


    def create_upload_ai(size, filename):
        name = get_ai_name(size)

        print('Creating {0} AI '.format(name))
        creation = de_rate_limit(hu_api.api.create_ai, requester, name, "Perf testing with {0} lines of training data".format(size))
        if not creation.success:
            print("Error creating AI: {0}".format(creation.text))
        aiid = creation.response["aiid"]

        print('Uploading new data file')
        upload = de_rate_limit(hu_api.api.upload_training, requester, aiid, filename)
        if not upload.success:
            print("Error uploading training data: {0}".format(upload.text))

        return aiid


    def train_start_ai(aiid):

        print('Starting training')

        while True:
            status_request = de_rate_limit(hu_api.api.get_ai, requester, aiid)
            if status_request.response["ai_status"] == "ai_ready_to_train":
                break
            else:
                print("Waiting for status to flip ...")

        train = de_rate_limit(hu_api.api.start_training, requester, aiid)
        if not train.success:
            print("Error starting training: {0}".format(train.text))


    def create_new_test_data():
        # load the full word list
        filtered = set([line.strip() for line in read_lines("wordlist.txt")
                        if "'" not in line and line == line.lower()])

        # remove common words
        for word in config.common_words:
            filtered.discard(word)

        print('Unique words ', len(filtered))

        used_question_words = []
        used_answer_words = []
        training_data = []

        for a in range(config.training_sizes[-1] // 2):
            question = create_sentence(filtered, config.common_words, used_question_words)
            answer = create_sentence(filtered, config.common_words, used_answer_words)
            training_data.append((question, answer))

        print("Generated {0} pairs, using {1} unique Q words, {2} unique A words. {3} words left over."
              .format(len(training_data), len(used_question_words), len(used_answer_words), len(filtered)))

        write_file_lines(config.unused_words_filename, filtered)

        for size in config.training_sizes:
            text = []
            for pair in training_data[:size // 2]:
                text.append(pair[0])
                text.append(pair[1])
                text.append('')
            filename = config.training_filename + "_" + str(size)
            write_file_lines(filename, text)


    def create_and_train_new_ais():

        aiids = []
        for size in config.training_sizes:
            filename = config.training_filename + "_" + str(size)
            aiids.append(create_upload_ai(size, filename))

        for aiid in aiids:
            train_start_ai(aiid)


    def have_we_got_the_files():
        missing_files = False
        for size in config.training_sizes:
            filename = config.training_filename + "_" + str(size)
            if not Path(filename).is_file():
                missing_files = True
                print("Missing {0}; recreating all data files".format(filename))
        return not missing_files


    requester = hu_api.api.ApiRequester(config.url_root, config.auth, [])

    check_api_available(requester)

    # if files are missing then recreate from scratch
    if not have_we_got_the_files():
        create_new_test_data()

    # delete all the old load test AIs
    delete_load_test_ais()

    # upload new training data
    create_and_train_new_ais()


if __name__ == "__main__":
    config = make_config()
    main()