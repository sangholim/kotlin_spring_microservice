package com.msa.product.service

import com.msa.domain.event.Event
import com.msa.domain.product.rest.ProductResource
import com.msa.domain.product.vo.Product
import com.msa.util.exception.EventProcessingException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink

@EnableBinding(Sink::class)
class MessageProcessor(
    private val productResource: ProductResource) {

    @StreamListener(target = Sink.INPUT)
    fun process(event: Event<Int, Product>) {
        LOG.info("Process message created at ${event.eventCreatedAt}...")
        when (event.eventType) {
            Event.Type.CREATE -> {
                val product: Product = event.data!!
                LOG.info("Create product with ID: {}", product.productId)
                productResource.createProduct(product)
            }
            Event.Type.DELETE -> {
                val productId: Int = event.key
                LOG.info("Delete recommendations with ProductID: $productId")
                productResource.deleteProduct(productId)
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