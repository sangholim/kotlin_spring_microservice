package com.msa.util.http

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.InetAddress

@Component
class ServiceUtil(
    @Value("\${server.port}")
    var port: String = ""
) {
    fun getServiceAddress(): String {
        var address = InetAddress.getLocalHost()
        return "${address.hostName}/${address.hostAddress}:$port"
    }
}