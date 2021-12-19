package com.msa.util.http

import org.springframework.http.HttpStatus
import java.time.Instant

class HttpErrorInfo(
    var path: String = "",
    val httpStatus: HttpStatus,
    var message: String = ""
) {
    val timestamp: Instant = Instant.now()

    fun getStatus() = httpStatus.value()
    fun getError() = httpStatus.reasonPhrase
}