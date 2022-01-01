#!/bin/bash
if [[ "$1" = "build" ]]
then
   ./gradlew build;
    docker-compose build 
    docker images
elif [[ "$1" = "run" ]]
then
    docker-compose up -d
    docker ps
elif [[ "$1" = "log" ]]
then
    docker-compose logs -f
elif [[ "$2" = "down" ]]
then
    docker-compose down
fi
