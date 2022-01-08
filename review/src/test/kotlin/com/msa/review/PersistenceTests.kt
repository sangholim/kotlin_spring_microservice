package com.msa.review

import com.msa.review.persistence.ReviewEntity
import com.msa.review.persistence.ReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PersistenceTests {

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    private lateinit var savedEntity: ReviewEntity

    @BeforeEach
    fun setupDb() {
        reviewRepository.deleteAll()
        val reviewEntity = ReviewEntity().apply {
            this.productId = 1
            this.reviewId = 2
            this.author = "a"
            this.subject = "s"
            this.content = "c"
        }
        savedEntity = reviewRepository.save(reviewEntity)
        assertEqualsReview(reviewEntity, savedEntity)

    }

    @Test
    fun create() {
        val reviewEntity = ReviewEntity().apply {
            this.productId = 1
            this.reviewId = 3
            this.author = "a"
            this.subject = "s"
            this.content = "c"
        }
        reviewRepository.save(reviewEntity)
        val foundEntity = reviewRepository.findById(reviewEntity.id!!).get()
        assertEqualsReview(reviewEntity, foundEntity)
        assertEquals(2, reviewRepository.count())
    }

    @Test
    fun update() {
        savedEntity.author = "a2"
        reviewRepository.save(savedEntity)

        val fountEntity = reviewRepository.findById(savedEntity.id!!).get()
        assertEquals(1, fountEntity.version)
        assertEquals("a2", fountEntity.author)
    }

    @Test
    fun delete() {
        reviewRepository.delete(savedEntity)
        assertEquals(false, reviewRepository.existsById(savedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        val entityList = reviewRepository.findByProductId(savedEntity.productId)

        assertThat(entityList, hasSize<ReviewEntity>(1))
        assertEqualsReview(savedEntity, entityList[0])
    }

    @Test
    @Throws(DataIntegrityViolationException::class)
    fun duplicateError() {
        val reviewEntity = ReviewEntity().apply {
            this.productId = 1
            this.reviewId = 2
            this.author = "a"
            this.subject = "s"
            this.content = "c"
        }
        assertThrows<DataIntegrityViolationException> {
            reviewRepository.save(reviewEntity)
        }
    }

    @Test
    fun optimisticLockError() {
        val reviewEntity1 = reviewRepository.findById(savedEntity.id!!).get()
        val reviewEntity2 = reviewRepository.findById(savedEntity.id!!).get()
        reviewEntity1.author = "a1"
        reviewRepository.save(reviewEntity1)

        // optimistic lock error
        try {
            reviewEntity2.author = "a2"
            reviewRepository.save(reviewEntity2)
        } catch (e: OptimisticLockingFailureException) {
            println(e.message)
        }
        val updatedEntity = reviewRepository.findById(savedEntity.id!!).get()
        assertEquals(1, updatedEntity.version)
        assertEquals(reviewEntity1.author, updatedEntity.author)
    }

    private fun assertEqualsReview(expectedEntity: ReviewEntity, actualEntity: ReviewEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.reviewId, actualEntity.reviewId)
        assertEquals(expectedEntity.author, actualEntity.author)
        assertEquals(expectedEntity.subject, actualEntity.subject)
        assertEquals(expectedEntity.content, actualEntity.content)
    }
}