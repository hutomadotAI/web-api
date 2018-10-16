# Base off of the Python image
FROM python:3.7.0-slim-stretch
LABEL maintainer "Paul Annetts <paul.annetts@hutoma.com>"

RUN echo "2018-10-16" > /os_patch_date.txt

RUN apt-get update \
 && apt upgrade -y \
 && apt install -y --no-install-recommends \
  build-essential \
 && rm -rf /var/lib/apt/lists/*

# Install pipenv
ENV PIP_NO_CACHE_DIR=false
RUN pip install --upgrade pip==18.1 pipenv==2018.10.13

# Apply the pipenv
COPY Pipfile* /
RUN pipenv install --system

# Create empty storage directory
RUN mkdir -p /data
# Code directory
RUN mkdir -p src/backend

#---------------------------
# create non root user
# define user/group IDs as ARG
ARG USERID=1000
ARG GROUPID=1000
RUN addgroup --system --gid $GROUPID appuser
RUN adduser --system --uid $USERID --gid $GROUPID appuser

RUN chown -R appuser:appuser /data

# Copy the code
COPY *.py src/backend/

#---------------------------
# switch to non root user
USER appuser

# Make available port 9090
EXPOSE 9090
CMD [ "python", "-u", "src/backend/server.py", "--port", "9090" ]