package com.msa.gateway

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Configuration
class HealthCheckConfiguration (
    webClientBuilder: WebClient.Builder,
    val healthAggregator: HealthAggregator
) {
    private var webClient: WebClient = webClientBuilder.build()
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun healthcheckMicroservices(): ReactiveHealthIndicator {
        val registry: ReactiveHealthIndicatorRegistry = DefaultReactiveHealthIndicatorRegistry(LinkedHashMap())
        registry.register("product") { getHealth("http://product") }
        registry.register("recommendation") { getHealth("http://recommendation") }
        registry.register("review") { getHealth("http://review") }
        registry.register("product-composite") { getHealth("http://product-composite") }
        return CompositeReactiveHealthIndicator(healthAggregator, registry)
    }

    private fun getHealth(url: String): Mono<Health> {
        log.debug("Will call the Health API on URL: $url")
        return webClient.get().uri("$url/actuator/health").retrieve().bodyToMono(String::class.java)
            .map {Health.Builder().up().build()}
            .onErrorResume { ex: Throwable ->
                Mono.just(Health.Builder().down(ex).build())
            }
            .log()
    }
}