package de.stefan_oltmann.smarthome.server.service

import de.stefan_oltmann.smarthome.server.model.DeviceId

interface WebhookService  {

    fun triggerWebhook(deviceId: DeviceId)

}
