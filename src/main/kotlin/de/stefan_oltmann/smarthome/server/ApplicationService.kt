/*
 * Stefans Smart Home Project
 * Copyright (C) 2022 Stefan Oltmann
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
package de.stefan_oltmann.smarthome.server

import de.stefan_oltmann.smarthome.server.data.DeviceRepository
import de.stefan_oltmann.smarthome.server.data.DeviceStateRepository
import de.stefan_oltmann.smarthome.server.data.impl.FileDeviceRepository
import de.stefan_oltmann.smarthome.server.data.impl.FileDeviceStateRepository
import de.stefan_oltmann.smarthome.server.data.impl.FileWebhookRepository
import de.stefan_oltmann.smarthome.server.data.impl.INFLUX_FILE_NAME
import de.stefan_oltmann.smarthome.server.data.impl.InfluxDbDeviceStateRepository
import de.stefan_oltmann.smarthome.server.data.impl.InfluxDbDeviceStateRepository.InfluxDbSettings
import de.stefan_oltmann.smarthome.server.knx.KnxService
import de.stefan_oltmann.smarthome.server.knx.impl.KnxServiceImpl
import de.stefan_oltmann.smarthome.server.service.WebhookServiceImpl
import java.io.File
import javax.inject.Singleton

@Singleton
class ApplicationService {

    val deviceRepository: DeviceRepository

    val deviceStateRepository: DeviceStateRepository

    val knxService: KnxService?

    init {

        deviceRepository = FileDeviceRepository()

        val influxDbSettings = findInfluxDbSettings()

        /*
         * If InfluxDB settings are configured this should be used.
         */
        if (influxDbSettings != null) {

            logger.info("$INFLUX_FILE_NAME found. Will log to InfluxDB.")

            deviceStateRepository = InfluxDbDeviceStateRepository(influxDbSettings)

        } else
            deviceStateRepository = FileDeviceStateRepository()

        val webhookRepository = FileWebhookRepository()

        val webhookService = WebhookServiceImpl(webhookRepository)

        logger.info("Staring KNX service...")

        var knxServiceTemp: KnxService? = null

        try {

            knxServiceTemp = KnxServiceImpl(
                deviceRepository,
                deviceStateRepository,
                webhookService
            )

            /* Read all states initially */
            knxServiceTemp.readAllDeviceStates()

            logger.info("KNX service started.")

        } catch (ex: Exception) {
            logger.error("Error on starting KNX service.", ex)
        }

        knxService = knxServiceTemp
    }

    private fun findInfluxDbSettings(): InfluxDbSettings? {

        val influxConfFile = File(DATA_DIR_NAME, INFLUX_FILE_NAME)

        if (!influxConfFile.exists())
            return null

        try {

            val lines = influxConfFile.readLines()

            if (lines.size < 2) {
                logger.error("${influxConfFile.absolutePath} has less than 2 lines.")
                return null
            }

            val url = lines[0].trim()
            val token = lines[1].trim()

            if (url.isBlank() || token.isBlank()) {
                logger.error("URL and/or token are blank in ${influxConfFile.absolutePath}.")
                return null
            }

            return InfluxDbSettings(url, token)

        } catch (ex: Exception) {
            logger.error("Could not read ${influxConfFile.absolutePath}.", ex)
        }

        return null
    }
}
