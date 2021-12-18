package com.msa.review.rest

import com.msa.domain.review.rest.ReviewResource
import com.msa.domain.review.vo.Review
import com.msa.util.exception.InvalidInputException
import com.msa.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController


@RestController
class ReviewResourceImpl(
    val serviceUtil: ServiceUtil
) : ReviewResource {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun getReviews(productId: Int): List<Review> {

        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        if (productId == 213) {
            log.debug("No reviews found for productId: $productId")
            return listOf()
        }

        return listOf(
            Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()),
            Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()),
            Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress())
        )
    }

}