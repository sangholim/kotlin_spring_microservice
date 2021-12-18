package com.msa.recommendation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.recommendation", "com.msa.util"])
class RecommendationApplication

fun main(args: Array<String>) {
	runApplication<RecommendationApplication>(*args)
}
