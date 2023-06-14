#!/bin/bash

docker-compose stop server

docker-compose stop web
docker-compose rm web

docker-compose pull
docker-compose up -d web
docker-compose up -d server
