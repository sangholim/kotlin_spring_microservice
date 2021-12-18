package com.msa.product.rest

import com.msa.domain.product.rest.ProductResource
import com.msa.domain.product.vo.Product
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import com.msa.util.exception.*

@RestController
class ProductResourceImpl : ProductResource {

    override fun getProduct(@PathVariable productId: Int): Product {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
        if (productId == 13) throw NotFoundException("No product found for productId: $productId")
        return Product(productId, "name-$productId", 123, "kk-kk-kk")
    }
}