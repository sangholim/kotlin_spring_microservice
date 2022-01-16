package com.msa.domain.recommendation.rest

import com.msa.domain.recommendation.vo.Recommendation
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

interface RecommendationResource {

    @GetMapping(value = ["/recommendation"], produces = ["application/json"])
    fun gerRecommendations(@RequestParam(value = "productId", required = true) productId: Int): Flux<Recommendation>

    @PostMapping(value = ["/recommendation"], consumes = ["application/json"], produces = ["application/json"])
    fun createRecommendation(@RequestBody body: Recommendation): Recommendation

    @DeleteMapping(value = ["/recommendation"])
    fun deleteRecommendations(@RequestParam(value = "productId", required = true) productId: Int)
}