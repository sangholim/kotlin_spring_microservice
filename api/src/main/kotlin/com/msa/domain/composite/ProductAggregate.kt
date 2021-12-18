package com.msa.domain.composite

class ProductAggregate(
    var productId: Int = 0,
    var name: String = "",
    var weight: Int = 0,
    var recommendations: List<RecommendationSummary> = listOf(),
    var reviews: List<ReviewSummary> = listOf(),
    var serviceAddresses: ServiceAddresses = ServiceAddresses()
)

class ServiceAddresses(
    var cmp: String = "",
    var pro: String = "",
    var rev: String = "",
    var rec: String = ""
)

class ReviewSummary(
    var reviewId: Int = 0,
    var author: String = "",
    var subject: String = ""
)


class RecommendationSummary(
    var recommendationId: Int = 0,
    var author: String = "",
    var rate: Int = 0
)