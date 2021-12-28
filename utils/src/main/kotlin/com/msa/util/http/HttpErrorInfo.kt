package com.msa.util.http

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime


class HttpErrorInfo(
    var timestamp: ZonedDateTime,
    var path: String,
    private var httpStatus: HttpStatus,
    var message: String
) {

    constructor(httpStatus: HttpStatus, path: String, message: String) : this(
        ZonedDateTime.now(),
        path,
        httpStatus,
        message
    )


    val status: Int
        get() = httpStatus!!.value()
    val error: String
        get() = httpStatus!!.reasonPhrase
}