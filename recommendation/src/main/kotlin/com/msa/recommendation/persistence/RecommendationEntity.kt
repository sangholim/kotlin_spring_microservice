package com.msa.recommendation.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "recommendations")
// 복합 인덱스 정의
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId': 1, 'recommendationId': 1}")
class RecommendationEntity {

    @Id
    lateinit var id: String

    @Version
    var version: Int = 0

    var productId: Int = 0
    var recommendationId: Int = 0
    lateinit var author: String
    var rating: Int = 0
    lateinit var content: String
}