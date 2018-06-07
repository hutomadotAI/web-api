#!/usr/bin/env python
"""Script to build code"""
import argparse
import datetime
import os
import re
from pathlib import Path
import subprocess
import mysql.connector

SCRIPT_PATH = Path(os.path.dirname(os.path.realpath(__file__)))
ROOT_DIR = SCRIPT_PATH.parent.parent
ALTERSCRIPT_PATH = ROOT_DIR/'db'
ALTERSCRIPT_INCLUDED_PATH = ALTERSCRIPT_PATH / 'alterscript_included'

MIGRATIONS_TABLE = """CREATE TABLE IF NOT EXISTS `migration_status`  (
  `enforce_one_row` enum('only') not null unique default 'only',
  `migration_date` date NOT NULL,
  `migration_id` int(11) NOT NULL,
  PRIMARY KEY (`enforce_one_row`)
) ENGINE=InnoDB;
"""

def get_migration_level(cnx, cursor):
    cursor.execute(MIGRATIONS_TABLE)
    cnx.commit()
    
    query = ("SELECT migration_id, migration_date FROM migration_status LIMIT 1;")
    cursor.execute(query)
    migration = -1
    date = None
    for (migration, date) in cursor:
        pass    

    print("Migration status: #{} on {}".format(migration, date))
    return migration

def apply_alterscripts(cnx, cursor, migration_level):
    REGEX_ALTER_INDEX = re.compile("alterscript-(\d+).sql")
    alterscript_included = sorted([int(REGEX_ALTER_INDEX.match(file.name).group(1))
                    for file in ALTERSCRIPT_INCLUDED_PATH.glob("alterscript-*.sql")])
    first_item = alterscript_included[0]
    last_item = alterscript_included[-1]
    print("Alterscripts included in structure.sql from {} to {}".format(first_item, last_item))
    alterscripts = sorted([int(REGEX_ALTER_INDEX.match(file.name).group(1))
                    for file in ALTERSCRIPT_PATH.glob("alterscript-*.sql")])
    first_item = min(first_item, alterscripts[0])
    last_item = max(last_item, alterscripts[-1])
    print("Alterscripts from {} to {} available".format(first_item, last_item))

    if migration_level >= last_item:
        print("No migrations to apply")
    else:
        migration_start = migration_level + 1
        migration_range = range(max(first_item, migration_start), last_item + 1)
        migrations = []

        for ii in migration_range:
            if ii in alterscript_included:
                migrations.append((ii, ALTERSCRIPT_INCLUDED_PATH / 'alterscript-{:04d}.sql'.format(ii)))
            elif ii in alterscripts:
                migrations.append((ii, ALTERSCRIPT_PATH / 'alterscript-{:04d}.sql'.format(ii)))
            else:
                raise Exception("Missing alterscript " + ii)

        REGEX_DELIMITER = re.compile('DELIMITER *(\S*)',re.I)
        UPDATE_MIGRATION = ("REPLACE INTO migration_status "
                            "(enforce_one_row, migration_date, migration_id) " 
                            "VALUES ('only', %s, %s) ")
        for ii, migration in migrations:                    
            print('*****************************************************')
            print("*** Applying migration {} ({})".format(ii, migration))
            with migration.open(mode='r', encoding="utf8") as file_handle:
                migration_data = file_handle.read()        
            
            # split script according to special delimiters
            # As per https://stackoverflow.com/a/16950944/694641
            migration_with_delimiters = REGEX_DELIMITER.split(migration_data)
            # Add first delimiter which is default
            migration_with_delimiters.insert(0, ';')
            # Create a list of "(delimiter, content) tuples"
            segments = zip(migration_with_delimiters[0::2], migration_with_delimiters[1::2])

            for delimiter, segment in segments:
                # Split into individual queries (detect the correct delimiter)
                query_list = segment.split(delimiter)
                for query in query_list:
                    query = query.strip()
                    if not query:
                        continue
                    print("Executing '{}'".format(query))
                    cursor.execute(query)
                    
            # if got here this migration was successfull!
            cursor.execute(UPDATE_MIGRATION, (datetime.date.today(), ii))
            cnx.commit()
        
        print('**** MIGRATION COMPLETE (up to alterscript #{})'.format(ii))
        print('*****************************************************')
            

def main(args):
    """Main function"""


    config = {
    'user': 'root',
    'password': args.password,
    'host': args.host,
    'port': args.port,
    'database': 'hutoma',
    'raise_on_warnings': False,
    'buffered': True
    }

    cnx = mysql.connector.connect(**config)
    cursor = cnx.cursor()
    print("Connected to DB")
    try:
        migration_level = get_migration_level(cnx, cursor)
        apply_alterscripts(cnx, cursor, migration_level)
    finally:
        cursor.close()
        cnx.close()

if __name__ == "__main__":
    PARSER = argparse.ArgumentParser(
        description='Apply alterscript command-line')
    PARSER.add_argument(
        '--host', help='MySql host machine', default="localhost")
    PARSER.add_argument(
        '--port', help='MySql port', default=3306, type=int)
    PARSER.add_argument(
        '--password', help='MySql root password', default="password")
    ARGS = PARSER.parse_args()
    main(ARGS)
