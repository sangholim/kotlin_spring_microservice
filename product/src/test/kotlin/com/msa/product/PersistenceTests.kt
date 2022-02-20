package com.msa.product

import com.msa.product.persistence.ProductEntity
import com.msa.product.persistence.ProductRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.test.StepVerifier
import java.security.InvalidParameterException


@ExtendWith(SpringExtension::class)
@DataMongoTest(properties = ["spring.cloud.config.enabled=false"])
class PersistenceTests {
    @Autowired
    private lateinit var repository: ProductRepository
    private lateinit var savedEntity: ProductEntity

    @BeforeEach
    fun setupDb() {
        repository.deleteAll().block()
        val entity = ProductEntity().apply {
            this.productId = 1
            this.name = "n"
            this.weight = 1
        }//ProductEntity(1, "n", 1)
        StepVerifier.create(repository.save(entity))
            .expectNextMatches {
                it.run {
                    savedEntity = this
                    areProductEqual(savedEntity, this)
                }
            }.verifyComplete()
    }

    @Test
    fun create() {
        val newEntity = ProductEntity().apply {
            this.productId = 2
            this.name = "n"
            this.weight = 2
        }
        StepVerifier.create(repository.save(newEntity))
            .expectNextMatches { newEntity.productId == it.productId }
            .verifyComplete()

        StepVerifier.create(repository.findById(newEntity.id))
            .expectNextMatches { areProductEqual(newEntity, it) }
            .verifyComplete()

        StepVerifier.create(repository!!.count())
            .expectNext(2).verifyComplete()

    }

    @Test
    fun update() {
        savedEntity.name = "n2"
        StepVerifier.create(repository.save(savedEntity))
            .expectNextMatches { it.version == 2 && it.name == "n2" }
            .verifyComplete()

        StepVerifier.create(repository.findById(savedEntity.id))
            .expectNextMatches { it.version == 2 && it.name == "n2" }
            .verifyComplete()
    }

    @Test
    fun delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete()
        StepVerifier.create(repository.existsById(savedEntity.id)).expectNext(false).verifyComplete()
    }

    @Test
    fun byProductId() {
        StepVerifier.create(repository.findByProductId(savedEntity.productId))
            .expectNextMatches { areProductEqual(savedEntity, it) }.verifyComplete()
    }

    @Test
    fun duplicateError() {
        val entity = ProductEntity().apply {
            this.productId = savedEntity.productId
            this.name = "n"
            this.weight = 1
        }//ProductEntity(savedEntity!!.productId, "n", 1)
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException::class.java).verify()
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id).block() ?: throw InvalidParameterException("not found")
        val entity2 = repository.findById(savedEntity.id).block() ?: throw InvalidParameterException("not found")

        // Update the entity using the first entity object
        entity1.name = "n1"
        repository.save(entity1).block()

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException::class.java).verify()

        // Get the updated entity from the database and verify its new sate
        StepVerifier.create(repository.findById(savedEntity.id))
            .expectNextMatches { it.version == 1 && it.name == "n1" }

    }

    private fun areProductEqual(expectedEntity: ProductEntity, actualEntity: ProductEntity): Boolean {
        return expectedEntity.id == actualEntity.id &&
                expectedEntity.version == actualEntity.version &&
                expectedEntity.productId == actualEntity.productId &&
                expectedEntity.name == actualEntity.name &&
                expectedEntity.weight == actualEntity.weight
    }
}