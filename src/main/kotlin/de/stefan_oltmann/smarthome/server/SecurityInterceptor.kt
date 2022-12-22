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
package de.stefan_oltmann.smarthome.server

import java.io.File
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.Response
import javax.ws.rs.ext.Provider

@Provider
class SecurityInterceptor : ContainerRequestFilter {

    private val authCode by lazy {

        val authCodeFile = File(DATA_DIR_NAME, AUTH_CODE_FILE_NAME)

        try {

            authCodeFile.readText()

        } catch (ex: Exception) {

            logger.error("Could not read ${authCodeFile.absolutePath}. Rejecting all requests.", ex)

            /* Initialize with empty String. */
            ""
        }
    }

    override fun filter(context: ContainerRequestContext) {

        val requestAuthCode = context.getHeaderString("AUTH_CODE")

        if (requestAuthCode == null || requestAuthCode.isBlank())
            abort(context, "No AUTH_CODE provided.")
        else if (authCode != requestAuthCode)
            abort(context, "Wrong AUTH_CODE.")
    }

    private fun abort(context: ContainerRequestContext, message: String) {
        context.abortWith(Response.status(Response.Status.FORBIDDEN).entity(message).build())
    }
}
