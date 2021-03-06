package com.msa.review.rest

import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class ReviewApiTest {

    @Test
    fun getReviews() {
        try {
            println(
                WebClient.builder()
                    .baseUrl("http://localhost:7003/review?productId=1")
                    .build().get().retrieve().bodyToMono(Any::class.java).block()
            )
        } catch (e: Exception) {
            println(
                "fail get reviews > ${e.message}"
            )
        }
    }
}