package com.msa.product

import com.msa.domain.product.vo.Product
import com.msa.product.persistence.ProductRepository
import org.junit.jupiter.api.Assertions.assertTrue
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
    properties = ["spring.data.mongodb.port: 0"]
)
class ProductServiceApplicationTests {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var productRepository: ProductRepository

    @BeforeEach
    fun setupDb() {
        productRepository.deleteAll()
    }

    @Test
    fun getProductById() {
        val productId = 1
        postAndVerifyProduct(productId, HttpStatus.OK)
        assertTrue(productRepository.findByProductId(productId)!!.productId == productId)
        getAndVerifyProduct(productId, HttpStatus.OK)
            .jsonPath("$.productId")
            .isEqualTo(productId)
    }

    @Test
    fun duplicateError() {
        val productId = 1
        postAndVerifyProduct(productId, HttpStatus.OK)
        assertTrue(productRepository.findByProductId(productId)!!.productId == productId)
        postAndVerifyProduct(productId, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product")
            .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: $productId");
    }

    @Test
    fun deleteProduct() {
        val productId = 1
        postAndVerifyProduct(productId, HttpStatus.OK)
        assertTrue(productRepository.findByProductId(productId)!!.productId == productId)
        deleteAndVerifyProduct(productId, HttpStatus.OK)
        assertTrue(productRepository.findByProductId(productId) == null)
        deleteAndVerifyProduct(productId, HttpStatus.NOT_FOUND)
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
}