package com.msa.recommendation.service

import com.msa.domain.event.Event
import com.msa.domain.recommendation.rest.RecommendationResource
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.util.exception.EventProcessingException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink


@EnableBinding(Sink::class)
class MessageProcessor(
    private val recommendationResource: RecommendationResource
) {

    @StreamListener(target = Sink.INPUT)
    fun process(event: Event<Int, Recommendation>) {
        LOG.info("Process message created at ${event.eventCreatedAt}...")
        when (event.eventType) {
            Event.Type.CREATE -> {
                val recommendation: Recommendation = event.data!!
                LOG.info("Create recommendation with ID: ${recommendation.productId}/${recommendation.recommendationId}")
                recommendationResource.createRecommendation(recommendation)
            }
            Event.Type.DELETE -> {
                val productId: Int = event.key
                LOG.info("Delete recommendations with ProductID: {}", productId)
                recommendationResource.deleteRecommendations(productId)
            }
            else -> {
                val errorMessage =
                    "Incorrect event type: ${event.eventType}, expected a CREATE or DELETE event"
                LOG.warn(errorMessage)
                throw EventProcessingException(errorMessage)
            }
        }
        LOG.info("Message processing done!")
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(MessageProcessor::class.java)
    }
}