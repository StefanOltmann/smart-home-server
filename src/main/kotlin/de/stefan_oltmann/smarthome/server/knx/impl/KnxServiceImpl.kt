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
package de.stefan_oltmann.smarthome.server.knx.impl

import de.stefan_oltmann.smarthome.server.DATA_DIR_NAME
import de.stefan_oltmann.smarthome.server.data.DeviceRepository
import de.stefan_oltmann.smarthome.server.data.DeviceStateRepository
import de.stefan_oltmann.smarthome.server.data.impl.FileDaikinRepository
import de.stefan_oltmann.smarthome.server.knx.KnxService
import de.stefan_oltmann.smarthome.server.model.Device
import de.stefan_oltmann.smarthome.server.model.DeviceId
import de.stefan_oltmann.smarthome.server.model.DevicePowerState
import de.stefan_oltmann.smarthome.server.model.GroupAddressType
import de.stefan_oltmann.smarthome.server.service.DaikinService
import de.stefan_oltmann.smarthome.server.service.DaikinServiceImpl
import de.stefan_oltmann.smarthome.server.service.WebhookService
import li.pitschmann.knx.core.address.GroupAddress
import li.pitschmann.knx.core.body.Body
import li.pitschmann.knx.core.body.TunnelingRequestBody
import li.pitschmann.knx.core.communication.DefaultKnxClient
import li.pitschmann.knx.core.communication.KnxClient
import li.pitschmann.knx.core.config.ConfigBuilder
import li.pitschmann.knx.core.config.CoreConfigs
import li.pitschmann.knx.core.datapoint.DPT1
import li.pitschmann.knx.core.datapoint.DPT5
import li.pitschmann.knx.core.datapoint.DPT9
import li.pitschmann.knx.core.exceptions.DataPointTypeIncompatibleBytesException
import li.pitschmann.knx.core.plugin.ObserverPlugin
import li.pitschmann.knx.core.plugin.audit.FileAuditPlugin
import li.pitschmann.knx.core.utils.Sleeper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import kotlin.concurrent.thread

const val KNX_AUDIT_FILE_NAME = "knx-audit.log"

class KnxServiceImpl(
    private val deviceRepository: DeviceRepository,
    deviceStateRepository: DeviceStateRepository,
    webhookService: WebhookService
) : KnxService {

    private var knxClient: KnxClient

    private val daikinService: DaikinService =
        DaikinServiceImpl(FileDaikinRepository())

    init {

        val groupMonitorPlugin = GroupMonitorPlugin(
            deviceRepository,
            deviceStateRepository,
            webhookService
        )

        val auditPlugin = FileAuditPlugin()

        val config = ConfigBuilder
            .tunneling()
            .setting(FileAuditPlugin.PATH, Paths.get("$DATA_DIR_NAME/$KNX_AUDIT_FILE_NAME"))
            .setting(CoreConfigs.Control.PORT, KNX_CONTROL_CHANNEL_PORT)
            .setting(CoreConfigs.Data.PORT, KNX_DATA_CHANNEL_PORT)
            .plugin(groupMonitorPlugin)
            .plugin(auditPlugin)
            .build()

        knxClient = DefaultKnxClient.createStarted(config)

        /*
         * Keep the KNX client running forever on a separate thread.
         */
        thread {

            while (true) {

                try {

                    knxClient.use { client ->

                        while (client.isRunning) {

                            Sleeper.seconds(60)

                            daikinService.updateStates(knxClient)
                        }
                    }

                } catch (ex: Exception) {
                    logger.error("Service stopped due to an exception. Will restart now.", ex)
                }

                /*
                 * If this code is reached the KNX client threw an
                 * exception, was likely not running anymore and
                 * needs a fresh start.
                 */
                Sleeper.seconds(1)

                logger.info("Restarting KNX client...")

                if (!knxClient.isRunning)
                    knxClient = DefaultKnxClient.createStarted(config)
            }
        }
    }

    override fun readAllDeviceStates() {

        /*
         * Sending a read request to all known devices will
         * trigger responses on the KNX bus which will get
         * caught by the group monitor.
         *
         * This must use the BLOCKING readRequest() (the one with timeout) since
         * otherwise there are to many requests at once and KNX IP will fail due to this.
         */

        val requestAllDevicesRunnable = Runnable {

            for (device in deviceRepository.devices) {

                device.gaPowerStateStatus?.let { groupAddress ->
                    knxClient.readRequest(GroupAddress.of(groupAddress), READ_TIMEOUT_MS)
                }

                device.gaPercentageStatus?.let { groupAddress ->
                    knxClient.readRequest(GroupAddress.of(groupAddress), READ_TIMEOUT_MS)
                }

                device.gaCurrentTemperature?.let { groupAddress ->
                    knxClient.readRequest(GroupAddress.of(groupAddress), READ_TIMEOUT_MS)
                }

                device.gaTargetTemperatureStatus?.let { groupAddress ->
                    knxClient.readRequest(GroupAddress.of(groupAddress), READ_TIMEOUT_MS)
                }

                device.gaWindSpeed?.let { groupAddress ->
                    knxClient.readRequest(GroupAddress.of(groupAddress), READ_TIMEOUT_MS)
                }

                device.gaLightIntensity?.let { groupAddress ->
                    knxClient.readRequest(GroupAddress.of(groupAddress), READ_TIMEOUT_MS)
                }

                device.gaRainfall?.let { groupAddress ->
                    knxClient.readRequest(GroupAddress.of(groupAddress), READ_TIMEOUT_MS)
                }

                device.gaLockObject?.let { groupAddress ->
                    knxClient.readRequest(GroupAddress.of(groupAddress), READ_TIMEOUT_MS)
                }

                /*
                 * With a short wait the chances are way better that we get
                 * all states in the end. If we spam the KNX bus most of the
                 * answers will be dropped.
                 */
                Thread.sleep(DELAY_BETWEEN_DEVICES_MS)
            }
        }

        Thread(requestAllDevicesRunnable).start()
    }

    override fun writePowerState(device: Device, powerState: DevicePowerState) {

        knxClient.writeRequest(
            GroupAddress.of(device.gaPowerStateWrite),
            DPT1.BOOL.of(powerState == DevicePowerState.ON)
        )
    }

    override fun writePercentage(device: Device, percentage: Int) {

        knxClient.writeRequest(
            GroupAddress.of(device.gaPercentageWrite),
            DPT5.SCALING.of(percentage)
        )
    }

    override fun writeTargetTemperature(device: Device, temperature: Double) {

        knxClient.writeRequest(
            GroupAddress.of(device.gaTargetTemperatureWrite),
            DPT9.TEMPERATURE.of(temperature)
        )
    }

    class GroupMonitorPlugin(
        private val deviceRepository: DeviceRepository,
        private val deviceStateRepository: DeviceStateRepository,
        private val webhookService: WebhookService
    ) : ObserverPlugin {

        private val logger: Logger = LoggerFactory.getLogger(GroupMonitorPlugin::class.java)

        override fun onInitialization(client: KnxClient) {
            logger.info("Initialized by client: $client")
        }

        override fun onIncomingBody(item: Body) {

            /*
             * Only look for this status updates and ignore other service types
             * (e.g. search, description, connect, disconnect)
             */
            if (item !is TunnelingRequestBody)
                return

            /*
             * Ignore read requests. We want status updates.
             */
            if (item.cemi.data.isEmpty())
                return

            handleItem(item)
        }

        override fun onOutgoingBody(item: Body) {
            /* We only want to handle incoming bodies. */
        }

        private fun handleItem(item: TunnelingRequestBody) {

            val groupAddress = item.cemi.destinationAddress as GroupAddress

            /*
             * Find the corresponding device in the registry/repository or ignore this event.
             */
            val deviceAndType = deviceRepository.findDeviceAndType(groupAddress.addressLevel3)
                ?: return

            val device = deviceAndType.first
            val type = deviceAndType.second

            when (type) {
                GroupAddressType.POWER_STATE_STATUS -> handlePowerStateStatusItem(
                    item,
                    device.id,
                    groupAddress
                )

                GroupAddressType.PERCENTAGE_STATUS -> handlePercentageStatusItem(
                    item,
                    device.id,
                    groupAddress
                )

                GroupAddressType.CURRENT_TEMPERATURE -> handleCurrentTemperatureItem(
                    item,
                    device.id,
                    groupAddress
                )

                GroupAddressType.TARGET_TEMPERATURE_STATUS -> handleTargetTemperatureItem(
                    item,
                    device.id,
                    groupAddress
                )

                GroupAddressType.WIND_SPEED -> handleWindSpeedItem(
                    item,
                    device.id,
                    groupAddress
                )

                GroupAddressType.LIGHT_INTENSITY -> handleLightIntensityItem(
                    item,
                    device.id,
                    groupAddress
                )

                GroupAddressType.RAINFALL -> handleRainfallItem(
                    item,
                    device.id,
                    groupAddress
                )

                GroupAddressType.LOCK_OBJECT -> handleLockObjectItem(
                    item,
                    device.id,
                    groupAddress
                )

                else -> return
            }
        }

        private fun handlePowerStateStatusItem(
            item: TunnelingRequestBody,
            deviceId: DeviceId,
            groupAddress: GroupAddress
        ) {

            try {

                val value = DPT1.BOOL.of(item.cemi.data).value

                val powerState = if (value) DevicePowerState.ON else DevicePowerState.OFF

                deviceStateRepository.updatePowerState(deviceId, powerState)

                /*
                 * Only trigger if an alarm becomes active.
                 */
                if (powerState == DevicePowerState.ON)
                    webhookService.triggerWebhook(deviceId)

            } catch (ex: DataPointTypeIncompatibleBytesException) {
                logger.error(createWrongTypeMessage(groupAddress, item, "DPT1"), ex)
            }
        }

        private fun handlePercentageStatusItem(
            item: TunnelingRequestBody,
            deviceId: DeviceId,
            groupAddress: GroupAddress
        ) {

            try {

                val percentage = DPT5.PERCENT.of(item.cemi.data).value / 255.0 * 100

                deviceStateRepository.updatePercentage(deviceId, percentage.toInt())

            } catch (ex: DataPointTypeIncompatibleBytesException) {
                logger.error(createWrongTypeMessage(groupAddress, item, "DPT5"), ex)
            }
        }

        private fun handleCurrentTemperatureItem(
            item: TunnelingRequestBody,
            deviceId: DeviceId,
            groupAddress: GroupAddress
        ) {

            try {

                val temperature = DPT9.TEMPERATURE.of(item.cemi.data).value

                deviceStateRepository.updateCurrentTemperature(deviceId, temperature)

            } catch (ex: DataPointTypeIncompatibleBytesException) {
                logger.error(createWrongTypeMessage(groupAddress, item, "DPT9"), ex)
            }
        }

        private fun handleTargetTemperatureItem(
            item: TunnelingRequestBody,
            deviceId: DeviceId,
            groupAddress: GroupAddress
        ) {

            try {

                val temperature = DPT9.TEMPERATURE.of(item.cemi.data).value

                deviceStateRepository.updateTargetTemperature(deviceId, temperature)

            } catch (ex: DataPointTypeIncompatibleBytesException) {
                logger.error(createWrongTypeMessage(groupAddress, item, "DPT9"), ex)
            }
        }

        private fun handleWindSpeedItem(
            item: TunnelingRequestBody,
            deviceId: DeviceId,
            groupAddress: GroupAddress
        ) {

            try {

                val windSpeed = DPT9.WIND_SPEED.of(item.cemi.data).value

                deviceStateRepository.updateWindSpeed(deviceId, windSpeed)

            } catch (ex: DataPointTypeIncompatibleBytesException) {
                logger.error(createWrongTypeMessage(groupAddress, item, "DPT9"), ex)
            }
        }

        private fun handleLightIntensityItem(
            item: TunnelingRequestBody,
            deviceId: DeviceId,
            groupAddress: GroupAddress
        ) {

            try {

                val lightIntensity = DPT9.LUMINOUS_FLUX.of(item.cemi.data).value

                deviceStateRepository.updateLightIntensity(deviceId, lightIntensity)

            } catch (ex: DataPointTypeIncompatibleBytesException) {
                logger.error(createWrongTypeMessage(groupAddress, item, "DPT9"), ex)
            }
        }

        private fun handleRainfallItem(
            item: TunnelingRequestBody,
            deviceId: DeviceId,
            groupAddress: GroupAddress
        ) {

            try {

                val rainfall = DPT1.BOOL.of(item.cemi.data).value

                deviceStateRepository.updateRainfall(deviceId, rainfall)

            } catch (ex: DataPointTypeIncompatibleBytesException) {
                logger.error(createWrongTypeMessage(groupAddress, item, "DPT1"), ex)
            }
        }

        private fun handleLockObjectItem(
            item: TunnelingRequestBody,
            deviceId: DeviceId,
            groupAddress: GroupAddress
        ) {

            try {

                val locked = DPT1.BOOL.of(item.cemi.data).value

                deviceStateRepository.updateLockObject(deviceId, locked)

            } catch (ex: DataPointTypeIncompatibleBytesException) {
                logger.error(createWrongTypeMessage(groupAddress, item, "DPT1"), ex)
            }
        }

        override fun onError(throwable: Throwable) {
            logger.error("KNX client error.", throwable)
        }

        private fun createWrongTypeMessage(
            groupAddress: GroupAddress,
            item: TunnelingRequestBody,
            expectedType: String
        ) = "GA ${groupAddress.addressLevel3} is not $expectedType: ${item.cemi.data.toHex()}"
    }

    companion object {

        const val DELAY_BETWEEN_DEVICES_MS = 200L
        const val READ_TIMEOUT_MS = 3000L

        /*
         * Fixed ports to be clear what needs to be exposed in Docker
         */
        const val KNX_CONTROL_CHANNEL_PORT = 50_011
        const val KNX_DATA_CHANNEL_PORT = 50_012

        val logger: Logger = LoggerFactory.getLogger(KnxServiceImpl::class.java)

    }
}
