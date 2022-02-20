package com.msa.configServer

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http
            // Disable CRCF to allow POST to /encrypt and /decrypt endpoins
            .csrf()
            .disable()
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .httpBasic()
    }
}