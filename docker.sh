#!/bin/bash

echo "sh docker.sh $service-name"
echo "service-name: product, product-composite, review, recommendation"
services=("product" "product-composite" "review" "recommendation")
if [[ ! " ${services[*]} " =~ $1 ]]; then
    echo "unknown service - $1"
    exit
fi

cd ./"$1"

if [[ "$2" = "build" ]]
then
    docker build -t "$1" .
    docker images | grep "$1"
elif [[ "$2" = "stop" ]]
then
    kill -9 "cat $1/$1.pid"
    rm "$1/$1.pid"
fi

