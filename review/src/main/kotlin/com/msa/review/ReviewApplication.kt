package com.msa.review

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.review", "com.msa.util"])
class ReviewApplication

fun main(args: Array<String>) {
	val application = SpringApplication(ReviewApplication::class.java)
	application.addListeners(ApplicationPidFileWriter())
	application.run(*args)
}
