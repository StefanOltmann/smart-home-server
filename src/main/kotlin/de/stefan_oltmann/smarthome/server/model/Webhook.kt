package de.stefan_oltmann.smarthome.server.model

data class Webhook(
    val deviceId: DeviceId,
    val url: String
)
