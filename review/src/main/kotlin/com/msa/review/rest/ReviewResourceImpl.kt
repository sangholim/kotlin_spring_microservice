package com.msa.review.rest

import com.msa.domain.review.rest.ReviewResource
import com.msa.domain.review.vo.Review
import com.msa.review.persistence.ReviewRepository
import com.msa.util.exception.InvalidInputException
import com.msa.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController


@RestController
class ReviewResourceImpl(
    val serviceUtil: ServiceUtil,
    val reviewMapper: ReviewMapper,
    val reviewRepository: ReviewRepository
) : ReviewResource {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun getReviews(productId: Int): List<Review> {

        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        if (productId == 213) {
            log.debug("No reviews found for productId: $productId")
            return listOf()
        }

        val serviceAddress = serviceUtil.getServiceAddress()
        val entityList = reviewRepository.findByProductId(productId)

        return reviewMapper.entityListToApiList(entityList).map {
            it.serviceAddress = serviceAddress
            it
        }
    }

    override fun createReview(body: Review): Review = try {
        val entity = reviewMapper.apiToEntity(body)
        val newEntity = reviewRepository.save(entity)
        val newBody = reviewMapper.entityToApi(newEntity)
        newBody.serviceAddress = serviceUtil.getServiceAddress()
        newBody
    } catch (dive: DataIntegrityViolationException) {
        throw InvalidInputException("Duplicate key, Product Id: ${body.productId}, Review Id:${body.reviewId}");
    }

    override fun deleteReviews(productId: Int) =
        reviewRepository.deleteAll(reviewRepository.findByProductId(productId))

}