package com.github.nayasis.kotlin.spring.extension.log

import ch.qos.logback.core.Context
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.spi.ContextAwareBase
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException

class NetworkAddressUtil(context: Context): ContextAwareBase() {

    init {
        setContext(context)
    }

    /**
     * Add the local host's name as a property
     */
    fun safelyGetLocalHostName(): String {
        try {
            return localHostName
        } catch (e: Exception) {
            addError("Failed to get local hostname", e)
        }
        return CoreConstants.UNKNOWN_LOCALHOST
    }

    fun safelyGetCanonicalLocalHostName(): String {
        try {
            return canonicalLocalHostName
        } catch (e: UnknownHostException) {
            addError("Failed to get canonical local hostname", e)
        } catch (e: SocketException) {
            addError("Failed to get canonical local hostname", e)
        } catch (e: SecurityException) {
            addError("Failed to get canonical local hostname", e)
        }
        return CoreConstants.UNKNOWN_LOCALHOST
    }

    companion object {
        @get:Throws(UnknownHostException::class, SocketException::class)
        val localHostName: String
            get() = try {
                val localhost = InetAddress.getLocalHost()
                localhost.hostName
            } catch (e: UnknownHostException) {
                localAddressAsString
            }

        @get:Throws(UnknownHostException::class, SocketException::class)
        val canonicalLocalHostName: String
            get() {
                return try {
                    val localhost = InetAddress.getLocalHost()
                    localhost.canonicalHostName
                } catch (e: UnknownHostException) {
                    localAddressAsString
                }
            }

        @get:Throws(UnknownHostException::class, SocketException::class)
        private val localAddressAsString: String
            get() {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces != null && interfaces.hasMoreElements()) {
                    val addresses = interfaces.nextElement().inetAddresses
                    while (addresses != null && addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (acceptableAddress(address)) {
                            return address.hostAddress
                        }
                    }
                }
                throw UnknownHostException()
            }

        private fun acceptableAddress(address: InetAddress?): Boolean {
            return address != null && !address.isLoopbackAddress && !address.isAnyLocalAddress && !address.isLinkLocalAddress
        }
    }

}