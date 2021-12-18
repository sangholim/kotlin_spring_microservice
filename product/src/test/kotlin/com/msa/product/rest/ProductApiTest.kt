package com.msa.product.rest

import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class ProductApiTest {

    @Test
    fun getProduct() {
        println(
            WebClient.builder().baseUrl("http://localhost:7001/product/1")
                .build().get().retrieve().bodyToMono(Any::class.java).block()
        )

    }


}