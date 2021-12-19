package com.msa.productComposite.api.composite.product.service

import com.msa.domain.composite.ProductAggregate
import com.msa.domain.composite.RecommendationSummary
import com.msa.domain.composite.ReviewSummary
import com.msa.domain.composite.ServiceAddresses
import com.msa.domain.product.vo.Product
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.domain.review.vo.Review
import com.msa.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers


@Component
class ProductCompositeIntegration(
    @Value("\${app.product-service.host}") productServiceHost: String = "",
    @Value("\${app.product-service.port}") productServicePort: Int = 0,
    @Value("\${app.recommendation-service.host}") recommendationServiceHost: String = "",
    @Value("\${app.recommendation-service.port}") recommendationServicePort: Int = 0,
    @Value("\${app.review-service.host}") reviewServiceHost: String = "",
    @Value("\${app.review-service.port}") reviewServicePort: Int = 0,
    val serviceUtil: ServiceUtil
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    val productUrl = "http://$productServiceHost:$productServicePort/product/"
    val recommendationUrl = "http://$recommendationServiceHost:$recommendationServicePort/recommendation?productId="
    val reviewUrl = "http://$reviewServiceHost:$reviewServicePort/review?productId="

    fun integration(productId: Int): ProductAggregate {
        return Mono.zip(
            getProduct(productId).subscribeOn(Schedulers.elastic()),
            getReviews(productId).subscribeOn(Schedulers.elastic()),
            getRecommendations(productId).subscribeOn(Schedulers.elastic()))
            .map {
                val product = it.t1
                val reviews = it.t2
                val recommendations = it.t3
                createProductAggregate(product, recommendations, reviews)
            }.onErrorResume(WebClientResponseException::class.java) {
                Mono.error(ResponseStatusException(it.statusCode, it.message))
            }.toFuture().get() ?: ProductAggregate()
    }

    fun getProduct(productId: Int) =
        WebClient.create(productUrl + productId)
            .get()
            .retrieve()
            .bodyToMono<Product>()
            .onErrorResume(WebClientResponseException::class.java) {
                Mono.error(ResponseStatusException(it.statusCode, it.message))
            }

    fun getReviews(productId: Int): Mono<List<Review>> =
        WebClient.create(reviewUrl + productId).get()
            .retrieve().bodyToMono(object : ParameterizedTypeReference<List<Review>>() {})
            .onErrorResume(WebClientResponseException::class.java) {
                Mono.error(ResponseStatusException(it.statusCode, it.message))
            }

    fun getRecommendations(productId: Int): Mono<List<Recommendation>> =
        WebClient.create(recommendationUrl + productId).get()
            .retrieve().bodyToMono(object : ParameterizedTypeReference<List<Recommendation>>() {})
            .onErrorResume(WebClientResponseException::class.java) {
                Mono.error(ResponseStatusException(it.statusCode, it.message))
            }

    fun createProductAggregate(
        product: Product,
        recommendations: List<Recommendation>,
        reviews: List<Review>
    ): ProductAggregate {
        val reviewAddress = reviews[0].serviceAddress
        val recommendationAddress = recommendations[0].serviceAddress
        val serviceAddresses = ServiceAddresses(
            serviceUtil.getServiceAddress(),
            product.serviceAddress,
            reviewAddress,
            recommendationAddress
        )
        val reviewSummaries =
            reviews.map { review -> ReviewSummary(review.reviewId, review.author, review.subject) }.toList()
        val recommendationSummaries = recommendations.map { recommendation ->
            RecommendationSummary(
                recommendation.recommendationId,
                recommendation.author,
                recommendation.rate
            )
        }

        return ProductAggregate(
            product.productId,
            product.name,
            product.weight,
            recommendationSummaries,
            reviewSummaries,
            serviceAddresses
        )
    }
}