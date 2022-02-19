## Spring Security
### 토큰 획득 
#### Sample
- product:read, product:write scope용 토큰
```
  url: https://writer:secret@localhost:8080/oauth/token
  parameter:
    grant_type:password
    username:magnus
    password:password
```

- product:read scope용 토큰
```
  url: https://reader:secret@localhost:8080/oauth/token
  parameter:
    grant_type:password
    username:magnus
    password:password
```

- reader client용 접근 토큰
```
  url: https://localhost:8080/oauth/authorize?response_type=token&clinet_id=reader&redirect_url=http://my.redirect.uri&scope=prodcut:read&state=48532
  브라우저에서 계정 정보 입력시 아래 입력
    username:magnus
    password:password
```

### auth0 연동 및 토큰 발급 예제는 따로 하기 