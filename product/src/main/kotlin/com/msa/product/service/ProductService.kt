package com.msa.product.service

import com.msa.product.domain.Product
import org.springframework.stereotype.Service

@Service
class ProductService {
    
    fun getProduct() = Product(1,"test-name", 50, "서울")
}