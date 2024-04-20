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
package de.stefan_oltmann.smarthome.server.data.impl

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.stefan_oltmann.smarthome.server.DATA_DIR_NAME
import de.stefan_oltmann.smarthome.server.data.DaikinRepository
import de.stefan_oltmann.smarthome.server.data.WebhookRepository
import de.stefan_oltmann.smarthome.server.model.Daikin
import de.stefan_oltmann.smarthome.server.model.Webhook
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

const val DAIKINS_FILE_NAME = "daikins.json"

class FileDaikinRepository : DaikinRepository {

    override val daikins: Set<Daikin>

    private val gson by lazy {

        val builder = GsonBuilder()
        builder.setPrettyPrinting()
        builder.create()
    }

    init {

        val daikinsFile = File(DATA_DIR_NAME, DAIKINS_FILE_NAME)

        val filePath = daikinsFile.absolutePath

        if (daikinsFile.exists()) {

            var parsedDaikins: Set<Daikin>

            try {

                val listType = object : TypeToken<HashSet<Daikin>>() {}.type

                parsedDaikins = gson.fromJson(daikinsFile.readText(), listType)

                logger.info("Started with ${parsedDaikins.size} daikins from '$filePath'.")

            } catch (ex: Exception) {

                parsedDaikins = emptySet()

                logger.error("Could not parse '$filePath'", ex)
            }

            daikins = parsedDaikins

        } else {

            daikins = emptySet()

            logger.error("Did not find file '$filePath'. Using empty daikins list.")
        }
    }

    companion object {

        val logger: Logger = LoggerFactory.getLogger(FileDaikinRepository::class.java)

    }
}
