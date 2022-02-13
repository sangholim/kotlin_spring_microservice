package com.msa.gateway

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = ["eureka.client.enabled=false"])
@Disabled
class GatewayApplicationTests {

	@Test
	fun contextLoads() {
	}

}
