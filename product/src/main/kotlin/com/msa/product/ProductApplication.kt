package com.msa.product

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.product", "com.msa.util"])
class ProductApplication

fun main(args: Array<String>) {
	val application = SpringApplication(ProductApplication::class.java)
	application.addListeners(ApplicationPidFileWriter())
	application.run(*args)
}
