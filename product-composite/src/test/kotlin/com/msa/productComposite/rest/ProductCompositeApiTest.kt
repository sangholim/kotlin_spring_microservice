package com.msa.productComposite.rest

import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.*

class ProductCompositeApiTest {

    @Test
    fun getProductComposite() {
        try {
            val results = mutableListOf<String>()
            listOf(
                // product, review, recommendations
                "1",
                // 404 not found
                "13",
                // empty recommendations
                "113",
                // empty reviews
                "213",
                // 422
                "-1"
            ).forEach {
                WebClient.builder()
                    .baseUrl("http://localhost:7000/product-composite/${it}")
                    .build().get()
                    .exchange()
                    .flatMap { response: ClientResponse -> response.bodyToMono<Map<*, *>>() }
                    .doOnSuccess { map -> results.add(map.toString()) }
                    .block()
            }

            results.forEach {
                println("result > $it")
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}