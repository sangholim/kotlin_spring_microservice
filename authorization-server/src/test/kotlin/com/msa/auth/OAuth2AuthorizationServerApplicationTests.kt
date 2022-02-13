package com.msa.auth

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = ["eureka.client.enabled=false"])
@AutoConfigureMockMvc
class OAuth2AuthorizationServerApplicationTests {

	@Autowired
	private lateinit var mvc: MockMvc

	@Test
	@Throws(Exception::class)
	fun requestTokenWhenUsingPasswordGrantTypeThenOk() {
		mvc.perform(
			post("/oauth/token")
				.param("grant_type", "password")
				.param("username", "magnus")
				.param("password", "password")
				.header("Authorization", "Basic cmVhZGVyOnNlY3JldA==")
		)
			.andExpect(status().isOk())
	}

	@Test
	@Throws(Exception::class)
	fun requestJwkSetWhenUsingDefaultsThenOk() {
		mvc.perform(get("/.well-known/jwks.json"))
			.andExpect(status().isOk())
	}
}
