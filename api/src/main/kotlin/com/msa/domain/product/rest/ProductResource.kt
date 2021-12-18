package com.msa.domain.product.rest

import com.msa.domain.product.vo.Product
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

interface ProductResource {

    @GetMapping(value = ["/product/{productId}"], produces = ["application/json"])
    fun getProduct(@PathVariable productId: Int): Product
}