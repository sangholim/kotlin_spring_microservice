package com.msa.recommendation.rest

import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class RecommendationApiTest {
    @Test
    fun getRecommendations() {
        println(
            WebClient.builder()
                .baseUrl("http://localhost:7002/recommendation?productId=1")
                .build().get().retrieve().bodyToMono(Any::class.java).block()
        )
    }
}