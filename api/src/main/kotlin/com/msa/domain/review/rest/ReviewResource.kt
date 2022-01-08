package com.msa.domain.review.rest

import com.msa.domain.review.vo.Review
import org.springframework.web.bind.annotation.*


interface ReviewResource {
    @GetMapping(value = ["/review"], produces = ["application/json"])
    fun getReviews(@RequestParam(value = "productId", required = true) productId: Int): List<Review>

    @PostMapping(value = ["/review"], consumes = ["application/json"], produces = ["application/json"])
    fun createReview(@RequestBody body: Review): Review

    @DeleteMapping(value = ["/review"])
    fun deleteReviews(@RequestParam(value = "productId", required = true) productId: Int)
}