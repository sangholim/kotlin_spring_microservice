package com.msa.productComposite.api.composite.product.service

import com.msa.domain.composite.ProductAggregate
import com.msa.domain.composite.RecommendationSummary
import com.msa.domain.composite.ReviewSummary
import com.msa.domain.composite.ServiceAddresses
import com.msa.domain.product.vo.Product
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.domain.review.vo.Review
import com.msa.util.exception.InvalidInputException
import com.msa.util.exception.NotFoundException
import com.msa.util.http.ServiceUtil
import io.swagger.annotations.Api
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.just
import java.lang.RuntimeException
import java.time.Duration

@Component
@Api(description = "REST API for composite product information")
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
    val recommendationUrl = "http://$recommendationServiceHost:$recommendationServicePort/recommendation"
    val reviewUrl = "http://$reviewServiceHost:$reviewServicePort/review"

    fun integration(productId: Int): ProductAggregate {
        return Mono.zip(getProduct(productId), getReviews(productId), getRecommendations(productId)).map {
            val product = it.t1
            val reviews = it.t2
            val recommendations = it.t3
            createProductAggregate(product, recommendations, reviews)
        }.doOnError {
            val webClientResponseException = (it as WebClientResponseException)
            when (webClientResponseException.statusCode) {
                HttpStatus.NOT_FOUND -> throw NotFoundException("not found exception")
                HttpStatus.UNPROCESSABLE_ENTITY -> throw InvalidInputException("invalid input exception")
                else -> throw RuntimeException(it.localizedMessage)
            }
        }.toFuture().get() ?: ProductAggregate()
    }

    fun <T> webClientResponseHandler(response: Mono<T>): T? = try {
        response.toFuture().get()
    } catch (e: WebClientResponseException) {
        when (e.statusCode) {
            HttpStatus.BAD_REQUEST -> throw NotFoundException("bad request")
            HttpStatus.NOT_FOUND -> throw NotFoundException("not found")
            HttpStatus.UNPROCESSABLE_ENTITY -> throw InvalidInputException("unprocessable entity")
            else -> throw RuntimeException(e.localizedMessage)
        }
    }

    fun createProduct(product: Product) =
        webClientResponseHandler(
            WebClient.create(productUrl).post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(just(product), Product::class.java)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(60))
        )

    fun deleteProduct(productId: Int) =
        webClientResponseHandler(
            WebClient.create(productUrl).delete()
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(60))
        )


    fun deleteRecommendations(productId: Int) = webClientResponseHandler(
        WebClient.create(recommendationUrl)
            .delete()
            .retrieve()
            .toBodilessEntity()
            .timeout(Duration.ofSeconds(60))
    )

    fun deleteReviews(productId: Int) = webClientResponseHandler(
        WebClient.create(reviewUrl).delete()
            .retrieve()
            .toBodilessEntity()
            .timeout(Duration.ofSeconds(60))
    )

    fun createRecommendation(recommendation: Recommendation) = webClientResponseHandler(
        WebClient.create(recommendationUrl).post()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(just(recommendation), Recommendation::class.java)
            .retrieve()
            .toBodilessEntity()
            .timeout(Duration.ofSeconds(60))
    )

    fun createReview(review: Review) = webClientResponseHandler(
        WebClient.create(reviewUrl).post()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(just(review), Review::class.java)
            .retrieve()
            .toBodilessEntity()
            .timeout(Duration.ofSeconds(60))
    )

    fun getProduct(productId: Int): Mono<Product> =
        WebClient.create(productUrl + productId).get().retrieve().bodyToMono(Product::class.java)
            .timeout(Duration.ofSeconds(60))

    fun getReviews(productId: Int): Mono<List<Review>> =
        WebClient.create("$reviewUrl?productId=$productId").get().retrieve()
            .bodyToMono(object : ParameterizedTypeReference<List<Review>>() {}).timeout(Duration.ofSeconds(60))

    fun getRecommendations(productId: Int): Mono<List<Recommendation>> =
        WebClient.create("$recommendationUrl?productId=$productId").get().retrieve()
            .bodyToMono(object : ParameterizedTypeReference<List<Recommendation>>() {}).timeout(Duration.ofSeconds(60))

    fun createProductAggregate(
        product: Product,
        recommendations: List<Recommendation>,
        reviews: List<Review>
    ): ProductAggregate {
        val reviewAddress = if (reviews.isNotEmpty()) reviews[0].serviceAddress else ""
        val recommendationAddress = if (recommendations.isNotEmpty()) recommendations[0].serviceAddress else ""
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
                recommendation.content,
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