#!/bin/bash

echo 'sh app.sh $service-name'
echo 'service-name: product, product-composite, review, recommendation'
services=("product" "product-composite" "review" "recommendation")
if [[ ! " ${services[@]} " =~ " $1 " ]]; then
    echo "unknown service - $1"
    exit
fi

echo "process $1 $@"
if [[ "$2" = "start" ]]
then
    nohup ./gradlew ":$1:bootrun" > /dev/null &
elif [[ "$2" = "stop" ]]
then
    kill -9 `cat $1/$1.pid`
    #rm "$1/$1.pid"
fi

