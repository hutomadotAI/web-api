import pymysql
import os

host = os.getenv("DBHOST", "localhost")
port = os.getenv("DBPORT", 3306)
user = os.getenv("DBUSER", "root")
passwd = os.getenv("DBPASS", "password")


class QueryData:
    def __init__(self):
        self.conn = None

    def __enter__(self):
        self.conn = pymysql.connect(host=host, port=int(port), user=user, passwd=passwd, db='data_storage')
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.conn.close()

    def getValueForRow(self, indexColumn, index, valueColumn, table):
        try:
            cur = self.conn.cursor()
            cur.callproc("data_storage.getData", [table, indexColumn, index, valueColumn])
            return cur.fetchone()[0]
        except BaseException:
            return "A database error occurred."
        finally:
            cur.close()

    def getIntentMapping(self, intentName):
        try:
            cur = self.conn.cursor(pymysql.cursors.DictCursor)
            cur.callproc("data_storage.getIntentMapping", [intentName])
            return cur.fetchone()
        except BaseException:
            return "A database error occurred."
        finally:
            cur.close()
