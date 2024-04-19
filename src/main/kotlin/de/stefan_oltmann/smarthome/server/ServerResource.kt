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
package de.stefan_oltmann.smarthome.server

import de.stefan_oltmann.smarthome.server.model.*
import io.quarkus.runtime.Startup
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

val DEVICE_UPDATED_RESPONSE: Response =
    Response.ok().entity("Device updated.").build()

val DEVICE_DOES_NOT_EXIST_RESPONSE: Response =
    Response.status(Response.Status.BAD_REQUEST).entity("Device does not exist.").build()

val KNX_SERVICE_NOT_AVAILABLE_RESPONSE: Response =
    Response.serverError().entity("KNX service not available.").build()

@Startup
@Path("/")
class ServerResource {

    @Inject
    lateinit var service: ApplicationService

    @GET
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
    fun findAllDevices(): List<Device> {

        try {

            logger.info("[API] | Requesting device list...")

            return service.deviceRepository.devices

        } catch (ex: Exception) {
            logger.error("Requesting device list failed.", ex)
            throw ex
        }
    }

    @GET
    @Path("/devices/current-states")
    @Produces(MediaType.APPLICATION_JSON)
    fun findAllDeviceStates(): Collection<DeviceState> {

        try {

            logger.info("[API] | Requesting current device states...")

            return service.deviceStateRepository.states

        } catch (ex: Exception) {
            logger.error("Requesting device states failed.", ex)
            throw ex
        }
    }

    @GET
    @Path("/devices/state-history")
    @Produces(MediaType.APPLICATION_JSON)
    fun findDeviceStateHistory(): List<DeviceStateHistoryEntry> {

        try {

            logger.info("[API] | Requesting device state history...")

            return service.deviceStateRepository.history

        } catch (ex: Exception) {
            logger.error("Requesting device state history failed.", ex)
            throw ex
        }
    }

    @GET
    @Path("/device/{deviceId}/set/power-state/value/{powerState}")
    fun setDevicePowerState(
        @PathParam("deviceId") deviceIdString: String,
        @PathParam("powerState") powerStateString: String
    ): Response {

        val deviceId = DeviceId(deviceIdString)
        val powerState: DevicePowerState = DevicePowerState.valueOf(powerStateString)

        logger.info("[API] | SET ${deviceId.value} TO $powerState")

        val device = service.deviceRepository.findById(deviceId)
            ?: return DEVICE_DOES_NOT_EXIST_RESPONSE

        service.knxService?.let { knxService ->

            /* Write to KNX bus first. */
            knxService.writePowerState(device, powerState)

            /* Update local object after the successful write to KNX bus. */
            service.deviceStateRepository.updatePowerState(device.id, powerState)

            return DEVICE_UPDATED_RESPONSE

        } ?: return KNX_SERVICE_NOT_AVAILABLE_RESPONSE
    }

    @GET
    @Path("/device/{deviceId}/set/percentage/value/{percentage}")
    fun setDevicePercentage(
        @PathParam("deviceId") deviceIdString: String,
        @PathParam("percentage") percentage: Int
    ): Response {

        val deviceId = DeviceId(deviceIdString)

        logger.info("[API] | SET ${deviceId.value} TO $percentage%")

        val device = service.deviceRepository.findById(deviceId)
            ?: return DEVICE_DOES_NOT_EXIST_RESPONSE

        service.knxService?.let { knxService ->

            /* Write to KNX bus first. */
            knxService.writePercentage(device, percentage)

            /* Update local object after the successful write to KNX bus. */
            service.deviceStateRepository.updatePercentage(device.id, percentage)

            return DEVICE_UPDATED_RESPONSE

        } ?: return KNX_SERVICE_NOT_AVAILABLE_RESPONSE
    }

    @GET
    @Path("/device/{deviceId}/set/target-temperature/value/{temperature}")
    fun setDeviceTargetTemperature(
        @PathParam("deviceId") deviceIdString: String,
        @PathParam("temperature") temperature: Double
    ): Response {

        val deviceId = DeviceId(deviceIdString)

        logger.info("[API] | SET ${deviceId.value} TO $temperature °C")

        val device = service.deviceRepository.findById(deviceId)
            ?: return DEVICE_DOES_NOT_EXIST_RESPONSE

        service.knxService?.let { knxService ->

            /* Write to KNX bus first. */
            knxService.writeTargetTemperature(device, temperature)

            /* Update local object after the successful write to KNX bus. */
            service.deviceStateRepository.updateTargetTemperature(device.id, temperature)

            return DEVICE_UPDATED_RESPONSE

        } ?: return KNX_SERVICE_NOT_AVAILABLE_RESPONSE
    }
}
