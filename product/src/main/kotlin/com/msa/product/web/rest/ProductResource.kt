package com.msa.product.web.rest

import com.msa.product.domain.Product
import com.msa.product.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductResource(val productService: ProductService) {
    @GetMapping(value = ["/product/{productId}"], produces = ["application/json"])
    fun getProduct(@PathVariable productId: Int): ResponseEntity<Product> {
        val product = productService.getProduct()
        return ResponseEntity(product, HttpStatus.OK)
    }
}