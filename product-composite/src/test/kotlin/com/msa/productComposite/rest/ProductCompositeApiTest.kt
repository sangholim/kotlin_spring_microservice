package com.msa.productComposite.rest

import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class ProductCompositeApiTest {

    @Test
    fun getProductComposite() {
        println(
            WebClient.builder()
                .baseUrl("http://localhost:7000/product-composite/1")
                .build().get().retrieve().bodyToMono(Any::class.java).block()
        )
    }
}