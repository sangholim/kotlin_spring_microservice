package com.msa.review

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

@SpringBootApplication
@ComponentScan(basePackages = ["com.msa.review", "com.msa.util"])
class ReviewApplication {

	private val LOG: Logger = LoggerFactory.getLogger(this.javaClass)

	@Value("\${spring.datasource.maximum-pool-size:10}")
	private var connectionPoolSize: Int = 10

	@Bean
	fun jdbcScheduler(): Scheduler {
		LOG.info("Creates a jdbcScheduler with connectionPoolSize = $connectionPoolSize")
		return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize))
	}


}

fun main(args: Array<String>) {
	val application = SpringApplication(ReviewApplication::class.java)
	application.addListeners(ApplicationPidFileWriter())
	application.run(*args)
}
