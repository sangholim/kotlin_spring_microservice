package com.msa.review

import com.msa.domain.event.Event
import com.msa.domain.review.vo.Review
import com.msa.review.persistence.ReviewRepository
import com.msa.util.exception.InvalidInputException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.messaging.MessagingException
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.support.GenericMessage
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

    @Autowired
    private lateinit var channels: Sink

    private lateinit var input: SubscribableChannel

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()
        input = channels.input() as SubscribableChannel
    }

    @Test
    fun getReviewsByProductId() {
        val productId = 1
        assertEquals(0, repository.findByProductId(productId).size)
        sendCreateReviewEvent(productId, 1);
        sendCreateReviewEvent(productId, 2);
        sendCreateReviewEvent(productId, 3);
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
        sendCreateReviewEvent(productId, reviewId);
        assertEquals(1, repository.count())

        try {
            sendCreateReviewEvent(productId, reviewId)
            fail("Expected a MessagingException here!")
        } catch (me: MessagingException) {
            if (me.cause is InvalidInputException) {
                val iie = me.cause as InvalidInputException?
                assertEquals("Duplicate key, Product Id: 1, Review Id:1", iie!!.message)
            } else {
                fail("Expected a InvalidInputException as the root cause!")
            }
        }

        assertEquals(1, repository.count())
    }

    @Test
    fun deleteReviews() {
        val productId = 1
        val reviewId = 1
        sendCreateReviewEvent(productId, reviewId);
        assertEquals(1, repository.findByProductId(productId).size)
        sendDeleteReviewEvent(productId);
        assertEquals(0, repository.findByProductId(productId).size)
        sendDeleteReviewEvent(productId);
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
        getAndVerifyReviewsByProductId("?productId=213", HttpStatus.NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/review")
            .jsonPath("$.message").isEqualTo("No review found for productId: 213")
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

    private fun sendCreateReviewEvent(productId: Int, reviewId: Int) {
        val review = Review(
            productId, reviewId,
            "Author $reviewId", "Subject $reviewId", "Content $reviewId", "SA"
        )
        val event: Event<Int, Review> = Event(Event.Type.CREATE, productId, review)
        input.send(GenericMessage<Any>(event))
    }

    private fun sendDeleteReviewEvent(productId: Int) {
        val event: Event<Int, Review> = Event(Event.Type.DELETE, productId, null)
        input.send(GenericMessage<Any>(event))
    }
}