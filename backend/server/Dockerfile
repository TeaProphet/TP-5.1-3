FROM python:3.11.3-bullseye

ENV PYTHONUNBUFFERED 1

RUN apt-get update && apt-get install -y python3-dev

WORKDIR /code

COPY requirements.txt /code/
RUN pip install -r requirements.txt

COPY . /code/
WORKDIR /code/

EXPOSE 8000