package com.msa.product.rest

import com.msa.domain.product.vo.Product
import com.msa.product.persistence.ProductEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface ProductMapper {

    @Mappings(
        Mapping(target = "serviceAddress", ignore = true)
    )
    fun entityToApi(entity: ProductEntity): Product

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "version", ignore = true)
    )
    fun apiToEntity(product: Product): ProductEntity
}