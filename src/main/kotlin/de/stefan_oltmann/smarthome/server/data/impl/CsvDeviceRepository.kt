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
import de.stefan_oltmann.smarthome.server.data.DeviceRepository
import de.stefan_oltmann.smarthome.server.model.Device
import de.stefan_oltmann.smarthome.server.model.DeviceId
import de.stefan_oltmann.smarthome.server.model.GroupAddressType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

const val DEVICES_FILE_NAME = "devices.json"

class CsvDeviceRepository : DeviceRepository {

    override val devices: List<Device>

    private val gson by lazy {

        val builder = GsonBuilder()
        builder.setPrettyPrinting()
        builder.create()
    }

    init {

        val devicesFile = File(DATA_DIR_NAME, DEVICES_FILE_NAME)

        if (devicesFile.exists()) {

            val listType = object : TypeToken<ArrayList<Device>>() {}.type

            devices = gson.fromJson(devicesFile.readText(), listType)

            logger.info("Started with ${devices.size} devices from '${devicesFile.absolutePath}'.")

        } else {

            devices = emptyList()

            logger.error("Did not find file '${devicesFile.absolutePath}'. Using empty devices list.")
        }
    }

    override fun findById(deviceId: DeviceId): Device? {

        for (device in devices)
            if (device.id == deviceId)
                return device

        return null
    }

    override fun findDeviceAndType(groupAddress: String): Pair<Device, GroupAddressType>? {

        for (device in devices) {

            when (groupAddress) {
                device.gaPowerStateWrite -> return device to GroupAddressType.POWER_STATE_WRITE
                device.gaPowerStateStatus -> return device to GroupAddressType.POWER_STATE_STATUS
                device.gaPercentageWrite -> return device to GroupAddressType.PERCENTAGE_WRITE
                device.gaPercentageStatus -> return device to GroupAddressType.PERCENTAGE_STATUS
                device.gaCurrentTemperature -> return device to GroupAddressType.CURRENT_TEMPERATURE
                device.gaTargetTemperature -> return device to GroupAddressType.TARGET_TEMPERATURE
            }
        }

        return null
    }

    companion object {

        val logger: Logger = LoggerFactory.getLogger(CsvDeviceRepository::class.java)

    }
}
