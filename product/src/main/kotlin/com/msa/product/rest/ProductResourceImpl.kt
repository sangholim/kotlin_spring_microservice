package com.msa.product.rest

import com.msa.domain.product.rest.ProductResource
import com.msa.domain.product.vo.Product
import com.msa.product.persistence.ProductRepository
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import com.msa.util.exception.*
import com.msa.util.http.ServiceUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorMap

@RestController
class ProductResourceImpl(
    val serviceUtil: ServiceUtil,
    val productRepository: ProductRepository,
    val productMapper: ProductMapper
) : ProductResource {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    }
    override fun getProduct(@PathVariable productId: Int): Mono<Product> {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
        if (productId == 13) throw NotFoundException("No product found for productId: $productId")
        return productRepository.findByProductId(productId)
            .map {
                productMapper.entityToApi(it).apply {
                    this.serviceAddress = serviceUtil.getServiceAddress()
                }
            }
            .switchIfEmpty(Mono.error(NotFoundException("No product found for productId: $productId")))
    }

    override fun createProduct(body: Product): Product {
        if(body.productId < 1) throw InvalidInputException("Invalid productId:${body.productId}")
        val productEntity = productMapper.apiToEntity(body)
        return productRepository.save(productEntity)
            .onErrorMap(DuplicateKeyException::class) { InvalidInputException("Duplicate key, Product Id: ${body.productId}") }
            .map {
                productMapper.entityToApi(it)
            }.block() ?: Product()
    }

    override fun deleteProduct(productId: Int) {
        productRepository.findByProductId(productId)
            .flatMap {
                productRepository.delete(it)
            }.block()
    }
}