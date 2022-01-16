package com.msa.productComposite

import com.msa.domain.composite.ProductAggregate
import com.msa.domain.composite.RecommendationSummary
import com.msa.domain.composite.ReviewSummary
import com.msa.domain.product.vo.Product
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.domain.review.vo.Review
import com.msa.productComposite.api.composite.product.service.ProductCompositeIntegration
import com.msa.util.exception.InvalidInputException
import com.msa.util.exception.NotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.just


@ExtendWith(value = [SpringExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

    private val PRODUCT_ID_OK = 1
    private val PRODUCT_ID_NOT_FOUND = 2
    private val PRODUCT_ID_INVALID = 3

    @Autowired
    private lateinit var client: WebTestClient

    @MockBean
    private lateinit var productCompositeIntegration: ProductCompositeIntegration

    @BeforeEach
    fun setUp() {
        `when`(productCompositeIntegration.getProduct(PRODUCT_ID_OK))
            .thenReturn(just(Product(PRODUCT_ID_OK, "name", 1, "mock-address")))
        `when`(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
            .thenThrow(NotFoundException("NOT FOUND: $PRODUCT_ID_NOT_FOUND"))
        `when`(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID))
            .thenThrow(InvalidInputException("INVALID: $PRODUCT_ID_INVALID"))
        `when`(productCompositeIntegration.getReviews(PRODUCT_ID_OK))
            .thenReturn(
                Flux.fromIterable(
                    listOf(
                        Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address")
                    )
                )
            )
        `when`(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK))
            .thenReturn(
                Flux.fromIterable(
                    listOf(
                        Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")
                    )
                )
            )
    }


    @Test
    @Disabled
    fun contextLoads() {
    }

    @Test
    fun createCompositeProduct1() {
        val productAggregate = ProductAggregate(
            1, "name", 0, null, null, null
        )
        postAndVerifyProduct(productAggregate, HttpStatus.OK)
    }

    @Test
    fun createCompositeProduct2() {
        val productAggregate = ProductAggregate(
            1, "name", 1,
            listOf(
                RecommendationSummary(1, "author", "content", 1)
            ),
            listOf(
                ReviewSummary(1, "author", "subject", "content")
            ),
            null
        )
    }

    @Test
    fun deleteCompositeProduct() {
        createCompositeProduct2()
        deleteAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
    }

    @Test
    fun getProductById() {
        getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
            .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("$.recommendations.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1)
    }

    @Test
    fun getProductNotFound() {
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product-composite/$PRODUCT_ID_NOT_FOUND")
            .jsonPath("$.message").isEqualTo("NOT FOUND: $PRODUCT_ID_NOT_FOUND")
    }

    @Test
    fun getProductInvalid() {
        getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product-composite/$PRODUCT_ID_INVALID")
            .jsonPath("$.message").isEqualTo("INVALID: $PRODUCT_ID_INVALID")
    }


    private fun getAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return client.get()
            .uri("/product-composite/$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyProduct(compositeProduct: ProductAggregate, expectedStatus: HttpStatus) {
        client.post()
            .uri("/product-composite")
            .body(just(compositeProduct), ProductAggregate::class.java)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }

    private fun deleteAndVerifyProduct(productId: Int, expectedStatus: HttpStatus) {
        client.delete()
            .uri("/product-composite/$productId")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }
}