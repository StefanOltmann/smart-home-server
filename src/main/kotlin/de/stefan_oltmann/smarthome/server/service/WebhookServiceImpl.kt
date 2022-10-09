package de.stefan_oltmann.smarthome.server.service

import de.stefan_oltmann.smarthome.server.data.WebhookRepository
import de.stefan_oltmann.smarthome.server.model.DeviceId
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

class WebhookServiceImpl(
    private val webhookRepository: WebhookRepository
) : WebhookService {

    private val httpClient: OkHttpClient = OkHttpClient()

    override fun triggerWebhook(deviceId: DeviceId) {

        val webhooks = webhookRepository.findForDevice(deviceId)

        for (webhook in webhooks) {

            val url = URL(webhook.url)

            val request = Request.Builder().url(url).build()

            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful)
                logger.info("Called ${webhook.url}")
            else
                logger.error("Failed to call ${webhook.url}")
        }
    }

    companion object {

        val logger: Logger = LoggerFactory.getLogger(WebhookServiceImpl::class.java)

    }
}
