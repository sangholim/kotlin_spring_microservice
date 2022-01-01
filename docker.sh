#!/bin/bash

echo "service-name: product, product-composite, review, recommendation"
services=("product" "product-composite" "review" "recommendation")
if [[ ! " ${services[*]} " =~ $1 ]]; then
    echo "unknown service - $1"
    exit
fi

cd ./"$1" || exit

if [[ "$2" = "build" ]]
then
    docker $2 -t "$1" .
    docker images | grep "$1"
elif [[ "$2" = "run" ]]
then
# -d = process background run
# --rm = container close when ctrl + c
# -p${container_port}:{docker_hot_port}
# -e = enable environment variables  
    docker $2 -d --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" --name $1 $1
elif [[ "$2" = "log" ]]
then
# -f = option output continuous log
    docker logs $1 -f
elif [[ "$2" = "rm" ]]
then
# -f = force docker container close, remove
    docker rm -f $1
fi

