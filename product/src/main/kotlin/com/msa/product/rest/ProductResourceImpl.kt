package com.msa.product.rest

import com.msa.domain.product.rest.ProductResource
import com.msa.domain.product.vo.Product
import com.msa.product.persistence.ProductRepository
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import com.msa.util.exception.*
import com.msa.util.http.ServiceUtil
import org.springframework.dao.DuplicateKeyException

@RestController
class ProductResourceImpl(
    val serviceUtil: ServiceUtil,
    val productRepository: ProductRepository,
    val productMapper: ProductMapper
) : ProductResource {

    override fun getProduct(@PathVariable productId: Int): Product {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
        if (productId == 13) throw NotFoundException("No product found for productId: $productId")
        val entity =
            productRepository.findByProductId(productId) ?: throw NotFoundException("No Product found for $productId")
        val product = productMapper.entityToApi(entity)
        product.serviceAddress = serviceUtil.getServiceAddress()
        return product
    }

    override fun createProduct(body: Product): Product = try {
        val productEntity = productMapper.apiToEntity(body)
        val newProductEntity = productRepository.save(productEntity)
        productMapper.entityToApi(newProductEntity)
    } catch (dke: DuplicateKeyException) {
        throw InvalidInputException("Duplicate key, Product Id: ${body.productId}");
    }

    override fun deleteProduct(productId: Int): Unit = productRepository.findByProductId(productId)?.run {
        productRepository.delete(this)
    } ?: throw NotFoundException("No Product found for $productId")

}