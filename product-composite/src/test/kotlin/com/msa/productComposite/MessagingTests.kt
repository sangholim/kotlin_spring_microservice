package com.msa.productComposite

import com.msa.domain.composite.ProductAggregate
import com.msa.domain.composite.RecommendationSummary
import com.msa.domain.composite.ReviewSummary
import com.msa.domain.event.Event
import com.msa.domain.product.vo.Product
import com.msa.domain.recommendation.vo.Recommendation
import com.msa.domain.review.vo.Review
import com.msa.productComposite.IsSameEvent.Companion.sameEventExceptCreatedAt
import com.msa.productComposite.api.composite.product.service.ProductCompositeIntegration
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono.just

import java.util.concurrent.BlockingQueue

@ExtendWith(value = [SpringExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["eureka.client.enabled=false"])
@Disabled
class MessagingTests {
    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var channels: ProductCompositeIntegration.MessageSources

    @Autowired
    private lateinit var collector: MessageCollector
    private lateinit var queueProducts: BlockingQueue<Message<*>>
    private lateinit var queueRecommendations: BlockingQueue<Message<*>>
    private lateinit var queueReviews: BlockingQueue<Message<*>>

    @BeforeEach
    fun setUp() {
        queueProducts = getQueue(channels.outputProducts())
        queueRecommendations = getQueue(channels.outputRecommendations())
        queueReviews = getQueue(channels.outputReviews())
    }

    @Test
    fun createCompositeProduct1() {
        val composite = ProductAggregate(1, "name", 1, null, null, null)
        postAndVerifyProduct(composite, HttpStatus.OK)

        // Assert one expected new product events queued up
        assertEquals(1, queueProducts!!.size)
        val expectedEvent: Event<Int, Product> =
            Event(
                Event.Type.CREATE,
                composite.productId,
                Product(composite.productId, composite.name, composite.weight, "")
            )
        assertThat(
            queueProducts,
            `is`(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent)))
        )

        // Assert none recommendations and review events
        assertEquals(0, queueRecommendations!!.size)
        assertEquals(0, queueReviews!!.size)
    }

    @Test
    fun createCompositeProduct2() {
        val composite = ProductAggregate(
            1, "name", 1,
            listOf(RecommendationSummary(1, "a", "c", 1)),
            listOf(ReviewSummary(1, "a", "s", "c")), null
        )
        postAndVerifyProduct(composite, HttpStatus.OK)

        // Assert one create product event queued up
        assertEquals(1, queueProducts!!.size)
        val expectedProductEvent: Event<Int, Product> =
            Event(
                Event.Type.CREATE,
                composite.productId,
                Product(composite.productId, composite.name, composite.weight, "")
            )
        assertThat(
            queueProducts,
            receivesPayloadThat(sameEventExceptCreatedAt(expectedProductEvent))
        )

        // Assert one create recommendation event queued up
        assertEquals(1, queueRecommendations!!.size)
        val rec = composite.recommendations!![0]
        val expectedRecommendationEvent: Event<Int, Recommendation> = Event(
            Event.Type.CREATE,
            composite.productId,
            Recommendation(composite.productId, rec.recommendationId, rec.author, rec.rate, rec.content, "")
        )
        assertThat(
            queueRecommendations,
            receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEvent))
        )

        // Assert one create review event queued up
        assertEquals(1, queueReviews.size)
        val rev = composite.reviews!![0]
        val expectedReviewEvent: Event<Int, Review> = Event(
            Event.Type.CREATE,
            composite.productId,
            Review(composite.productId, rev.reviewId, rev.author, rev.subject, rev.content, "")
        )
        assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)))
    }

    @Test
    fun deleteCompositeProduct() {
        deleteAndVerifyProduct(1, HttpStatus.OK)

        // Assert one delete product event queued up
        assertEquals(1, queueProducts.size)
        val expectedEvent: Event<Int, Product> = Event(Event.Type.DELETE, 1, null)
        assertThat(
            queueProducts,
            `is`(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent)))
        )

        // Assert one delete recommendation event queued up
        assertEquals(1, queueRecommendations!!.size)
        val expectedRecommendationEvent: Event<Int, Product> = Event(Event.Type.DELETE, 1, null)
        assertThat(
            queueRecommendations,
            receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEvent))
        )

        // Assert one delete review event queued up
        assertEquals(1, queueReviews!!.size)
        val expectedReviewEvent: Event<Int, Product> = Event(Event.Type.DELETE, 1, null)
        assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)))
    }

    private fun getQueue(messageChannel: MessageChannel): BlockingQueue<Message<*>> {
        return collector.forChannel(messageChannel)
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

    companion object {
        private const val PRODUCT_ID_OK = 1
        private const val PRODUCT_ID_NOT_FOUND = 2
        private const val PRODUCT_ID_INVALID = 3
    }
}