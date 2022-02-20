package com.msa.cloud

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = ["eureka.client.enabled=false", "spring.cloud.config.enabled=false"])
@Disabled
class CloudApplicationTests {

	@Test
	fun contextLoads() {
	}

}
