Runs chat stress tests against the full API.

There are two main scripts in the main folder. One creates the test bots on the target system, the other runs the actual stress tests.

**Create**
`python.exe create_test_data.py --url https://dev.hutoma.com:8502/v1`

* creates a number of training files containing question answer pairs taken from an english dictionary
* creates one empty bot and links it to the chit-chat skill (unless you specify the `--no-chitchat` option)
* creates one intent-only bot

These are created, uploaded and trained on the target system via API calls.
You only need to run this once.

**Stress Test**
`python.exe .\chat_stress_test.py --url https://dev.hutoma.com:8502/v1 --server wnet --bots 1`

This will connect to the target system and start issuing chat requests. It will start with only a few simultaneous calls and then ramp up the number if the system looks like it can handle it. A standard test run lasts 5 minutes, after which a CSV file `results.csv` will be written to the same directory as the scripts and the test data.

***Parameters***
`--url` the URL for the API
`--server` the sub-system to target. This can be `wnet`, `rnn`, `aiml` or `intent`. Default: `wnet`
`--bots` when targeting `wnet` or `rnn` you can send all chat requests to the same bot `--bots 1` or spread them across _n_ bots where _0 < n < 16_
`--env` specifies which credentials to use. Valid entries are `dev`, `dev-old`, `sf`, `sf-old`, where `sf` means snowflake. The `-old` creds do not use the `10ad10ad-1111-1111-1111-111111111111` guid.

If you get an error 403 when connecting to the API then try using a different `--env` setting.

*Advanced*

Have a look inside the `chat_stress_test.py` file. Somewhere at the top are a few constants


`TARGET_LATENCY_SECONDS = 2.5`  
The stress script will auto-regulate the number of calls per second to try to achieve this latency. Raising this number means that the script will throw more at the API.

`TIME_WINDOW_SECONDS = 10.0` 
A factor for how quickly the script will react to changes in timings. This completely recalibrates the flux capacitor so don't change it.

`RUN_LENGTH_SECONDS = 5.0 * 60.0` 
The length of time to run the script for, in seconds. If you set this to an hour or more then be warned that the results processing phase might take a few minutes to complete rather than seconds, and the script is not very good at communicating this.

`LOAD_MIN = 10.0`
The minimum number of simultaneous calls to make. The script will not autoregulate below this.

`LOAD_MAX = 100.0`
As above but the maximum. If you change these two numbers to be the same then you get a "fixed load test" e.g. 30 min 30 max to certify that the server is good for up to 30 chats per second.

`SPEED_SCALING_FACTOR = 2.0`
Another factor for how quickly the script autoregulates. Raise this to ramp up more quickly. It also means that the script will down-regulate more quickly if it sees an error. Setting this too high will result in classic PID oscillations. Good values are 1.2 - 4.0 but 2.0 is about right unless you're using tri-blade propellers rather than the normal Gemfan 5030s.



