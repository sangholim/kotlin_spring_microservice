package com.msa.recommendation.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux

interface RecommendationRepository : ReactiveCrudRepository<RecommendationEntity, String> {
    @Transactional(readOnly = true)
    fun findByProductId(productId: Int): Flux<RecommendationEntity>
}