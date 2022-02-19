package com.msa.productComposite.api.composite.product.config

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod.*
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders
import org.springframework.security.web.server.SecurityWebFilterChain


@EnableWebFluxSecurity
class SecurityConfig {
	@Bean
	fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
		http
			.authorizeExchange()
			.pathMatchers("/actuator/**").permitAll()
			.pathMatchers(POST, "/product-composite/**").hasAuthority("SCOPE_product:write")
			.pathMatchers(DELETE, "/product-composite/**").hasAuthority("SCOPE_product:write")
			.pathMatchers(GET, "/product-composite/**").hasAuthority("SCOPE_product:read")
			.anyExchange().authenticated()
			.and()
			.oauth2ResourceServer()
			.jwt()
		return http.build()
	}

}