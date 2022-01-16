package com.msa.recommendation

import com.msa.domain.event.Event
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.recommendation.persistence.RecommendationRepository
import com.msa.util.exception.InvalidInputException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import kotlin.test.fail


@ExtendWith(value = [SpringExtension::class])
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.data.mongodb.port: 0"]
)
class RecommendationServiceApplicationTests {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: RecommendationRepository

    @Autowired
    private lateinit var channels: Sink

    private lateinit var input: SubscribableChannel



    @BeforeEach
    fun setupDb() {
        input = channels.input() as SubscribableChannel
        repository.deleteAll().block()
    }

    @Test
    fun getRecommendationsByProductId() {
        val productId = 1
        sendCreateRecommendationEvent(productId, 1)
        sendCreateRecommendationEvent(productId, 2)
        sendCreateRecommendationEvent(productId, 3)
        assertEquals(3, repository.findByProductId(productId).count().block()!!)

        getAndVerifyRecommendationsByProductId(productId, HttpStatus.OK)
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[2].productId").isEqualTo(productId)
            .jsonPath("$[2].recommendationId").isEqualTo(3)
    }

    @Test
    fun duplicateError() {
        val productId = 1
        val recommendationId = 1

        sendCreateRecommendationEvent(productId, recommendationId);
        assertEquals(1, repository.count().block()!!)

        try {
            sendCreateRecommendationEvent(productId, recommendationId)
            fail("Expected a MessagingException here!")
        } catch (me: MessagingException) {
            if (me.cause is InvalidInputException) {
                val iie = me.cause as InvalidInputException?
                assertEquals("Duplicate key, Product Id: 1 Recommendation Id: 1", iie!!.message)
            } else {
                fail("Expected a InvalidInputException as the root cause!")
            }
        }
    }

    @Test
    fun deleteRecommendations() {
        val productId = 1
        val recommendationId = 1

        sendCreateRecommendationEvent(productId, recommendationId);
        assertEquals(1, repository.findByProductId(productId).count().block()!!)
        sendDeleteRecommendationEvent(productId)
        assertEquals(0, repository.findByProductId(productId).count().block()!!)

    }

    @Test
    fun getRecommendationsMissingParameter() {
        getAndVerifyRecommendationsByProductId("", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("")
        //.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present")
    }

    @Test
    fun getRecommendationsInvalidParameter() {
        getAndVerifyRecommendationsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("")
            //.jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getRecommendationsNotFound() {
        getAndVerifyRecommendationsByProductId("?productId=113", HttpStatus.OK)
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun getRecommendationsInvalidParameterNegativeValue() {
        val productIdInvalid = -1
        getAndVerifyRecommendationsByProductId("?productId=$productIdInvalid", HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }

    private fun getAndVerifyRecommendationsByProductId(
        productId: Int,
        expectedStatus: HttpStatus
    ): BodyContentSpec {
        return getAndVerifyRecommendationsByProductId("?productId=$productId", expectedStatus)
    }


    private fun getAndVerifyRecommendationsByProductId(
        productIdQuery: String,
        expectedStatus: HttpStatus
    ): BodyContentSpec {
        return client.get()
            .uri("/recommendation$productIdQuery")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyRecommendation(
        productId: Int,
        recommendationId: Int,
        expectedStatus: HttpStatus
    ): BodyContentSpec {
        val recommendation = Recommendation(
            productId, recommendationId,
            "Author $recommendationId", recommendationId, "Content $recommendationId", "SA"
        )
        return client.post()
            .uri("/recommendation")
            .body(just(recommendation), Recommendation::class.java)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun deleteAndVerifyRecommendationsByProductId(
        productId: Int,
        expectedStatus: HttpStatus
    ): BodyContentSpec {
        return client.delete()
            .uri("/recommendation?productId=$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()
    }

    private fun sendCreateRecommendationEvent(productId: Int, recommendationId: Int) {
        val recommendation = Recommendation(
            productId, recommendationId,
            "Author $recommendationId", recommendationId, "Content $recommendationId", "SA"
        )
        val event: Event<Int, Recommendation> = Event(Event.Type.CREATE, productId, recommendation)
        input.send(GenericMessage<Any>(event))
    }

    private fun sendDeleteRecommendationEvent(productId: Int) {
        val event: Event<Int, Recommendation> = Event(Event.Type.DELETE, productId, null)
        input.send(GenericMessage<Any>(event))
    }
}