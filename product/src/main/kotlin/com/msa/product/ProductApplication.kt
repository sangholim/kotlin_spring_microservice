package com.msa.product

import com.msa.product.persistence.ProductEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.IndexDefinition
import org.springframework.data.mongodb.core.index.IndexResolver
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver


@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.product", "com.msa.util"])
class ProductApplication {


    @Autowired
    lateinit var reactiveMongoTemplate: ReactiveMongoTemplate

    @EventListener(ContextRefreshedEvent::class)
    fun initIndicesAfterStartup() {
        val mappingContext = reactiveMongoTemplate.converter.mappingContext
        val resolver: IndexResolver = MongoPersistentEntityIndexResolver(mappingContext)
        val indexOps = reactiveMongoTemplate.indexOps(ProductEntity::class.java)
        resolver.resolveIndexFor(ProductEntity::class.java).forEach { e: IndexDefinition -> indexOps.ensureIndex(e).block() }
    }

}

fun main(args: Array<String>) {
    val application = SpringApplication(ProductApplication::class.java)

    application.addListeners(ApplicationPidFileWriter())
    application.run(*args)
}
