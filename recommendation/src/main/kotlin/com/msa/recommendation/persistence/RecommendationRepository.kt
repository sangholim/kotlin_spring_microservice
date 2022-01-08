package com.msa.recommendation.persistence

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.transaction.annotation.Transactional

interface RecommendationRepository : PagingAndSortingRepository<RecommendationEntity, String> {
    @Transactional(readOnly = true)
    fun findByProductId(productId: Int): List<RecommendationEntity>
}