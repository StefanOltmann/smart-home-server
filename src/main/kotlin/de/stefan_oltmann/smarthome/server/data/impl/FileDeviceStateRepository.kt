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

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import de.stefan_oltmann.smarthome.server.DATA_DIR_NAME
import de.stefan_oltmann.smarthome.server.data.DeviceStateRepository
import de.stefan_oltmann.smarthome.server.model.DeviceId
import de.stefan_oltmann.smarthome.server.model.DevicePowerState
import de.stefan_oltmann.smarthome.server.model.DeviceState
import de.stefan_oltmann.smarthome.server.model.DeviceStateHistoryEntry

private const val HISTORY_CSV = "device_state_history.csv"

/**
 * Class acting as a container for multiple DeviceStatus and managing updates to them.
 */
class FileDeviceStateRepository : DeviceStateRepository {

    private val csvWriter = csvWriter {
        charset = "ISO_8859_1"
        delimiter = ';'
        lineTerminator = "\r\n"
    }

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
        csvWriter.writeAll(
            rows = listOf(listOf(deviceId.value, millis, "powerState", powerState.asInt())),
            targetFileName = "$DATA_DIR_NAME/$HISTORY_CSV",
            append = true
        )
    }

    override fun updatePercentage(deviceId: DeviceId, percentage: Int) {

        getOrCreateDeviceStatus(deviceId).percentage = percentage

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, percentage = percentage))

        /* Persist */
        csvWriter.writeAll(
            rows = listOf(listOf(deviceId.value, millis, "percentage", percentage)),
            targetFileName = "$DATA_DIR_NAME/$HISTORY_CSV",
            append = true
        )
    }

    override fun updateCurrentTemperature(deviceId: DeviceId, temperature: Double) {

        getOrCreateDeviceStatus(deviceId).currentTemperature = temperature

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, currentTemperature = temperature))

        /* Persist */
        csvWriter.writeAll(
            rows = listOf(listOf(deviceId.value, millis, "currentTemperature", temperature)),
            targetFileName = "$DATA_DIR_NAME/$HISTORY_CSV",
            append = true
        )
    }

    override fun updateTargetTemperature(deviceId: DeviceId, temperature: Double) {

        getOrCreateDeviceStatus(deviceId).targetTemperature = temperature

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, targetTemperature = temperature))

        /* Persist */
        csvWriter.writeAll(
            rows = listOf(listOf(deviceId.value, millis, "targetTemperature", temperature)),
            targetFileName = "$DATA_DIR_NAME/$HISTORY_CSV",
            append = true
        )
    }

    override fun updateWindSpeed(deviceId: DeviceId, windSpeed: Double) {

        getOrCreateDeviceStatus(deviceId).windSpeed = windSpeed

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, windSpeed = windSpeed))

        /* Persist */
        csvWriter.writeAll(
            rows = listOf(listOf(deviceId.value, millis, "windSpeed", windSpeed)),
            targetFileName = "$DATA_DIR_NAME/$HISTORY_CSV",
            append = true
        )
    }

    override fun updateLightIntensity(deviceId: DeviceId, lightIntensity: Double) {

        getOrCreateDeviceStatus(deviceId).lightIntensity = lightIntensity

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, lightIntensity = lightIntensity))

        /* Persist */
        csvWriter.writeAll(
            rows = listOf(listOf(deviceId.value, millis, "lightIntensity", lightIntensity)),
            targetFileName = "$DATA_DIR_NAME/$HISTORY_CSV",
            append = true
        )
    }

    override fun updateRainfall(deviceId: DeviceId, rainfall: Boolean) {

        getOrCreateDeviceStatus(deviceId).rainfall = rainfall

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, rainfall = rainfall))

        /* Persist */
        csvWriter.writeAll(
            rows = listOf(listOf(deviceId.value, millis, "rainfall", if (rainfall) 1 else 0)),
            targetFileName = "$DATA_DIR_NAME/$HISTORY_CSV",
            append = true
        )
    }

    override fun updateLockObject(deviceId: DeviceId, locked: Boolean) {

        getOrCreateDeviceStatus(deviceId).locked = locked

        val millis = System.currentTimeMillis()

        /* Write local object */
        _history.add(DeviceStateHistoryEntry(deviceId, millis, locked = locked))

        /* Persist */
        csvWriter.writeAll(
            rows = listOf(listOf(deviceId.value, millis, "locked", if (locked) 1 else 0)),
            targetFileName = "$DATA_DIR_NAME/$HISTORY_CSV",
            append = true
        )
    }
}
