package com.msa.domain.product.rest

import com.msa.domain.product.vo.Product
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

interface ProductResource {

    @GetMapping(value = ["/product/{productId}"], produces = ["application/json"])
    fun getProduct(@PathVariable productId: Int): Mono<Product>

    @PostMapping(value = ["/product"], produces = ["application/json"])
    fun createProduct(@RequestBody body: Product): Product

    @DeleteMapping(value = ["/product/{productId}"])
    fun deleteProduct(@PathVariable productId: Int)
}