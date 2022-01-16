package com.msa.review.service

import com.msa.domain.event.Event
import com.msa.domain.review.rest.ReviewResource
import com.msa.domain.review.vo.Review
import com.msa.util.exception.EventProcessingException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink


@EnableBinding(Sink::class)
class MessageProcessor(
    private val reviewResource: ReviewResource
) {

    @StreamListener(target = Sink.INPUT)
    fun process(event: Event<Int, Review>) {
        LOG.info("Process message created at ${event.eventCreatedAt}...")
        when (event.eventType) {
            Event.Type.CREATE -> {
                val review: Review = event.data!!
                LOG.info("Create review with ID: ${review.productId}/${review.reviewId}")
                reviewResource.createReview(review)
            }
            Event.Type.DELETE -> {
                val productId: Int = event.key
                LOG.info("Delete reviews with ProductID: {}", productId)
                reviewResource.deleteReviews(productId)
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