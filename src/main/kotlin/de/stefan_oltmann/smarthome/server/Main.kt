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

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

val logger: Logger = LoggerFactory.getLogger(Main::class.java)

const val AUTH_CODE_LENGTH = 50

const val DATA_DIR_NAME = "data"
const val AUTH_CODE_FILE_NAME = "auth_code.txt"
const val LOG_FILE_NAME = "server.log"

@QuarkusMain
object Main {

    @Suppress("MemberNameEqualsClassName")
    @JvmStatic
    fun main(args: Array<String>) {

        /* Override application.properties setting to respect data dir. */
        System.setProperty("quarkus.log.file.path", "$DATA_DIR_NAME/$LOG_FILE_NAME")

        /* Try to create a data folder on startup. */
        createDataDirIfNeeded()

        Quarkus.run(MyApp::class.java, *args)
    }

    private fun createDataDirIfNeeded() {

        val dataDir = File(DATA_DIR_NAME)

        if (!dataDir.exists()) {

            val dataDirCreated = dataDir.mkdirs()

            if (dataDirCreated)
                logger.info("Data dir '${dataDir.absolutePath}' created.")
            else
                logger.error("Creation of data dir '${dataDir.absolutePath}' failed.")
        }
    }

    class MyApp : QuarkusApplication {

        @Throws(Exception::class)
        override fun run(vararg args: String): Int {

            logger.info("Quarkus is starting...")

            writeNewAuthCodeFileIfNeeded()

            /* Wait */
            Quarkus.waitForExit()

            logger.info("Quarkus exited.")

            return 0
        }
    }

    /**
     * Creates the auth_code.txt if it does not exist.
     */
    private fun writeNewAuthCodeFileIfNeeded() {

        val authCodeFile = File(DATA_DIR_NAME, AUTH_CODE_FILE_NAME)

        if (!authCodeFile.exists()) {

            authCodeFile.writeText(generateAuthCode())

            logger.info("New AUTH_CODE created in '${authCodeFile.absolutePath}'.")

        } else
            logger.info("Using existing AUTH_CODE from '${authCodeFile.absolutePath}'.")
    }

    /**
     * Generates a long random string that is suitable as authorization code.
     */
    private fun generateAuthCode(): String {

        val characterSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        val password = StringBuilder()

        repeat(AUTH_CODE_LENGTH) {
            val index = characterSet.indices.random()
            password.append(characterSet[index])
        }

        return password.toString()
    }
}
