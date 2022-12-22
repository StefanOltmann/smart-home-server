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
package de.stefan_oltmann.smarthome.server.data

import de.stefan_oltmann.smarthome.server.model.DeviceId
import de.stefan_oltmann.smarthome.server.model.DevicePowerState
import de.stefan_oltmann.smarthome.server.model.DeviceState
import de.stefan_oltmann.smarthome.server.model.DeviceStateHistoryEntry

interface DeviceStateRepository {

    /**
     * Collection of the current state of each device.
     */
    val states: Collection<DeviceState>

    /**
     * Map of a timestamp (Millis as Long) to a device state.
     */
    val history: List<DeviceStateHistoryEntry>

    fun updatePowerState(deviceId: DeviceId, powerState: DevicePowerState)

    fun updatePercentage(deviceId: DeviceId, percentage: Int)

    fun updateCurrentTemperature(deviceId: DeviceId, temperature: Double)

    fun updateTargetTemperature(deviceId: DeviceId, temperature: Double)

    fun updateWindSpeed(deviceId: DeviceId, windSpeed: Double)

    fun updateLightIntensity(deviceId: DeviceId, lightIntensity: Double)

    fun updateRainfall(deviceId: DeviceId, rainfall: Boolean)

    fun updateLockObject(deviceId: DeviceId, locked: Boolean)

}
