package com.msa.review

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.review", "com.msa.util"])
class ReviewApplication

fun main(args: Array<String>) {
	runApplication<ReviewApplication>(*args)
}
