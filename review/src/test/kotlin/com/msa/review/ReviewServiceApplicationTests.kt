package com.msa.review

import com.msa.domain.review.vo.Review
import com.msa.review.persistence.ReviewRepository
import io.netty.handler.codec.http.HttpResponseStatus.UNPROCESSABLE_ENTITY
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Mono.just


@ExtendWith(value = [SpringExtension::class])
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.datasource.url=jdbc:h2:mem:review-db"]
)
class ReviewServiceApplicationTests {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ReviewRepository

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()
    }

    @Test
    fun getReviewsByProductId() {
        val productId = 1
        assertEquals(0, repository.findByProductId(productId).size)
        postAndVerifyReview(productId, 1, HttpStatus.OK)
        postAndVerifyReview(productId, 2, HttpStatus.OK)
        postAndVerifyReview(productId, 3, HttpStatus.OK)
        assertEquals(3, repository.findByProductId(productId).size)

        getAndVerifyReviewsByProductId(productId, HttpStatus.OK)
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[2].productId").isEqualTo(productId)
            .jsonPath("$[2].reviewId").isEqualTo(3)
    }

    @Test
    fun duplicateError() {
        val productId = 1
        val reviewId = 1
        assertEquals(0, repository.count())
        postAndVerifyReview(productId, reviewId, HttpStatus.OK)
            .jsonPath("$.productId").isEqualTo(productId)
            .jsonPath("$.reviewId").isEqualTo(reviewId)
        assertEquals(1, repository.count())
        postAndVerifyReview(productId, reviewId, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id:1")
        assertEquals(1, repository.count())
    }

    @Test
    fun deleteReviews() {
        val productId = 1
        val recommendationId = 1
        postAndVerifyReview(productId, recommendationId, HttpStatus.OK)
        assertEquals(1, repository.findByProductId(productId).size)
        deleteAndVerifyReviewsByProductId(productId, HttpStatus.OK)
        assertEquals(0, repository.findByProductId(productId).size)
        deleteAndVerifyReviewsByProductId(productId, HttpStatus.OK)
    }

    @Test
    fun getReviewsMissingParameter() {
        getAndVerifyReviewsByProductId("", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("")
            //.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present")
    }

    @Test
    fun getReviewsInvalidParameter() {
        getAndVerifyReviewsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("")
            //.jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getReviewsNotFound() {
        getAndVerifyReviewsByProductId("?productId=213", HttpStatus.OK)
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun getReviewsInvalidParameterNegativeValue() {
        val productIdInvalid = -1
        getAndVerifyReviewsByProductId("?productId=$productIdInvalid", HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }


    private fun getAndVerifyReviewsByProductId(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return getAndVerifyReviewsByProductId("?productId=$productId", expectedStatus)
    }

    private fun getAndVerifyReviewsByProductId(
        productIdQuery: String,
        expectedStatus: HttpStatus
    ): BodyContentSpec {
        return client.get()
            .uri("/review$productIdQuery")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyReview(productId: Int, reviewId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        val review = Review(
            productId, reviewId,
            "Author $reviewId", "Subject $reviewId", "Content $reviewId", "SA"
        )
        return client.post()
            .uri("/review")
            .body(just(review), Review::class.java)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun deleteAndVerifyReviewsByProductId(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return client.delete()
            .uri("/review?productId=$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()
    }
}