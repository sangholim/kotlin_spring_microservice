package com.msa.domain.review.vo

class Review(
    var productId: Int = 0,
    var reviewId: Int = 0,
    var author: String = "",
    var subject: String = "",
    var content: String = "",
    var serviceAddress: String = ""
)