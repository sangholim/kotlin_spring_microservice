package com.msa.recommendation.rest

import com.msa.domain.recommendation.rest.RecommendationResource
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.recommendation.persistence.RecommendationRepository
import com.msa.util.exception.InvalidInputException
import com.msa.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController


@RestController
class RecommendationResourceImpl(
    val serviceUtil: ServiceUtil,
    val recommendationRepository: RecommendationRepository,
    val recommendationMapper: RecommendationMapper
) : RecommendationResource {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun gerRecommendations(productId: Int): List<Recommendation> {

        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        if (productId == 113) {
            log.debug("No recommendations found for productId: {}", productId)
            return listOf()
        }
        val serviceAddress = serviceUtil.getServiceAddress()

        val entityList = recommendationRepository.findByProductId(productId)
        return recommendationMapper.entityListToApiList(entityList).map {
            it.serviceAddress = serviceAddress
            it
        }
    }

    override fun createRecommendation(body: Recommendation): Recommendation = try {
        val entity = recommendationMapper.apiToEntity(body)
        val newEntity = recommendationRepository.save(entity)
        recommendationMapper.entityToApi(newEntity)
    } catch (dke: DuplicateKeyException) {
        throw InvalidInputException("Duplicate key, Product Id: ${body.productId} Recommendation Id: ${body.recommendationId}")
    }

    override fun deleteRecommendations(productId: Int) =
        recommendationRepository.deleteAll(recommendationRepository.findByProductId(productId))
}