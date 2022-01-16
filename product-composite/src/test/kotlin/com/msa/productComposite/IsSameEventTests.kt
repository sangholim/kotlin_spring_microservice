package com.msa.productComposite

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.domain.event.Event
import com.msa.domain.product.vo.Product
import com.msa.productComposite.IsSameEvent.Companion.sameEventExceptCreatedAt
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test


class IsSameEventTests {
    var mapper = ObjectMapper()
    @Test
    @Throws(JsonProcessingException::class)
    fun testEventObjectCompare() {

        // Event #1 and #2 are the same event, but occurs as different times
        // Event #3 and #4 are different events
        val event1: Event<Int, Product> = Event(Event.Type.CREATE, 1, Product(1, "name", 1, ""))
        val event2: Event<Int, Product> = Event(Event.Type.CREATE, 1, Product(1, "name", 1, ""))
        val event3: Event<Int, Product> = Event(Event.Type.DELETE, 1, null)
        val event4: Event<Int, Product> = Event(Event.Type.CREATE, 1, Product(2, "name", 1, ""))
        val event1JSon = mapper.writeValueAsString(event1)
        assertThat(event1JSon, `is`(sameEventExceptCreatedAt(event2)))
        assertThat(event1JSon, not(sameEventExceptCreatedAt(event3)))
        assertThat(event1JSon, not(sameEventExceptCreatedAt(event4)))
    }
}