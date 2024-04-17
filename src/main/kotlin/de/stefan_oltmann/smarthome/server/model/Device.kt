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

package de.stefan_oltmann.smarthome.server.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Class representing a KNX device.
 */
data class Device(
    val id: DeviceId,
    val name: String,
    val type: DeviceType,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaPowerStateWrite: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaPowerStateStatus: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaPercentageWrite: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaPercentageStatus: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaCurrentTemperature: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaTargetTemperatureWrite: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaTargetTemperatureStatus: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaWindSpeed: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaLightIntensity: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaRainfall: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL) val gaLockObject: String? = null
)
