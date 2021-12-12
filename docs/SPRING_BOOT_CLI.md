## Spring Boot CLI
- 버전 `2.3.0` 2021-12-12 기준으로 minimum
- https://repo.spring.io/ui/native/release/org/springframework/boot/spring-boot-cli/2.1.0.RELEASE/spring-boot-cli-2.1.0.RELEASE-bin.zip
- 압축 파일 해제
- 환경변수 `Path` 추가 ${압축파일 폴더 절대 경로}/bin
- Spring 깡통 커맨드 라인 (Power-Shell)
- 상품 서비스 깡통 `product`
`spring init  --boot-version=2.3.0 --build=gradle --java-version=11 --packaging=jar --language=kotlin --name=product --package-name=com.msa.product --groupId=com.msa.product --dependencies=actuator,webflux --version=1.0.0-SNAPSHOT product`
  
- 추천 서비스 깡통 `recommendation`
`spring init  --boot-version=2.3.0 --build=gradle --java-version=11 --packaging=jar --language=kotlin --name=recommendation --package-name=com.msa.recommendation --groupId=com.msa.recommendation --dependencies=actuator,webflux --version=1.0.0-SNAPSHOT recommendation`

- 리뷰 서비스 깡통 `review`
`spring init  --boot-version=2.3.0 --build=gradle --java-version=11 --packaging=jar --language=kotlin --name=review --package-name=com.msa.review --groupId=com.msa.review --dependencies=actuator,webflux --version=1.0.0-SNAPSHOT review`

- 상품 - 추천 - 리뷰 병합 서비스 깡통 `product-composite`
  `spring init  --boot-version=2.3.0 --build=gradle --java-version=11 --packaging=jar --language=kotlin --name=product-composite --package-name=com.msa.product-composite --groupId=com.msa.product-composite --dependencies=actuator,webflux --version=1.0.0-SNAPSHOT product-composite`
  

