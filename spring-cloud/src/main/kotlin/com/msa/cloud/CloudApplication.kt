package com.msa.cloud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@EnableEurekaServer
@SpringBootApplication
class CloudApplication

fun main(args: Array<String>) {
	runApplication<CloudApplication>(*args)
}
