package com.msa.review.persistence

import javax.persistence.*

@Entity
@Table(
    name = "reviews",
    indexes = [Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId")]
)
class ReviewEntity {

    @Id
    @GeneratedValue
    var id: Int? = null

    @Version
    var version: Int = 0

    var productId: Int = 0
    var reviewId: Int = 0
    lateinit var author: String
    lateinit var subject: String
    lateinit var content: String

}