package com.msa.product.persistence

import org.springframework.data.repository.PagingAndSortingRepository

interface ProductRepository : PagingAndSortingRepository<ProductEntity, String> {

    fun findByProductId(productId: Int): ProductEntity?
}