/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.models

enum class Protocol(val value: String) {
    TCP("TCP"),
    UDP("UDP"),
    IKEV2("IKEv2"),
    WIREGUARD("WIREGUARD");

    companion object {
        @JvmStatic
        fun fromString(protocol: String): Protocol =
                values().find { value -> value.value.toLowerCase() == protocol.toLowerCase() }
                        ?: TCP
    }
}