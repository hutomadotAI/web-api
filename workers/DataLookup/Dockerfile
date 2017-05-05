FROM python:3

COPY server.py /
COPY querydata.py /
COPY requirements.txt /

ENV DBHOST localhost
ENV DBPORT 3306
ENV DBUSER root
ENV DBPASS password
ENV PORT 5858

RUN pip install -r requirements.txt
CMD [ "python", "-u", "server.py" ]