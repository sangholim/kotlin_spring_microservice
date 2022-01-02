package com.msa.productComposite.api.composite.product.rest

import com.msa.productComposite.api.composite.product.service.ProductCompositeIntegration
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductCompositeResource(val productCompositeIntegration: ProductCompositeIntegration) {

    @ApiOperation(
        value = "\${api.product-composite.get-composite-product.description}",
        notes =  "\${api.product-composite.get-composite-product.notes}")
    @ApiResponses(
        value = [
            ApiResponse(
                code = 400, message = "Bad Request, invalid format of request. " +
                        "See response message for more information."),
            ApiResponse(
                code = 404, message = "Not found, the specified id does not exist."),
            ApiResponse(
                code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. " +
                        "See response message for more information.")
        ]
    )
    @GetMapping(value = ["/product-composite/{productId}"])
    fun getProduct(@PathVariable productId: Int) = productCompositeIntegration.integration(productId)
}