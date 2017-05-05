from aiohttp import web, http_exceptions
import os
from querydata import QueryData

class AcceptChooser:
    def __init__(self):
        self._accepts = {}

    async def do_route(self, request):
        for accept in request.headers.getall('Content-Type', []):
            acceptor = self._accepts.get(accept)
            if acceptor is not None:
                return (await acceptor(request))
        raise http_exceptions.InvalidHeader("Content-Type")

    def reg_acceptor(self, accept, handler):
        self._accepts[accept] = handler

async def handle_json(request):
    body = await request.json()

    intentName = body['intentName']
    variables = body['memoryVariables']
    with QueryData() as querydata:
        intentMapping = querydata.getIntentMapping(intentName)

        try:
            knownValue = next((x for x in variables if x['name'] == intentMapping['key_entity']), None)['currentValue']
            knownColumn = intentMapping['key_column']
            targetValue = next((x for x in variables if x['name'] == intentMapping['value_entity']), None)['currentValue']
        except BaseException:
            return web.Response(text="A processing error occurred.")

        return web.Response(text=querydata.getValueForRow(knownColumn, knownValue, targetValue, intentName))

app = web.Application()
chooser = AcceptChooser()
app.router.add_post('/', chooser.do_route)

chooser.reg_acceptor('application/json', handle_json)

web.run_app(app, port=os.getenv("PORT", 5858))