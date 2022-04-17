/*
 * Stefans Smart Home Project
 * Copyright (C) 2021 Stefan Oltmann
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

package de.stefan_oltmann.smarthome.server.data.impl

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.stefan_oltmann.smarthome.server.DATA_DIR_NAME
import de.stefan_oltmann.smarthome.server.data.WebhookRepository
import de.stefan_oltmann.smarthome.server.model.Device
import de.stefan_oltmann.smarthome.server.model.DeviceId
import de.stefan_oltmann.smarthome.server.model.Webhook
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.Exception
import java.net.http.WebSocketHandshakeException
import kotlin.math.log

const val WEBHOOKS_FILE_NAME = "webhooks.json"

class FileWebhookRepository : WebhookRepository {

    override val webhooks: Set<Webhook>

    private val gson by lazy {

        val builder = GsonBuilder()
        builder.setPrettyPrinting()
        builder.create()
    }

    init {

        val webhooksFile = File(DATA_DIR_NAME, WEBHOOKS_FILE_NAME)

        if (webhooksFile.exists()) {

            var parsedWebhooks: Set<Webhook>

            try {

                val listType = object : TypeToken<HashSet<Webhook>>() {}.type

                parsedWebhooks = gson.fromJson(webhooksFile.readText(), listType)

                logger.info("Started with ${parsedWebhooks.size} webhooks from '${webhooksFile.absolutePath}'.")

            } catch (ex: Exception) {

                parsedWebhooks = emptySet()

                logger.error("Could not parse '${webhooksFile.absolutePath}'", ex)
            }

            webhooks = parsedWebhooks

        } else {

            webhooks = emptySet()

            logger.error("Did not find file '${webhooksFile.absolutePath}'. Using empty webhook list.")
        }
    }

    override fun findForDevice(deviceId: DeviceId): Set<Webhook> {

        return webhooks.filter {
            it.deviceId == deviceId
        }.toSet()
    }

    companion object {

        val logger: Logger = LoggerFactory.getLogger(FileWebhookRepository::class.java)

    }
}
