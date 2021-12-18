package com.msa.product

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.product", "com.msa.util"])
class ProductApplication

fun main(args: Array<String>) {
	runApplication<ProductApplication>(*args)
}
