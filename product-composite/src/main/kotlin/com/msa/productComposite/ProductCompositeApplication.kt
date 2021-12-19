package com.msa.productComposite

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.productComposite", "com.msa.util"])
class ProductCompositeApplication

fun main(args: Array<String>) {
	val application = SpringApplication(ProductCompositeApplication::class.java)
	application.addListeners(ApplicationPidFileWriter())
	application.run(*args)
}
