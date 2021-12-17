package com.msa.productcomposite.api.composite.product.service

import com.msa.productcomposite.api.composite.product.domain.ProductAggregate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductCompositeResource {

    @GetMapping(value = ["/product-composite/{productId}"])
    fun getProduct(@PathVariable productId: Int): ProductAggregate = ProductAggregate()
}