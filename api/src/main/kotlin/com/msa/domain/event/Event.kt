package com.msa.domain.event

import java.time.LocalDateTime
import java.time.LocalDateTime.now


class Event<K, T>(
    var eventType: Type,
    var key: K,
    var data: T?,
    var eventCreatedAt: LocalDateTime = now()
) {
    enum class Type {
        CREATE, DELETE
    }
}