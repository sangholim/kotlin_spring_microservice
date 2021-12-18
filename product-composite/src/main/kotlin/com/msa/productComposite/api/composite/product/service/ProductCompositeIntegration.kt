package com.msa.productComposite.api.composite.product.service

import com.msa.domain.composite.ProductAggregate
import com.msa.domain.composite.RecommendationSummary
import com.msa.domain.composite.ReviewSummary
import com.msa.domain.composite.ServiceAddresses
import com.msa.domain.product.vo.Product
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.domain.review.vo.Review
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class ProductCompositeIntegration(
    @Value("\${app.product-service.host}") productServiceHost: String = "",
    @Value("\${app.product-service.port}") productServicePort: Int = 0,
    @Value("\${app.recommendation-service.host}") recommendationServiceHost: String = "",
    @Value("\${app.recommendation-service.port}") recommendationServicePort: Int = 0,
    @Value("\${app.review-service.host}") reviewServiceHost: String = "",
    @Value("\${app.review-service.port}") reviewServicePort: Int = 0
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    val productUrl = "http://$productServiceHost:$productServicePort/product/"
    val recommendationUrl = "http://$recommendationServiceHost:$recommendationServicePort/recommendation?productId="
    val reviewUrl = "http://$reviewServiceHost:$reviewServicePort/review?productId="

    fun integration(productId: Int): ProductAggregate {
        return Mono.zip(getProduct(productId), getReviews(productId), getRecommendations(productId)).map {
            val product = it.t1
            val reviews = it.t2.map { review -> ReviewSummary(review.reviewId, review.author, review.subject) }.toList()
            val recommendations = it.t3.map { recommendation ->
                RecommendationSummary(
                    recommendation.recommendationId,
                    recommendation.author,
                    recommendation.rate
                )
            }
            ProductAggregate(productId, product.name, product.weight, recommendations, reviews, ServiceAddresses())
        }.toFuture().get() ?: ProductAggregate()
    }

    fun getProduct(productId: Int): Mono<Product> =
        WebClient.create(productUrl + productId).get().retrieve().bodyToMono(Product::class.java)

    fun getReviews(productId: Int): Mono<List<Review>> =
        WebClient.create(reviewUrl + productId).get().retrieve()
            .bodyToMono(object : ParameterizedTypeReference<List<Review>>() {})

    fun getRecommendations(productId: Int): Mono<List<Recommendation>> =
        WebClient.create(recommendationUrl + productId).get().retrieve()
            .bodyToMono(object : ParameterizedTypeReference<List<Recommendation>>() {})

}