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

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.client.write.Point
import de.stefan_oltmann.smarthome.server.data.DeviceStateRepository
import de.stefan_oltmann.smarthome.server.model.DeviceId
import de.stefan_oltmann.smarthome.server.model.DevicePowerState
import de.stefan_oltmann.smarthome.server.model.DeviceState
import de.stefan_oltmann.smarthome.server.model.DeviceStateHistoryEntry
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

const val INFLUX_FILE_NAME = "influxdb.ini"
const val INFLUX_ORG_NAME = "smarthome"
const val INFLUX_BUCKET_NAME = "SmartHome"

/**
 * Class acting as a container for multiple DeviceStatus and managing updates to them.
 */
class InfluxDbDeviceStateRepository(
    influxDbSettings: InfluxDbSettings
) : DeviceStateRepository {

    /**
     * Internal mutable map of the current state of each device.
     */
    private val _states = mutableMapOf<DeviceId, DeviceState>()

    private val _history = mutableListOf<DeviceStateHistoryEntry>()

    /**
     * Collection of the current state of each device.
     */
    override val states: Collection<DeviceState>
        get() = _states.values

    /**
     * Map of a timestamp (Millis as Long) to a device state.
     */
    override val history: List<DeviceStateHistoryEntry>
        get() = _history

    private val client: InfluxDBClientKotlin

    init {

        logger.info("Connecting to InfluxDB ${influxDbSettings.url} ...")

        client = InfluxDBClientKotlinFactory.create(
            url = influxDbSettings.url,
            token = influxDbSettings.token.toCharArray(),
            org = INFLUX_ORG_NAME,
            bucket = INFLUX_BUCKET_NAME
        )
    }

    private fun getOrCreateDeviceStatus(deviceId: DeviceId): DeviceState {

        var deviceState = _states[deviceId]

        if (deviceState != null)
            return deviceState

        deviceState = DeviceState(deviceId)

        _states[deviceId] = deviceState

        return deviceState
    }

    override fun updatePowerState(deviceId: DeviceId, powerState: DevicePowerState) {

        getOrCreateDeviceStatus(deviceId).powerState = powerState

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, powerState = powerState))

        /* Persist */

        val point = Point.measurement(deviceId.value)
            .addField("powerState", powerState.asInt())
            .time(Instant.now().toEpochMilli(), WritePrecision.MS)

        runBlocking { client.getWriteKotlinApi().writePoint(point) }
    }

    override fun updatePercentage(deviceId: DeviceId, percentage: Int) {

        getOrCreateDeviceStatus(deviceId).percentage = percentage

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, percentage = percentage))

        /* Persist */

        val point = Point.measurement(deviceId.value)
            .addField("percentage", percentage)
            .time(Instant.now().toEpochMilli(), WritePrecision.MS)

        runBlocking { client.getWriteKotlinApi().writePoint(point) }
    }

    override fun updateCurrentTemperature(deviceId: DeviceId, temperature: Double) {

        getOrCreateDeviceStatus(deviceId).currentTemperature = temperature

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, currentTemperature = temperature))

        /* Persist */

        val point = Point.measurement(deviceId.value)
            .addField("currentTemperature", temperature)
            .time(Instant.now().toEpochMilli(), WritePrecision.MS)

        runBlocking { client.getWriteKotlinApi().writePoint(point) }
    }

    override fun updateTargetTemperature(deviceId: DeviceId, temperature: Double) {

        getOrCreateDeviceStatus(deviceId).targetTemperature = temperature

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, targetTemperature = temperature))

        /* Persist */

        val point = Point.measurement(deviceId.value)
            .addField("targetTemperature", temperature)
            .time(Instant.now().toEpochMilli(), WritePrecision.MS)

        runBlocking { client.getWriteKotlinApi().writePoint(point) }
    }

    override fun updateLockObject(deviceId: DeviceId, locked: Boolean) {

        getOrCreateDeviceStatus(deviceId).locked = locked

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, locked = locked))

        /* Persist */

        val point = Point.measurement(deviceId.value)
            .addField("locked", locked)
            .time(Instant.now().toEpochMilli(), WritePrecision.MS)

        runBlocking { client.getWriteKotlinApi().writePoint(point) }
    }

    data class InfluxDbSettings(val url: String, val token: String)

    companion object {

        val logger: Logger = LoggerFactory.getLogger(InfluxDbDeviceStateRepository::class.java)

    }
}
