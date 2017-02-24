class Config:
    def __init__(self):
        self.proxies = {
            #'http': 'http://127.0.0.1:4444',
        }
        # self.urlRoot = "https://api.hutoma.com/v1"
        self.url_root = "http://localhost:15000/v1"
        #self.url_root = "http://10.150.0.4:8080/v1"

        # dev token for donkey@hutoma.com (Load Testing account)
        self.auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkSQTM6NEY5NE3ZTUZBNdk0TTJN0k4yQj3cTUREvDZMNU85QUA6VaAAAAAP__.c6zeCRsUV8Wd5X3ZjDPrHDoUoOzjnZZoWKy0tEY7rN4'

        # test-role token for donkey@hutoma.com, used only for load-test chat endpoint
        self.chat_auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8SGuwSFKOkrFpUlAkSQTM6NEY5NE3ZTUZBNdk0TTJN0k4yQj3cTUREvDZMNU85QUA6VaAAAAAP__.eUytifp7MPitydSm1sGQ8FVlp97CtgMvAlrt0AEyoRE'

        # this is the list of training sizes for each target AI
        self.training_sizes = [x for x in range(100, 3000, 500)]

        # this is the list of (simultaneous requests, total requests) for each load test run
        self.request_pattern = [(x, x * 10) for x in [1, 5, 10]]

        self.common_words = [word.lower() for word in
                             ['the', 'be', 'to', 'of', 'and', 'a', 'in', 'that', 'have', 'I', 'it', 'for',
                              'not', 'on', 'with', 'he', 'as', 'you', 'do', 'at', 'this', 'but', 'his', 'by',
                              'from', 'they', 'we', 'say', 'her', 'she', 'or', 'an', 'will', 'my', 'one', 'all',
                              'would', 'there', 'their', 'what', 'so', 'up', 'out', 'if', 'about', 'who',
                              'get', 'which', 'go', 'me', 'when', 'make', 'can', 'like', 'time', 'no', 'just',
                              'him', 'know', 'take', 'people', 'into', 'year', 'your', 'good', 'some', 'could',
                              'them', 'see', 'other', 'than', 'then', 'now', 'look', 'only', 'come', 'its',
                              'over', 'think', 'also', 'back', 'after', 'use', 'two', 'how', 'our', 'work',
                              'first', 'well', 'way', 'even', 'new', 'want', 'because', 'any', 'these',
                              'give', 'most', 'us']]

        self.unused_words_filename = "set_unused_words"
        self.training_filename = "set_training"
