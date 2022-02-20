package com.msa.recommendation


import com.msa.recommendation.persistence.RecommendationEntity
import com.msa.recommendation.persistence.RecommendationRepository
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataMongoTest(properties = ["spring.cloud.config.enabled=false"])
class PersistenceTests {
    @Autowired
    private lateinit var repository: RecommendationRepository
    private lateinit var savedEntity: RecommendationEntity
    @BeforeEach
    fun setupDb() {
        repository.deleteAll().block()
        val entity = RecommendationEntity().apply {
            this.productId = 1
            this.recommendationId = 2
            this.author = "a"
            this.rating = 3
            this.content = "c"
        }
        //RecommendationEntity(1, 2, "a", 3, "c")
        savedEntity = repository.save(entity).block()!!
        assertEqualsRecommendation(entity, savedEntity)
    }

    @Test
    fun create() {
        val newEntity = RecommendationEntity().apply {
            this.productId = 1
            this.recommendationId = 3
            this.author = "a"
            this.rating = 3
            this.content = "c"
        }
           // RecommendationEntity(1, 3, "a", 3, "c")
        repository.save(newEntity).block()
        val foundEntity = repository.findById(newEntity.id).block()
        assertEqualsRecommendation(newEntity, foundEntity!!)
        assertEquals(2, repository.count().block())
    }

    @Test
    fun update() {
        savedEntity.author = "a2"
        repository.save(savedEntity).block()
        val foundEntity = repository.findById(savedEntity.id).block()!!
        assertEquals(2, foundEntity.version.toLong())
        assertEquals("a2", foundEntity.author)
    }

    @Test
    fun delete() {
        repository.delete(savedEntity).block()
        assertFalse(repository.existsById(savedEntity.id).block() ?: false)
    }

    @Test
    fun byProductId() {
            val entityList = repository.findByProductId(savedEntity.productId).collectList().block()!!
            assertThat(entityList, Matchers.hasSize<RecommendationEntity>(1))
            assertEqualsRecommendation(savedEntity, entityList[0])
        }

    @Test
    @Throws(DuplicateKeyException::class)
    fun duplicateError() {
        val entity = RecommendationEntity().apply {
            this.productId = 1
            this.recommendationId = 2
            this.author = "a"
            this.rating = 3
            this.content = "c"
        } //RecommendationEntity(1, 2, "a", 3, "c")
        assertThrows<DuplicateKeyException> {
            repository.save(entity).block()
        }
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id).block()!!
        val entity2 = repository.findById(savedEntity.id).block()!!

        // Update the entity using the first entity object
        entity1.author = "a1"
        repository.save(entity1).block()

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.author = "a2"
            repository.save(entity2).block()
            fail("Expected an OptimisticLockingFailureException")
        } catch (e: OptimisticLockingFailureException) {
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findById(savedEntity.id).block()!!
        assertEquals(2, updatedEntity.version)
        assertEquals("a1", updatedEntity.author)
    }

    private fun assertEqualsRecommendation(expectedEntity: RecommendationEntity?, actualEntity: RecommendationEntity) {
        assertEquals(expectedEntity!!.id, actualEntity.id)
        assertEquals(expectedEntity.version.toLong(), actualEntity.version.toLong())
        assertEquals(expectedEntity.productId.toLong(), actualEntity.productId.toLong())
        assertEquals(expectedEntity.recommendationId.toLong(), actualEntity.recommendationId.toLong())
        assertEquals(expectedEntity.author, actualEntity.author)
        assertEquals(expectedEntity.rating.toLong(), actualEntity.rating.toLong())
        assertEquals(expectedEntity.content, actualEntity.content)
    }
}