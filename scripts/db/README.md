DB scripts

## How to use
Requires Python and pipenv

Go into this directory and call `pipenv install --dev` on first time, or if the requirements change, to initialise a virtual env, with all dependencies managed.

Then run `pipenv shell` to open a shell with all the prerequisite libraries install. Then,

`python apply-alterscript.py -h`

will show the CLI help.

The `retrain-all-bots.py` script has been deleted as this functionality is now inside a dockerised container and made more versatile. See in `workers/version_migrator` folder in this Git repo.
