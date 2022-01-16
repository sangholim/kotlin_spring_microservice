package com.msa.productComposite.api.composite.product.rest

import com.msa.domain.composite.ProductAggregate
import com.msa.domain.composite.RecommendationSummary
import com.msa.domain.composite.ReviewSummary
import com.msa.domain.composite.ServiceAddresses
import com.msa.domain.product.vo.Product
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.domain.review.vo.Review
import com.msa.productComposite.api.composite.product.service.ProductCompositeIntegration
import com.msa.util.http.ServiceUtil
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
class ProductCompositeResource(
    val productCompositeIntegration: ProductCompositeIntegration,
    val serviceUtil: ServiceUtil
) {


    @ApiOperation(
        value = "\${api.product-composite.get-composite-product.description}",
        notes = "\${api.product-composite.get-composite-product.notes}"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                code = 400, message = "Bad Request, invalid format of request. " +
                        "See response message for more information."
            ),
            ApiResponse(
                code = 404, message = "Not found, the specified id does not exist."
            ),
            ApiResponse(
                code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. " +
                        "See response message for more information."
            )
        ]
    )
    @GetMapping(value = ["/product-composite/{productId}"])
    fun getCompositeProduct(@PathVariable productId: Int) =
        integration(productId)


    @ApiOperation(
        value = "\${api.product-composite.create-composite-product.description}",
        notes = "\${api.product-composite.create-composite-product.notes}"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                code = 400, message = "Bad Request, invalid format of request. " +
                        "See response message for more information."
            ),
            ApiResponse(
                code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. " +
                        "See response message for more information."
            )
        ]
    )
    @PostMapping(value = ["/product-composite"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createCompositeProduct(@RequestBody body: ProductAggregate) {
        val product = Product(body.productId, body.name, body.weight, "")
        productCompositeIntegration.createProduct(product)

        val recommendations = body.recommendations ?: listOf()
        if (recommendations.isNotEmpty())
            recommendations.forEach {
                val recommendation =
                    Recommendation(product.productId, it.recommendationId, it.author, it.rate, it.content, "")
                productCompositeIntegration.createRecommendation(recommendation)
            }
        val reviews = body.reviews ?: listOf()
        if (reviews.isNotEmpty())
            reviews.forEach {
                val review = Review(product.productId, it.reviewId, it.author, it.subject, it.content, "")
                productCompositeIntegration.createReview(review)
            }
    }

    @ApiOperation(
        value = "\${api.product-composite.delete-composite-product.description}",
        notes = "\${api.product-composite.delete-composite-product.notes}"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                code = 400, message = "Bad Request, invalid format of request. " +
                        "See response message for more information."
            ),
            ApiResponse(
                code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. " +
                        "See response message for more information."
            )
        ]
    )
    @DeleteMapping(value = ["/product-composite/{productId}"])
    fun deleteCompositeProduct(@PathVariable productId: Int) {
        productCompositeIntegration.deleteProduct(productId)
        productCompositeIntegration.deleteRecommendations(productId)
        productCompositeIntegration.deleteReviews(productId)
    }


    private fun integration(productId: Int): Mono<ProductAggregate> {
        return Mono.zip(
            productCompositeIntegration.getProduct(productId),
            productCompositeIntegration.getRecommendations(productId).collectList(),
            productCompositeIntegration.getReviews(productId).collectList()
        ).map {
            createProductAggregate(it.t1, it.t2, it.t3, serviceUtil.getServiceAddress())
        }.doOnError { ex ->
            ex.message
        }
    }

    private fun createProductAggregate(
        product: Product,
        recommendations: List<Recommendation>,
        reviews: List<Review>,
        serviceAddress: String
    ): ProductAggregate {

        // 1. Setup product info
        val productId: Int = product.productId
        val name: String = product.name
        val weight: Int = product.weight

        // 2. Copy summary recommendation info, if available
        val recommendationSummaries =
            recommendations.map { r -> RecommendationSummary(r.recommendationId, r.author, r.content, r.rate) }


        // 3. Copy summary review info, if available
        val reviewSummaries = reviews
            .map { r -> ReviewSummary(r.reviewId, r.author, r.subject, r.content) }

        // 4. Create info regarding the involved microservices addresses
        val productAddress: String = product.serviceAddress
        val reviewAddress = if (reviews.isNotEmpty()) reviews[0].serviceAddress else ""
        val recommendationAddress = if (recommendations.isNotEmpty()) recommendations[0].serviceAddress else ""
        val serviceAddresses = ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress)
        return ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses)

    }

}