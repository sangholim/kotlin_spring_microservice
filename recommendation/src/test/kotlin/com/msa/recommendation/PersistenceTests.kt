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
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataMongoTest
class PersistenceTests {
    @Autowired
    private val repository: RecommendationRepository? = null
    private var savedEntity: RecommendationEntity? = null
    @BeforeEach
    fun setupDb() {
        repository!!.deleteAll()
        val entity = RecommendationEntity().apply {
            this.productId = 1
            this.recommendationId = 2
            this.author = "a"
            this.rating = 3
            this.content = "c"
        }
        //RecommendationEntity(1, 2, "a", 3, "c")
        savedEntity = repository.save(entity)
        assertEqualsRecommendation(entity, savedEntity!!)
    }

    @Test
    fun create() {
        val newEntity = RecommendationEntity().apply {
            this.productId = 1
            this.recommendationId = 2
            this.author = "a"
            this.rating = 3
            this.content = "c"
        }
           // RecommendationEntity(1, 3, "a", 3, "c")
        repository!!.save(newEntity)
        val foundEntity = repository.findById(newEntity.id).get()
        assertEqualsRecommendation(newEntity, foundEntity)
        assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        savedEntity!!.author = "a2"
        repository!!.save(savedEntity!!)
        val foundEntity = repository.findById(savedEntity!!.id).get()
        assertEquals(2, foundEntity.version.toLong())
        assertEquals("a2", foundEntity.author)
    }

    @Test
    fun delete() {
        repository!!.delete(savedEntity!!)
        assertFalse(repository.existsById(savedEntity!!.id))
    }

    @get:Test
    val byProductId: Unit
        get() {
            val entityList = repository!!.findByProductId(savedEntity!!.productId)
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
        repository!!.save(entity)
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository!!.findById(savedEntity!!.id).get()
        val entity2 = repository.findById(savedEntity!!.id).get()

        // Update the entity using the first entity object
        entity1.author = "a1"
        repository.save(entity1)

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.author = "a2"
            repository.save(entity2)
            fail("Expected an OptimisticLockingFailureException")
        } catch (e: OptimisticLockingFailureException) {
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findById(savedEntity!!.id).get()
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