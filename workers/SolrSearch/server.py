from urllib.parse import urlencode
from aiohttp import web
import os
import requests

solrUrl = os.getenv("SOLR_URL", "http://52.44.202.141:8983/solr/")

async def handle(request):
    body = await request.json()

    chatResult = body['chatResult']

    try:
        query = chatResult['query'].lower().split("search for ",1)[1]
        solrResult = requests.get(solrUrl + "nokia/select?wt=json&indent=true&q=" + query)
        result = solrResult.json()['response']['docs'][0]['id']
        return web.Response(text=result)
    except BaseException as E:
        return web.Response(text="A processing error occurred.")

app = web.Application()
app.router.add_post('/', handle)

web.run_app(app, port=5859)