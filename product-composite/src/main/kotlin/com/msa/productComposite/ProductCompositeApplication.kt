package com.msa.productComposite

import com.msa.productComposite.api.composite.product.service.ProductCompositeIntegration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.health.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors.basePackage
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType.SWAGGER_2
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2


@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.productComposite", "com.msa.util"])
@EnableBinding(value = [ProductCompositeIntegration.MessageSources::class])
@EnableSwagger2
class ProductCompositeApplication {
    @Value("\${api.common.version}")
    lateinit var apiVersion: String

    @Value("\${api.common.title}")
    lateinit var apiTitle: String

    @Value("\${api.common.description}")
    lateinit var apiDescription: String

    @Value("\${api.common.termsOfServiceUrl}")
    lateinit var apiTermsOfServiceUrl: String

    @Value("\${api.common.license}")
    lateinit var apiLicense: String

    @Value("\${api.common.licenseUrl}")
    lateinit var apiLicenseUrl: String

    @Value("\${api.common.contact.name}")
    lateinit var apiContactName: String

    @Value("\${api.common.contact.url}")
    lateinit var apiContactUrl: String

    @Value("\${api.common.contact.email}")
    lateinit var apiContactEmail: String


    @Autowired
    lateinit var healthAggregator: HealthAggregator

    /**
     * Will exposed on $HOST:$PORT/swagger-ui.html
     *
     * @return
     */
    @Bean
    fun apiDocumentation(): Docket {

        return Docket(SWAGGER_2)
            .select()
            .apis(basePackage("com.msa.productComposite"))
            .paths(PathSelectors.any())
            .build()
            .globalResponseMessage(RequestMethod.GET, emptyList())
            .apiInfo(
                ApiInfo(
                    apiTitle,
                    apiDescription,
                    apiVersion,
                    apiTermsOfServiceUrl,
                    Contact(apiContactName, apiContactUrl, apiContactEmail),
                    apiLicense,
                    apiLicenseUrl,
                    emptyList()
                )
            )
    }

    @Bean
    fun coreServices(productCompositeIntegration: ProductCompositeIntegration): ReactiveHealthIndicator {
        val registry: ReactiveHealthIndicatorRegistry = DefaultReactiveHealthIndicatorRegistry(LinkedHashMap())
        registry.register("product") { productCompositeIntegration.getProductHealth() }
        registry.register("recommendation") { productCompositeIntegration.getRecommendationHealth() }
        registry.register("review") { productCompositeIntegration.getReviewHealth() }
        return CompositeReactiveHealthIndicator(healthAggregator, registry)
    }

}

fun main(args: Array<String>) {
    val application = SpringApplication(ProductCompositeApplication::class.java)
    application.addListeners(ApplicationPidFileWriter())
    application.run(*args)
}
