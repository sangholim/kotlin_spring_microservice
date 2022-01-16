package com.msa.productComposite

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.msa.domain.event.Event
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException


class IsSameEvent(
    private var expectedEvent: Event<*, *>
) : TypeSafeMatcher<String>() {
    private val mapper = ObjectMapper()

    override fun matchesSafely(eventAsJson: String): Boolean {
        if (expectedEvent == null) return false
        LOG.trace("Convert the following json string to a map: {}", eventAsJson)
        val mapEvent = convertJsonStringToMap(eventAsJson)
        mapEvent.remove("eventCreatedAt")
        val mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent)
        LOG.trace("Got the map: {}", mapEvent)
        LOG.trace("Compare to the expected map: {}", mapExpectedEvent)
        return mapEvent == mapExpectedEvent
    }

    override fun describeTo(description: Description) {
        val expectedJson = convertObjectToJsonString(expectedEvent)
        description.appendText("expected to look like $expectedJson")
    }

    private fun getMapWithoutCreatedAt(event: Event<*, *>): MutableMap<*, *> {
        val mapEvent = convertObjectToMap(event)

        mapEvent.remove("eventCreatedAt")
        return mapEvent
    }

    private fun convertObjectToMap(`object`: Any): MutableMap<*, *> {
        val node: JsonNode = mapper.convertValue(`object`, JsonNode::class.java)
        return mapper.convertValue(node, MutableMap::class.java)
    }

    private fun convertObjectToJsonString(`object`: Any?): String {
        return try {
            mapper.writeValueAsString(`object`)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    private fun convertJsonStringToMap(eventAsJson: String): MutableMap<*, *> {
        return try {
            mapper.readValue<MutableMap<*, *>>(eventAsJson)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(IsSameEvent::class.java)
        fun sameEventExceptCreatedAt(expectedEvent: Event<*, *>): Matcher<String> {
            return IsSameEvent(expectedEvent)
        }
    }
}