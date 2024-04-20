/*
 * Stefans Smart Home Project
 * Copyright (C) 2024 Stefan Oltmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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

        try {

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

        } catch (ex: Exception) {
            logger.error("Triggering WebHooks failed.")
        }
    }

    companion object {

        val logger: Logger = LoggerFactory.getLogger(WebhookServiceImpl::class.java)

    }
}
