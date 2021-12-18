package com.msa.domain.recommendation.rest

import com.msa.domain.recommendation.vo.Recommendation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

interface RecommendationResource {

    @GetMapping(value = ["/recommendation"], produces = ["application/json"])
    fun gerRecommendations(@RequestParam(value = "productId", required = true) productId: Int): List<Recommendation>
}