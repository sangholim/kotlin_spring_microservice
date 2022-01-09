## Docker
- 애플리케이션을 신속하게 구축, 테스트 및 배포할 수 있는 소프트웨어 플랫폼
- 단일 커맨드로 컨테이너들을 통제시 `Docker Compose`
- 설정값 형식 `Yaml`

### Install
- https://sinau.tistory.com/42

### Yaml
- `build` 마이크로서비스에 사용할 Docker 파일 지정 <br>
  `Docker Compose`을 통해 이미지 빌드, 컨테이너 시작
- 도커 실행 서버 포트: 8080 <br>
  컨테이너 포트: 8080
  ```
    composite:
        build: xxx
        ports:
            - "8080:8080"
  ```

### Command
- `docker-compose up -d` 모든 컨테이너를 백그라운드로 시작 <br>
                         커맨드 실행 터미날 잠금 해제 `-d`
- `docker-compose down` 모든 컨테이너를 중지
- `docker-compose log -f --tail=0` <br>
  모든 컨테이너의 로그 메시지를 출력한다. <br>
  커맨드를 종료하지 않고 새 메시지를 기다린다. `-f`
  새 로그 메시지 확인 `--tail=0`
    
### Issue
- `docker-compose up -d` 는 기존의 이미지가 있다면 새로 빌드하지 않는거 같음
- `docker-compose up --force-recreate --build -d`: 새로 이미지 빌드
- `docker image prune -f` : 기존 이미지 제거

### DataBase CLI
#### Mongodb
- `docker-compose exec mongodb mongo --quiet`
- 질의
```sql
docker-compose exec mongodb mongo product-db --quiet --eval "db.products.find()"
docker-compose exec mongodb mongo recommendation-db --quiet --eval "db.recommendation.find()"
 
```
#### Mysql
- `docker-compose exec mysql mysql -u{username} -p ${db-name}`
- 권한 부여
```sql
GRANT ALL PRIVILEGES ON *.* TO user@'%' IDENTIFIED BY 'pwd';
FLUSH privileges;
```
- 질의
```sql
docker-compose exec mysql mysql -uuser -p review-db -e "select * from reviews"
```