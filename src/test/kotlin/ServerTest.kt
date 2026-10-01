package com

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {

    @Test
    fun `unknown endpoint returns not found`() = testApplication {
        environment {
            config = MapApplicationConfig("koog.google.apikey" to "test-api-key")
        }
        application {
            configureRouting()
        }

        assertEquals(HttpStatusCode.NotFound, client.get("/not-found").status)
    }

}
