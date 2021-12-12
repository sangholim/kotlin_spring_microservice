## Gradle kts

### Settings.gradle.kts
- 멀티 프로젝트 빌드 설정
```
rootProject.name = "kotlin_spring_microservice"
include("product", "review", "recommendation", "product-composite") 
```