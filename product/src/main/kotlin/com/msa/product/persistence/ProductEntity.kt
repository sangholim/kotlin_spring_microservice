package com.msa.product.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "products")
class ProductEntity {
    @Id
    lateinit var id: String

    @Version
    var version: Int = 0

    @Indexed(unique = true)
    var productId: Int = 0

    lateinit var name: String
    var weight: Int = 0

}