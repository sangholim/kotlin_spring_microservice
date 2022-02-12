package com.msa.product

import com.msa.domain.event.Event
import com.msa.domain.product.vo.Product
import com.msa.product.persistence.ProductRepository
import com.msa.util.exception.InvalidInputException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.messaging.MessagingException
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.support.AbstractMessageChannel
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Mono.just


@ExtendWith(value = [SpringExtension::class])
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.data.mongodb.port: 0", "eureka.client.enabled=false"]
)
class ProductServiceApplicationTests {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var channels: Sink

    private lateinit var input: SubscribableChannel

    @BeforeEach
    fun setupDb() {
        input = channels.input() as SubscribableChannel
        productRepository.deleteAll().block()
    }

    @Test
    fun getProductById() {
        val productId = 1
        assertNull(productRepository.findByProductId(productId).block())
        assertEquals(0, productRepository.count().block())

        sendCreateProductEvent(productId)

        assertNotNull(productRepository.findByProductId(productId).block())
        assertEquals(1, productRepository.count().block())

        getAndVerifyProduct(productId, HttpStatus.OK)
            .jsonPath("$.productId")
            .isEqualTo(productId)
    }

    @Test
    fun duplicateError() {
        val productId = 1

        assertNull(productRepository.findByProductId(productId).block())
        assertEquals(0, productRepository.count().block())

        sendCreateProductEvent(productId)

        assertNotNull(productRepository.findByProductId(productId).block())
        assertEquals(1, productRepository.count().block())

        try {
            sendCreateProductEvent(productId);

            fail("Expected a MessagingException here!");
        } catch (me: MessagingException) {
            if (me.cause is InvalidInputException) {
                val iie = me.cause as InvalidInputException;
                assertEquals("Duplicate key, Product Id: $productId", iie.message);
            } else {
                fail("Expected a InvalidInputException as the root cause!");
            }
        }
    }

    @Test
    fun deleteProduct() {
        val productId = 1

        sendCreateProductEvent(productId)
        assertNotNull(productRepository.findByProductId(productId))

        sendDeleteProductEvent(productId)
        assertNull(productRepository.findByProductId(productId).block())
    }

    @Test
    fun getProductInvalidParameterString() {
        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/product/no-integer")
    }

    @Test
    fun getProductNotFound() {
        val productIdNotFound = 13
        getAndVerifyProduct(13, HttpStatus.NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product/$productIdNotFound")
            .jsonPath("$.message").isEqualTo("No product found for productId: $productIdNotFound")
    }

    @Test
    fun getProductInvalidParameterNegativeValue() {
        val productIdInvalid = -1
        getAndVerifyProduct(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product/$productIdInvalid")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }

    private fun getAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return getAndVerifyProduct("/$productId", expectedStatus)
    }

    private fun getAndVerifyProduct(productIdPath: String, expectedStatus: HttpStatus): BodyContentSpec {
        return client.get()
            .uri("/product$productIdPath")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        val product = Product(productId, "Name $productId", productId, "SA")
        return client.post()
            .uri("/product")
            .body(just(product), Product::class.java)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
    }

    private fun deleteAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return client.delete()
            .uri("/product/$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()
    }


    private fun sendCreateProductEvent(productId: Int) {
        val product = Product(productId, "Name $productId", productId, "SA")
        val event: Event<Int, Product> = Event(Event.Type.CREATE, productId, product)
        input.send(GenericMessage<Any>(event))
    }

    private fun sendDeleteProductEvent(productId: Int) {
        val event: Event<Int, Product> = Event(Event.Type.DELETE, productId, null)
        input.send(GenericMessage<Any>(event))
    }
}