package com.msa.recommendation

import com.msa.recommendation.persistence.RecommendationEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.index.IndexDefinition
import org.springframework.data.mongodb.core.index.IndexResolver
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver


@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.recommendation", "com.msa.util"])
class RecommendationApplication {

	@Autowired
	private lateinit var mongoTemplate: ReactiveMongoOperations

	@EventListener(ContextRefreshedEvent::class)
	fun initIndicesAfterStartup() {
		val mappingContext = mongoTemplate.converter.mappingContext
		val resolver: IndexResolver = MongoPersistentEntityIndexResolver(mappingContext)
		val indexOps = mongoTemplate.indexOps(RecommendationEntity::class.java)
		resolver.resolveIndexFor(RecommendationEntity::class.java).forEach { e: IndexDefinition? ->
			indexOps.ensureIndex(e!!).block()
		}
	}

}

fun main(args: Array<String>) {
	val application = SpringApplication(RecommendationApplication::class.java)
	application.addListeners(ApplicationPidFileWriter())
	application.run(*args)
}
