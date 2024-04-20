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
package de.stefan_oltmann.smarthome.server.service

import de.stefan_oltmann.smarthome.server.data.DaikinRepository
import de.stefan_oltmann.smarthome.server.data.WebhookRepository
import de.stefan_oltmann.smarthome.server.model.DeviceId
import io.quarkus.logging.Log
import kotlinx.coroutines.delay
import li.pitschmann.knx.core.address.GroupAddress
import li.pitschmann.knx.core.communication.KnxClient
import li.pitschmann.knx.core.datapoint.DPT9
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import kotlin.concurrent.thread

class DaikinServiceImpl(
    private val daikinRepository: DaikinRepository
) : DaikinService {

    private val httpClient: OkHttpClient = OkHttpClient()

    override fun updateStates(knxClient: KnxClient) {

        try {

            for (daikin in daikinRepository.daikins) {

                val url = URL("http://${daikin.ip}/aircon/get_sensor_info")

                val request = Request.Builder().url(url).build()

                val response = httpClient.newCall(request).execute()

                /* Example: "ret=OK,htemp=20.0,hhum=-,otemp=6.0,err=0,cmpfreq=0,mompow=1" */
                val returnString = response.body()?.bytes()?.decodeToString()

                if (returnString == null) {
                    Log.info("Skip Daikin ${daikin.ip} as we did not receive wanted response.")
                    continue
                }

                for (part in returnString.split(',')) {

                    if (part.startsWith(DAIKIN_HOUSE_TEMP_KEY)) {

                        val temp = part.substring(DAIKIN_HOUSE_TEMP_KEY.length)

                        knxClient.writeRequest(
                            GroupAddress.of(daikin.gaHouseTemp),
                            DPT9.TEMPERATURE.of(temp.toDouble())
                        )
                    }

                    if (part.startsWith(DAIKIN_OUTER_TEMP_KEY)) {

                        val temp = part.substring(DAIKIN_OUTER_TEMP_KEY.length)

                        knxClient.writeRequest(
                            GroupAddress.of(daikin.gaOuterTemp),
                            DPT9.TEMPERATURE.of(temp.toDouble())
                        )
                    }
                }
            }

        } catch (ex: Exception) {
            logger.error("Updating Daikin states failed.", ex)
        }
    }

    companion object {

        val logger: Logger = LoggerFactory.getLogger(DaikinServiceImpl::class.java)

        const val DAIKIN_HOUSE_TEMP_KEY = "htemp="
        const val DAIKIN_OUTER_TEMP_KEY = "otemp="
    }
}
