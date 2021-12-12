## Docker
- 애플리케이션을 신속하게 구축, 테스트 및 배포할 수 있는 소프트웨어 플랫폼
- 단일 커맨드로 컨테이너들을 통제시 `Docker Compose`
- 설정값 형식 `Yaml`
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
    

