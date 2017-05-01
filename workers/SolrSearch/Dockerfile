FROM python:3

COPY server.py /
COPY requirements.txt /

ENV SOLR_URL="http://52.44.202.141:8983/solr/"

RUN pip install -r requirements.txt
CMD [ "python", "-u", "server.py" ]