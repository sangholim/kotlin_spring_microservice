package com.msa.productComposite.api.composite.product.rest

import com.msa.productComposite.api.composite.product.service.ProductCompositeIntegration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductCompositeResource(val productCompositeIntegration: ProductCompositeIntegration) {

    @GetMapping(value = ["/product-composite/{productId}"])
    fun getProduct(@PathVariable productId: Int) = productCompositeIntegration.integration(productId)
}