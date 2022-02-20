import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.0.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	kotlin("jvm") version "1.3.72"
	kotlin("plugin.spring") version "1.3.72"
}

repositories {
	mavenCentral()
}

dependencies {

	implementation(project(":api","default"))
	implementation(project(":utils","default"))
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.security:spring-security-oauth2-resource-server")
	implementation("org.springframework.security:spring-security-oauth2-jose")
	implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
	implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("io.springfox:springfox-boot-starter:3.0.0")
	implementation ("com.nimbusds:nimbus-jose-jwt:6.7")


	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.springframework.cloud:spring-cloud-stream-test-support")
	testImplementation("io.projectreactor:reactor-test")
}


tasks.withType<JavaCompile> {
	sourceCompatibility = "1.8"
	targetCompatibility = "1.8"
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}


dependencyManagement {
	imports {
		mavenBom ("org.springframework.cloud:spring-cloud-dependencies:Hoxton.SR6")
	}
}

tasks.test {
	maxParallelForks = 3
	setForkEvery(1)
	useJUnitPlatform()
	filter {
		includeTestsMatching("com.msa.*")
	}
}