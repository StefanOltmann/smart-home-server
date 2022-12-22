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

package de.stefan_oltmann.smarthome.server.model

import com.fasterxml.jackson.annotation.JsonInclude

data class DeviceStateHistoryEntry(
    val deviceId: DeviceId,
    val timestampInMillis: Long,
    @JsonInclude(JsonInclude.Include.NON_NULL) val powerState: DevicePowerState? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val percentage: Int? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val currentTemperature: Double? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val targetTemperature: Double? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val windSpeed: Double? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val lightIntensity: Double? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val rainfall: Boolean? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val locked: Boolean? = null
)
