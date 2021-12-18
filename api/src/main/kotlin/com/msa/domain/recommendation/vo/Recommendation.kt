package com.msa.domain.recommendation.vo

class Recommendation(
    var productId: Int = 0,
    var recommendationId: Int = 0,
    var author: String = "",
    var rate: Int = 0,
    var content: String = "",
    var serviceAddress: String = ""
)