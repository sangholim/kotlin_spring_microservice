package com.msa.recommendation.rest

import com.msa.domain.recommendation.rest.RecommendationResource
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.util.exception.InvalidInputException
import com.msa.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController


@RestController
class RecommendationResourceImpl(val serviceUtil: ServiceUtil): RecommendationResource {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun gerRecommendations(productId: Int): List<Recommendation> {

        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        if (productId == 113) {
            log.debug("No recommendations found for productId: {}", productId)
            return listOf()
        }

        return listOf(
            Recommendation(productId,  1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()),
            Recommendation(productId,  2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress())
            )
    }

}