package com.msa.recommendation.rest

import com.msa.domain.recommendation.rest.RecommendationResource
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.recommendation.persistence.RecommendationRepository
import com.msa.util.exception.InvalidInputException
import com.msa.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux


@RestController
class RecommendationResourceImpl(
    val serviceUtil: ServiceUtil,
    val recommendationRepository: RecommendationRepository,
    val recommendationMapper: RecommendationMapper
) : RecommendationResource {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun gerRecommendations(productId: Int): Flux<Recommendation> {

        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        val serviceAddress = serviceUtil.getServiceAddress()
        return recommendationRepository.findByProductId(productId)
            .map {
                recommendationMapper.entityToApi(it).apply {
                    this.serviceAddress = serviceAddress
                }
            }
    }

    override fun createRecommendation(body: Recommendation): Recommendation {
        val entity = recommendationMapper.apiToEntity(body)
        return recommendationRepository.save(entity)
            .onErrorMap(DuplicateKeyException::class.java) {
                InvalidInputException("Duplicate key, Product Id: ${body.productId} Recommendation Id: ${body.recommendationId}")
            }.map { recommendationMapper.entityToApi(it) }.block() ?: Recommendation()

    }


    override fun deleteRecommendations(productId: Int) {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
        recommendationRepository.deleteAll(recommendationRepository.findByProductId(productId)).block()
    }
}