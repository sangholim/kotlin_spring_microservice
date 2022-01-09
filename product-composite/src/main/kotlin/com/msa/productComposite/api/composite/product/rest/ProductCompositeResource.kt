package com.msa.productComposite.api.composite.product.rest

import com.msa.domain.composite.ProductAggregate
import com.msa.domain.product.vo.Product
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.domain.review.vo.Review
import com.msa.productComposite.api.composite.product.service.ProductCompositeIntegration
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
class ProductCompositeResource(val productCompositeIntegration: ProductCompositeIntegration) {

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
    fun getCompositeProduct(@PathVariable productId: Int) = productCompositeIntegration.integration(productId)

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

        val recommendations = body.recommendations?: listOf()
        if (recommendations.isNotEmpty())
            recommendations.forEach {
                val recommendation =
                    Recommendation(product.productId, it.recommendationId, it.author, it.rate, it.content, "")
                productCompositeIntegration.createRecommendation(recommendation)
            }
        val reviews = body.reviews?: listOf()
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
}