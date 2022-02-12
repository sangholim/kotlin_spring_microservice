package com.msa.productComposite.api.composite.product.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.msa.domain.event.Event
import com.msa.domain.product.vo.Product
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.domain.review.vo.Review
import com.msa.util.exception.InvalidInputException
import com.msa.util.exception.NotFoundException
import com.msa.util.http.HttpErrorInfo
import com.msa.util.http.ServiceUtil
import io.swagger.annotations.Api
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.cloud.stream.annotation.Output
import org.springframework.http.HttpStatus
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.empty
import java.io.IOException
import java.time.Duration

@Component
@Api(description = "REST API for composite product information")
class ProductCompositeIntegration(
    private val messageSources: MessageSources,
    private val webClientBuilder: WebClient.Builder
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val objectMapper = jacksonObjectMapper()
    val productUrl = "http://product"
    val recommendationUrl = "http://recommendation"
    val reviewUrl = "http://review"
    private val webClient = webClientBuilder.build()
    fun getProduct(productId: Int): Mono<Product> =
        webClient.get().uri("$productUrl/product/$productId")
        .retrieve().bodyToMono(Product::class.java)
            .timeout(Duration.ofSeconds(60))
            .onErrorMap(WebClientResponseException::class.java) { ex -> handleException(ex) }

    fun createProduct(product: Product): Product {
        messageSources.outputProducts()
            .send(MessageBuilder.withPayload(Event(Event.Type.CREATE, product.productId, product)).build())
        return product
    }

    fun deleteProduct(productId: Int) {
        messageSources.outputProducts()
            .send(MessageBuilder.withPayload(Event(Event.Type.DELETE, productId, null)).build())
    }

    fun getRecommendations(productId: Int): Flux<Recommendation> =
        webClient.get().uri("$recommendationUrl/recommendation?productId=$productId")
            .retrieve()
            .bodyToFlux(Recommendation::class.java).timeout(Duration.ofSeconds(60))
            .onErrorResume { empty() }


    fun createRecommendation(recommendation: Recommendation): Recommendation {
        messageSources.outputRecommendations()
            .send(
                MessageBuilder.withPayload(Event(Event.Type.CREATE, recommendation.productId, recommendation)).build()
            )
        return recommendation
    }

    fun deleteRecommendations(productId: Int) {
        messageSources.outputRecommendations()
            .send(MessageBuilder.withPayload(Event(Event.Type.DELETE, productId, null)).build())
    }

    fun getReviews(productId: Int): Flux<Review> =
        webClient.get().uri("$reviewUrl/review?productId=$productId")
            .retrieve()
            .bodyToFlux(Review::class.java)
            .timeout(Duration.ofSeconds(60))
            .onErrorResume { empty() }

    fun deleteReviews(productId: Int) {
        messageSources.outputReviews()
            .send(MessageBuilder.withPayload(Event(Event.Type.DELETE, productId, null)).build())
    }

    fun createReview(review: Review): Review {
        messageSources.outputReviews()
            .send(
                MessageBuilder.withPayload(Event(Event.Type.CREATE, review.productId, review)).build()
            )
        return review
    }

    fun getProductHealth(): Mono<Health> {
        return getHealth(productUrl)
    }

    fun getRecommendationHealth(): Mono<Health> {
        return getHealth(recommendationUrl)
    }

    fun getReviewHealth(): Mono<Health> {
        return getHealth(reviewUrl)
    }

    private fun getHealth(url: String): Mono<Health> {
        log.debug("Will call the Health API on URL: {}", url)
        return webClient.get().uri("$url/actuator/health").retrieve().bodyToMono(String::class.java)
            .map { s -> Health.Builder().up().build() }
            .onErrorResume { ex -> Mono.just(Health.Builder().down(ex).build()) }
    }


    private fun handleException(ex: Throwable): Throwable {
        if (ex !is WebClientResponseException) {
            log.warn("Got a unexpected error: ${ex.message}, will rethrow it")
            return ex
        }
        val wcre = ex
        return when (wcre.statusCode) {
            HttpStatus.NOT_FOUND -> NotFoundException(getErrorMessage(wcre))
            HttpStatus.UNPROCESSABLE_ENTITY -> InvalidInputException(getErrorMessage(wcre))
            else -> {
                log.warn("Got a unexpected HTTP error: ${wcre.statusCode}, will rethrow it")
                log.warn("Error body: ${wcre.responseBodyAsString}")
                ex
            }
        }
    }

    private fun getErrorMessage(ex: WebClientResponseException): String {
        return try {
            objectMapper.readValue(ex.responseBodyAsString, HttpErrorInfo::class.java).message
        } catch (ioex: IOException) {
            ex.message!!
        }
    }

    interface MessageSources {
        @Output(OUTPUT_PRODUCTS)
        fun outputProducts(): MessageChannel

        @Output(OUTPUT_RECOMMENDATIONS)
        fun outputRecommendations(): MessageChannel

        @Output(OUTPUT_REVIEWS)
        fun outputReviews(): MessageChannel

        companion object {
            const val OUTPUT_PRODUCTS = "output-products"
            const val OUTPUT_RECOMMENDATIONS = "output-recommendations"
            const val OUTPUT_REVIEWS = "output-reviews"
        }
    }
}

