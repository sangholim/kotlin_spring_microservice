## Spring Cloud Stream
- Message `메시지`: 메시지 서비스와 주고 받는 데이터 셋
- Publisher `게시자`: 보내는 주체 
- Subscriber `구독자`: 받는 주체
- Channel `채널`: 메시지 통신, 게시자는 `출력 채널` 구독자는 `입력 채널`
- Binder `바인더`: 메시지 에이전트` 연동`
### Partition
#### Mean 
- 메시지 서비스가 메시지를 수발신시, 애플리케이션 인스턴스가 서로 영향을 받지 않게 나누기 위함 (의역?)
#### REF
- https://docs.spring.io/spring-cloud-stream/docs/Brooklyn.RELEASE/reference/htmlsingle/#_configuring_input_bindings_for_partitioning
#### Docker Option
##### Domain Application
- SPRING_CLOUD_STREAM_BINDINGS_INPUT_CONSUMER_PARTITIONED=true
- SPRING_CLOUD_STREAM_BINDINGS_INPUT_CONSUMER_INSTANCECOUNT=2
- SPRING_CLOUD_STREAM_BINDINGS_INPUT_CONSUMER_INSTANCEINDEX=0
#### Gateway
- SPRING_CLOUD_STREAM_BINDINGS_OUTPUT-PRODUCTS_PRODUCER_PARTITION-KEY-EXPRESSION=payload.key
- SPRING_CLOUD_STREAM_BINDINGS_OUTPUT-PRODUCTS_PRODUCER_PARTITION-COUNT=2
- SPRING_CLOUD_STREAM_BINDINGS_OUTPUT-RECOMMENDATIONS_PRODUCER_PARTITION-KEY-EXPRESSION=payload.key
- SPRING_CLOUD_STREAM_BINDINGS_OUTPUT-RECOMMENDATIONS_PRODUCER_PARTITION-COUNT=2
- SPRING_CLOUD_STREAM_BINDINGS_OUTPUT-REVIEWS_PRODUCER_PARTITION-KEY-EXPRESSION=payload.key
- SPRING_CLOUD_STREAM_BINDINGS_OUTPUT-REVIEWS_PRODUCER_PARTITION-COUNT=2
