package com.msa.product

import com.msa.product.persistence.ProductEntity
import com.msa.product.persistence.ProductRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.stream.Collectors
import java.util.stream.IntStream


@ExtendWith(SpringExtension::class)
@DataMongoTest
class PersistenceTests {
    @Autowired
    private val repository: ProductRepository? = null
    private var savedEntity: ProductEntity? = null

    @BeforeEach
    fun setupDb() {
        repository!!.deleteAll()
        val entity = ProductEntity().apply {
            this.productId = 1
            this.name = "n"
            this.weight = 1
        }//ProductEntity(1, "n", 1)
        savedEntity = repository.save(entity)
        assertEqualsProduct(entity, savedEntity!!)
    }

    @Test
    fun create() {
        val newEntity = ProductEntity().apply {
            this.productId = 2
            this.name = "n"
            this.weight = 2
        }
        repository!!.save(newEntity)
        val foundEntity = repository.findById(newEntity.id).get()
        assertEqualsProduct(newEntity, foundEntity)
        assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        savedEntity!!.name = "n2"
        repository!!.save(savedEntity!!)
        val foundEntity = repository.findById(savedEntity!!.id).get()
        assertEquals(2, foundEntity.version.toLong())
        assertEquals("n2", foundEntity.name)
    }

    @Test
    fun delete() {
        repository!!.delete(savedEntity!!)
        assertFalse(repository.existsById(savedEntity!!.id))
    }

    @get:Test
    val byProductId: Unit
        get() {
            val entity: ProductEntity = repository!!.findByProductId(savedEntity!!.productId) ?: ProductEntity()
            assertTrue(entity.productId > 0)
            assertEqualsProduct(savedEntity!!, entity)
        }

    @Test
    @Throws(DuplicateKeyException::class)
    fun duplicateError(){
        val entity = ProductEntity().apply {
            this.productId = savedEntity!!.productId
            this.name = "n"
            this.weight = 1
        }//ProductEntity(savedEntity!!.productId, "n", 1)
        repository!!.save(entity)
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository!!.findById(savedEntity!!.id).get()
        val entity2 = repository.findById(savedEntity!!.id).get()

        // Update the entity using the first entity object
        entity1.name = "n1"
        repository.save(entity1)

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.name = "n2"
            repository.save(entity2)
            fail("Expected an OptimisticLockingFailureException")
        } catch (e: OptimisticLockingFailureException) {
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findById(savedEntity!!.id).get()
        assertEquals(2, updatedEntity.version)
        assertEquals("n1", updatedEntity.name)
    }

    @Test
    fun paging() {

        repository!!.deleteAll()
        val newProducts = IntStream.rangeClosed(1001, 1010)
            .mapToObj { i: Int ->
                ProductEntity().apply {
                    this.productId = i
                    this.name = "name $i"
                    this.weight = i
                }
            }
            .collect(Collectors.toList())
        repository.saveAll(newProducts)
        var nextPage: Pageable = PageRequest.of(0, 4, Sort.Direction.ASC, "productId")
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true)
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true)
        testNextPage(nextPage, "[1009, 1010]", false)
    }

    private fun testNextPage(nextPage: Pageable, expectedProductIds: String, expectsNextPage: Boolean): Pageable {
        val productPage = repository!!.findAll(nextPage)
        assertEquals(
            expectedProductIds,
            productPage.content.stream().map { p: ProductEntity -> p.productId }.collect(Collectors.toList()).toString()
        )
        assertEquals(expectsNextPage, productPage.hasNext())
        return productPage.nextPageable()
    }

    private fun assertEqualsProduct(expectedEntity: ProductEntity, actualEntity: ProductEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.name, actualEntity.name)
        assertEquals(expectedEntity.weight, actualEntity.weight)
    }
}