package com.msa.recommendation

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.recommendation", "com.msa.util"])
class RecommendationApplication

fun main(args: Array<String>) {
	val application = SpringApplication(RecommendationApplication::class.java)
	application.addListeners(ApplicationPidFileWriter())
	application.run(*args)
}
