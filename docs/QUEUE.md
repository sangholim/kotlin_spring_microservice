## Queue
#### Rabbit
- ${HOST-URL}:15672/#/queues
- login/password: `guest/guest`
### Kafka
- 주키퍼랑 같이 사용 (관리자)
- docker 에서 토픽 체크
  - `docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper --list`
- docker 에서 product 토픽 체크
  - `docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh--describe --zookeeper zookeeper --topic products`
- docker 에서 product 토픽, 메시지 확인
  - `docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic products --from-beginning --timeout-ms 1000`
- docker 에서 product 토픽의 특정 파티션 메시지 확인
  - `docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic products --from-beginning --timeout-ms 1000 --partition 1`
