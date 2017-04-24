# WebHook Example
In this folder there are several files relating to using WebHooks in the platform, as an example of what the requests and responses look like, and example code for how to use them.

## The Request  
The [payload.json](payload.json) file provides an example of the JSON payload your endpoint will receive when a request is made from our service. It currently contains three main attributes for you to use.

The first attribute is the **intentName** attribute. The intentName attribute is simply that, the name of the intent that you created in the platform that you've triggered to use your endpoint.

The second is the **memoryVariables** attribute. This contains the entity variables that your intent has captured, with some extra information. It will tell you the:  
**currentValue** - The current value held by the variable.  
**isMandatory** - Whether this variable was mandatory.  
**entityKeys** - The possible values that this variable could have, including the current value.  
**prompts** - The prompts used to ascertain this value.  
**timesPrompted** - The number of times the user was prompted to capture the value.  
**timesToPrompt** - The maximum number of times to prompt the user.  

The third is the **chatResult** attribute. We'll go into further detail in the future about the attributes of this, but the key one you may be interested in is:  
**query** - What the user has said to prompt this call.

## The Response
Currently, the response you must return is very simple, as demonstrated in [response.json](response.json). It requires a JSON response, that takes a single attribute, **text**, which is the text response you want to return to the user. If this is left blank, it will default to one of the responses specified in the intent.

## Example Code
We've provided an example Python in [main.py](main.py) program that demonstrates how to handle the example [payload.json](payload.json). To use this example, you will need to install Flask Restful which can be achieved with pip:  
`pip install flask-restful`

Flask Restful is compatible with most versions of Python, please check their [documentation](http://flask-restful.readthedocs.io/en/0.3.5/installation.html) to check compatibility.