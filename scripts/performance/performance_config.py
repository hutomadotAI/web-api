import argparse
import os
import random
import urllib

from api_cli import arg_error

class AiDefinition:
    def __init__(self, file_prefix, ai_prefix, response_prefix, defs):
        self.file_prefix = file_prefix
        self.ai_prefix = ai_prefix
        self.response_prefix = response_prefix
        self.defs = defs


class Config:
    def __init__(self, url, server, bots):

        self.bots = bots

        self.proxies = {
            #'http': 'http://127.0.0.1:4444',
        }
        self.url_root = url
        self.server = server

        # dev token for donkey@hutoma.com (Load Testing account)
        self.auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkSQTM6NEY5NE3ZTUZBNdk0TTJN0k4yQj3cTUREvDZMNU85QUA6VaAAAAAP__.c6zeCRsUV8Wd5X3ZjDPrHDoUoOzjnZZoWKy0tEY7rN4'

        # test-role token for donkey@hutoma.com, used only for load-test chat endpoint
        self.chat_auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8SGuwSFKOkrFpUlAkSQTM6NEY5NE3ZTUZBNdk0TTJN0k4yQj3cTUREvDZMNU85QUA6VaAAAAAP__.eUytifp7MPitydSm1sGQ8FVlp97CtgMvAlrt0AEyoRE'

        self.load_test_ais = \
            AiDefinition("set_load_", "Load-Test", "lt-", \
            [ (x, x, "set_load_" + str(x)) for x in range(100, 3000, 500)])

        self.chat_test_ais = \
            AiDefinition("set_chat_", "Multichat-Test", "mt-", \
            [ (20, x, "set_chat_" + str(x)) for x in range(1, 16)])

        self.aiml = {"chitchat": "1",
                      "mika": "4"}

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

        self.aiml_words = {'A', 'ABILITIES', 'ABLE', 'ABOUT', 'ABSOLUTELY', 'ACCESS', 'ACCOUNT', 'ACTIVATE', 'ACTOR',
                           'ACTUALLY', 'ADDRESS', 'AGAIN', 'AGE', 'AH', 'AHA', 'AHEM', 'AHH', 'ALARM', 'ALIENS', 'ALL',
                           'ALREADY', 'ALRIGHT', 'ALSO', 'ALWAYS', 'AM', 'AMAZING', 'AMERICAN', 'AN', 'AND', 'ANGRY',
                           'ANIMAL', 'ANIMATION', 'ANOTHER', 'ANSWER', 'ANT', 'ANTS', 'ANY', 'ANYTHING', 'APOLOGIZE',
                           'APP', 'APPARENTLY', 'APPLICATION', 'ARE', 'ARTIST', 'ASDF', 'ASIAN', 'ASK', 'ASKED',
                           'ASKING', 'AT', 'AUNT', 'AUNTS', 'AW', 'AWESOME', 'AWSOME', 'AYUH', 'B', 'BABE', 'BABY',
                           'BACK', 'BAD', 'BAND', 'BANK', 'BASKETBALL', 'BATMAN', 'BATTERY', 'BC', 'BE', 'BEAUTIFUL',
                           'BECAUSE', 'BED', 'BEEN', 'BEING', 'BELIEVE', 'BEST', 'BETTER', 'BETWEEN', 'BIEBER', 'BIG',
                           'BIRDS', 'BIRTH', 'BIRTHDATE', 'BIRTHDAY', 'BIRTHPLACE', 'BIT', 'BLACK', 'BLEEDING',
                           'BLONDE', 'BONJOUR', 'BOOBS', 'BOOK', 'BORING', 'BORN', 'BOSS', 'BOT', 'BOTMASTER',
                           'BOY', 'BOYFRIEND', 'BRAND', 'BRITISH', 'BRO', 'BROS', 'BROTHER', 'BROTHERS', 'BROWSER',
                           'BUSY', 'BUT', 'BUY', 'BY', 'BYE', 'CALCULATOR', 'CALENDAR', 'CALL', 'CALLED', 'CAMERA',
                           'CHINESE', 'CITY', 'CLIENT', 'client.', 'CLIENTS', 'CLOCK', 'CLOSEST', 'COLDPLAY',
                           'COLOR', 'COLORS', 'COM', 'COME', 'COMPUTER', 'CONSIDER', 'CONTACT', 'CONTACTS', 'COOL',
                           'CORRECT', 'COST', 'COULD', 'COUNTRY', 'COURSE', 'CRAZY', 'CREAM', 'CREATED', 'CREATOR',
                           'CURRENT', 'CUTE', 'CUZ', 'CYA', 'D', 'DAD', 'DADA', 'DADAS', 'DADDY', 'DAMN', 'DARE',
                           'DATE', 'DATS', 'DAUGHTER', 'DAUGHTERS', 'DAY', 'DEAR', 'DEFINE', 'DEFINITELY', 'DEFINITION',
                           'DEN', 'DEPENDS', 'DESCRIBE', 'DESIGNED', 'DEVICE', 'DIAL', 'DICKS', 'DID', 'DIDNT', 'DIET',
                           'DIFFERENCE', 'DIRECTIONS', 'DISTANCE', 'DISTINGUISH', 'DO', 'DOES', 'DOG', 'DOING', 'DONT',
                           'dont', 'DRINK', 'DRIVING', 'DUDE', 'DUH', 'DUMB', 'DUMMY', 'DUNNO', 'DUPLICATE', 'EAT',
                           'EBAY', 'EDUCATION', 'EHM', 'EMAIL', 'EMOTIONS', 'END', 'ENGLISH', 'ENJOY', 'ER', 'ET',
                           'ETHNICITY', 'EVEN', 'EVENING', 'EVER', 'EVERYTHING', 'EW', 'EWW', 'EXACTLY', 'EXAMPLE',
                           'EXPLAIN', 'EYECOLOR', 'EYES', 'FACEBOOK', 'FAR', 'FAT', 'FATHER', 'FAVOIRTE', 'FAVORITE',
                           'FAVORITES', 'FAVOURITE', 'FEEL', 'FEELING', 'FEELINGS', 'FEMALE', 'FINALLY', 'FIND', 'FINE',
                           'FIRED', 'FLASHLIGHT', 'FLAVOR', 'FO', 'FOOD', 'FOOTBALL', 'FOR', 'FORECAST', 'FRIEND',
                           'FRIENDS', 'FROM', 'FUN', 'FUNCTION', 'FUNNY', 'G', 'GALLERY', 'GAS', 'GAY', 'GEE', 'GENDER',
                           'GERMAN', 'GET', 'GETTING', 'GIMME', 'GIRL', 'GIRLFRIEND', 'GIVE', 'GLAD', 'GMAIL', 'GO',
                           'GOD', 'GOING', 'GOLD', 'GOLF', 'GONNA', 'GOOD', 'GOODBYE', 'GOODNIGHT', 'GOOGLE', 'GOSH',
                           'GOT', 'GOTO', 'GOTTA', 'GRACE', 'GRACIAS', 'GRANDDAD', 'GRANDDADS', 'GRANDFATHER',
                           'GRANDFATHERS', 'GRANDMA', 'GRANDMAS', 'GRANDMOM', 'GRANDMOMS', 'GRANDMOTHER', 'GRANDMOTHERS',
                           'GUESS', 'HA', 'HAH', 'HAHA', 'HAHAHA', 'HAHAHAHA', 'HAHAHAHAHA', 'HAI', 'HALLO', 'HALO',
                           'HAO', 'HAPPEN', 'HAPPENED', 'HAPPY', 'HAS', 'HAVE', 'HAY', 'HE', 'HEADLINES', 'HEARD', 'HEH',
                           'HEHE', 'HEHEHE', 'HEIGHT', 'HELL', 'HELLO', 'HELP', 'HER', 'HERE', 'HES', 'HEY', 'HEYA',
                           'HEYY', 'HI', 'HII', 'HIIII', 'HIS', 'HISTORY', 'HM', 'HMM', 'HMMM', 'HMMMM', 'HMMMMM',
                           'HOBBIES', 'HOBBY', 'HOLA', 'HOME', 'HOMO', 'HONEY', 'HORNY', 'HORSE', 'HOT', 'HOW', 'HUBBY',
                           'HUBBYS', 'HUH', 'HUMAN', 'HUNGRY', 'HUTOMA', 'HY', 'I', 'ICE', 'ID', 'IDC', 'IDEA', 'IDIOT',
                           'IDK', 'IF', 'IK', 'ILL', 'IM', 'IMA', 'IMPLODE', 'IN', 'INFORMATION', 'INTERJECTION',
                           'INTERNET', 'INVENTED', 'IQ', 'IRIS', 'IS', 'IT', 'ITS', 'JA', 'JAPANESE', 'JERK', 'JESUS',
                           'JK', 'JOB', 'JOKE', 'JOKES', 'JUS', 'JUST', 'JUSTIN', 'K', 'KAY', 'KEWL', 'KIDDING', 'KIDS',
                           'KIND', 'KINDA', 'KK', 'KKK', 'KNEW', 'KNO', 'KNOW', 'L', 'LAME', 'LANGUAGE', 'LANGUAGES',
                           'LAST', 'LATER', 'LATEST', 'LAUGH', 'LAUNCH', 'LEARN', 'LEARNED', 'LEAVE', 'LEAVING', 'LEFT',
                           'LESBIAN', 'LET', 'LETS', 'LEVEL', 'LIFE', 'LIKE', 'LINK', 'LIST', 'LISTEN', 'LITTLE', 'LIVE',
                           'LIVING', 'LMFAO', 'LOCATE', 'LOCATED', 'LOCATION', 'LOG', 'LOL', 'LONDON', 'LONELY', 'LOOK',
                           'LOOKS', 'LOOKUP', 'LOT', 'LOVE', 'LUCK', 'LYING', 'M', 'MA', 'MAD', 'MADE', 'MAIL', 'MAJOR',
                           'MAKE', 'MAKES', 'MALAYSIA', 'MALE', 'MAM', 'MAN', 'MANAGER', 'MANCHESTER', 'MANY', 'MAP',
                           'MAPS', 'MARRIED', 'MARRY', 'MAS', 'MASTER', 'MATH', 'MAY', 'MAYBE', 'MCDONALD', 'ME', 'MEAN',
                           'MEANING', 'MEANS', 'MEANT', 'MEET', 'MENT', 'MERCI', 'MESSAGE', 'MESSAGES', 'MESSAGING',
                           'MEXICO', 'MHM', 'MINAJ', 'MIND', 'MISS', 'MISSION', 'MISUNDERSTOOD', 'MITSUKU', 'MM',
                           'MMM', 'MMMM', 'MMMMM', 'MODEL', 'MOI', 'MOM', 'MOMMA', 'MOMMAS', 'MORNING', 'MOTHER',
                           'MOUTH', 'MOVIE', 'MOVIES', 'MUCH', 'MUM', 'MUMMA', 'MUMMAS', 'MUSIC', 'MUSLIM', 'MY',
                           'N', 'NA', 'NAH', 'NAME', 'NAMED', 'NAMES', 'NATIONALITY', 'NAW', 'NEAREST', 'NEED',
                           'NEPHEW', 'NEPHEWS', 'NEVER', 'NEVERMIND', 'NEW', 'NEWS', 'NI', 'NICE', 'NICKI', 'NIECE',
                           'NIECES', 'NIGHT', 'NIKE', 'NIZZLE', 'NM', 'NO', 'NOO', 'NOOO', 'NOP', 'NOPE', 'NOT', 'not.',
                           'NOTHIN', 'NOTHING', 'NOW', 'NP', 'NUMBER', 'NUTHING', 'O', 'OBAMA', 'OF', 'OFF', 'OFFICE',
                           'OH', 'OHHH', 'OHK', 'OI', 'OK', 'OKAY', 'OKK', 'OLA', 'OLD', 'ON', 'ONE', 'only', 'OOH',
                           'OOOO', 'OPEN', 'OPINION', 'OR', 'ORIENTATION', 'OTHER', 'OUT', 'OUTSIDE', 'PAGE', 'PANDORA',
                           'PENIS', 'PEOPLE', 'PERCENTAGE', 'PERSON', 'PERSONALITY', 'PET', 'PHONE', 'PHOTO', 'PIC',
                           'PICTURE', 'PICTURES', 'PISS', 'PLAY', 'PLAYER', 'PLEASE', 'PLS', 'PLZZ', 'POOP', 'POPULATION',
                           'PREPARE', 'PRESIDENT', 'PRETTY', 'PROBLEM', 'PROFESSOR', 'PROFILE', 'PROGRAMMED',
                           'PROSTITUTE', 'PROVINCE', 'PUB', 'PULL', 'PUT', 'QUESTION', 'QUIET', 'QUITE', 'R', 'RAIN',
                           'RAINING', 'RE', 'READ', 'REAL', 'REALLY', 'REALY', 'RECHERCHE', 'REGARDING', 'RELIGION',
                           'RELIGIOUS', 'REMEMBER', 'REMIND', 'REPORT', 'RESEARCH', 'RESIDENCE', 'RETARDED', 'RIGHT',
                           'ROAD', 'ROBOT', 'ROBOTS', 'ROFL', 'S', 'SAD', 'SAID', 'SAY', 'SAYING', 'SCHOOL', 'SEARCH',
                           'SEE', 'SEEN', 'SEND', 'SENSE', 'SET', 'SEXY', 'SHE', 'SHH', 'SHIZZLE', 'SHO', 'SHOE',
                           'SHOULD', 'SHOW', 'SHUT', 'SHUTUP', 'SI', 'SICK', 'SIGH', 'SING', 'SINGER', 'SINGLE', 'SIR',
                           'SIRI', 'SIS', 'SISS', 'SISTER', 'SISTERS', 'SIZE', 'SKILLS', 'SKYPE', 'SLEEP', 'SLEEPY', 'SMART',
                           'SMARTER', 'SMESSAGE', 'SMS', 'SO', 'SOME', 'SOMETHING', 'SON', 'SONG', 'SONS', 'SOO',
                           'SOON', 'SORRY', 'SOUND', 'SOUNDS', 'SPANISH', 'SPEAK', 'SPEAKING', 'SPECIES', 'SPELL',
                           'SPELLED', 'SPORT', 'STAR', 'STATE', 'STATES', 'STATION', 'STATS', 'status', 'STFU', 'STILL',
                           'STOP', 'STORE', 'STORY', 'STREET', 'STUPID', 'SUBJECT', 'SUCK', 'SUP', 'SUPER', 'SURE',
                           'SURNAME', 'TACO', 'TAKE', 'TALK', 'TALKING', 'TALL', 'TASK', 'TAXI', 'TEACH', 'TEACHER',
                           'TEAM', 'TELEPHONE', 'TELEVISION', 'TELL', 'TEMPERATURE', 'TEST', 'TEXT', 'THAN', 'THANK',
                           'THANKS', 'THANKYOU', 'THANX', 'THAT', 'THATS', 'THE', 'THEN', 'THERE', 'THEY', 'THEYRE', 'THING',
                           'THINK', 'THIS', 'THOUGHT', 'THX', 'TIME', 'TIRED', 'TO', 'TODAY', 'TODAYS', 'TOLD', 'TOMORROW',
                           'TOO', 'TOPIC', 'TOWN', 'TRANSCRIPT', 'TRANSCRIPTS', 'TRANSLATE', 'TREK', 'TRYING', 'TU', 'TUBE',
                           'TV', 'TWITTER', 'TYPE', 'U', 'UGH', 'UGLY', 'UH', 'UM', 'UMBRELLA', 'UMM', 'UMMM', 'UN', 'UNCLE',
                           'UNCLES', 'UNDERSTAND', 'UNDERSTOOD', 'UNITED', 'UP', 'update', 'US', 'USELESS', 'USUALLY',
                           'VAGINA', 'VERSION', 'VERY', 'VIEW', 'VIRGIN', 'VOCABULARY', 'WAKE', 'WALGREENS', 'WANA',
                           'WANNA', 'WANT', 'WAS', 'WASSUP', 'WAT', 'WATZ', 'WAY', 'WE', 'WEARING', 'WEATHER', 'WEBSITE',
                           'WEIGH', 'WEIGHT', 'WELCOME', 'WELL', 'WERE', 'WHAT', 'WHAY', 'WHEN', 'WHERE', 'WHICH', 'WHO',
                           'WHOA', 'WHOM', 'WHT', 'WHY', 'WICH', 'WIFEY', 'WIFEYS', 'WIKIPEDIA', 'WILL', 'WITH', 'WOAH',
                           'WOMAN', 'WONDERFUL', 'WOO', 'WOOHOO', 'WORD', 'WORDS', 'WORK', 'WORLD', 'WORLDS', 'WOULD',
                           'WRONG', 'WUT', 'WWW', 'WYD', 'XBOX', 'Y', 'YA', 'YAA', 'YAAY', 'YAH', 'YAHOO', 'YAY', 'YE',
                           'YEA', 'YEAH', 'YEAR', 'YEARS', 'YED', 'YEH', 'YELP', 'YEP', 'YEPP', 'YES', 'YESS', 'YEY',
                           'YIPPEE', 'YO', 'YOU', 'YOUDOING', 'YOUR', 'YOURSELF', 'YOUTUBE', 'YRS', 'YUP', 'YWS',
                           'ZOMBIE'}

        self.unused_words_filename = "set_unused_words"
        self.training_filename = "set_training"
        self.chat_filename = "set_bot"

        self.chat_bot_count = 15


class DevConfigNew(Config):
    def __init__(self, url, server, bots):
        Config.__init__(self, url, server, bots)

        # dev token for donkey@hutoma.com (Load Testing account)
        self.auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAEUMDRxcQ1jUEAjQCBpRqAQAAAP__.gIyZFtV2SHF2NL8JxtBZ8jl_rHfwucBk_bk_1HfyQrA'

        # test-role token for donkey@hutoma.com, used only for load-test chat endpoint
        self.chat_auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8SGuwSFKOkrFpUlAEUMDRxcQ1jUEAjQCBpRqAQAAAP__.02soquVIA-keCyCumtjTPDLciF_EvbnGA9uxZ__AaJ4'


class SnowflakeConfigOld(Config):
    def __init__(self, url, server, bots):
        Config.__init__(self, url, server, bots)

        # dev token for donkey@hutoma.com (Load Testing account)
        self.auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkSQTM6NEY5NE3ZTUZBNdk0TTJN0k4yQj3cTUREvDZMNU85QUA6VaAAAAAP__.kiZdfwMJlbfIg7eVSsvnBuZHh1fQfASuya2D3GxozXE'

        # test-role token for donkey@hutoma.com, used only for load-test chat endpoint
        self.chat_auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8SGuwSFKOkrFpUlAkSQTM6NEY5NE3ZTUZBNdk0TTJN0k4yQj3cTUREvDZMNU85QUA6VaAAAAAP__.sWOsK9mVxRJd89pmUPes_YC4EZLPuSuPbHJ-KSp8SHA'


class SnowflakeConfigNew(Config):
    def __init__(self, url, server, bots):
        Config.__init__(self, url, server, bots)

        # dev token for donkey@hutoma.com (Load Testing account)
        self.auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAEUMDRxcQ1jUEAjQCBpRqAQAAAP__.meHkjpLbijX4YJXrLyVyeuVEdc0HzpDJdrF1lMBYLgY'

        # test-role token for donkey@hutoma.com, used only for load-test chat endpoint
        self.chat_auth = 'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8SGuwSFKOkrFpUlAEUMDRxcQ1jUEAjQCBpRqAQAAAP__.sY2z5ubn7oYW6_t14MWMPtds5gcdIy-RzigtuqWtdUI'


def make_config():

    parser = argparse.ArgumentParser(description='Hutoma performance test runner')
    parser.add_argument('--url', help='URL to API. Can also be set using the HUTOMA_API_CLI_URL environment variable.')
    parser.add_argument('--server', help='which server to target. rnn or wnet')
    parser.add_argument('--env', help='what environment to target. dev, sf, dev-old, sf-old')
    parser.add_argument('--bots', help='how many different bots to hit')
    parser.add_argument('--no-chitchat', help="Don't test chit-chat", action="store_true")

    args = parser.parse_args()

    server = args.server.lower() if args.server else ''
    server = server if server in ['wnet', 'rnn', 'aiml', 'intent'] \
        else 'wnet'

    url_raw = args.url
    if url_raw is None or len(url_raw) == 0:
        url_raw = os.environ.get('HUTOMA_API_CLI_URL')

    if url_raw is None or len(url_raw) == 0:
        arg_error(parser, "URL is not set")

    url_parsed = urllib.parse.urlparse(url_raw)
    if not url_parsed.scheme.startswith('http'):
        url_parsed = urllib.parse.urlparse('http://' + url_raw)
    url = url_parsed.geturl()
    print("API URL is", url)

    bot_count = 1
    if args.bots:
        try:
            bot_count = int(args.bots)
        except ValueError:
            raise Exception("Bad value for --bots: {}".format(args.bots))
    if not (1 <= bot_count <= 15):
        print("--bots must be between 1 and 15")
        exit(0)

    env = args.env.lower() if args.env else 'dev'
    options = {'dev': DevConfigNew(url, server, bot_count),
               'sf': SnowflakeConfigNew(url, server, bot_count),
               'dev-old': Config(url, server, bot_count),
               'sf-old': SnowflakeConfigOld(url, server, bot_count)}

    try:
        config = options[env]
    except KeyError:
        raise Exception("Unrecognised config environment {}".format(args.env))
        
    config.chitchat = not args.no_chitchat

    return config
