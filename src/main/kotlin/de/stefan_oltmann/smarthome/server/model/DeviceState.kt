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

/**
 * Class representing the power state and percentage.
 */
class DeviceState(val deviceId: DeviceId) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var powerState: DevicePowerState? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var percentage: Int? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var currentTemperature: Double? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var targetTemperature: Double? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var windSpeed: Double? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var lightIntensity: Double? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var rainfall: Boolean? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var locked: Boolean? = null

}
