/*
 * Copyright 2022 Jap
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jap.heart
package examples

import client.http4s.Http4sStatusCodeHealthCheck
import config.HeartServerConfig
import interop.zio.*
import json.circe.*
import server.http4s.*

import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.client.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

object Http4sExample extends ZIOAppDefault {
  implicit val client = JavaNetClientBuilder[Task].create

  val healthApi: HealthApi[Task] =
    HealthApi(
      PostgresHealthCheck("jdbc:postgresql://localhost/jap?user=pavel&password=kravec&ssl=true").name("pgdb"),
      DelayedHealthCheck("https://jap-company.github.io/fields/"),
      Http4sStatusCodeHealthCheck(Request[Task](uri = Uri.unsafeFromString("http://localhost:3000")))
        .tags(List("frontend"))
        .addTags("admin", "react")
        .addExtra("service", "http://localhost:3000")
        .modify(_.addExtra("url", "http://localhost:3000")),
    )
      .timeoutTo(HealthState.degraded(message = Some("Timeout bro")), 1.second)
      .recover((e: Throwable) => HealthState.unhealthy(message = Some(e.getMessage)))
      .modify(_.addExtra("server", "http://localhost:8080"))

  val endpoints: HttpRoutes[Task] =
    HealthHttp4sEndpoints(
      healthApi = healthApi,
      config = HeartServerConfig.default.enableAdminUI,
      // .basicAuth("secret", "secret"),
      // .secretAuth("secret"),
    ).healthRoutes

  val httpApp =
    CORS.policy.withExposeHeadersAll.withAllowHeadersAll.apply(endpoints.orNotFound)

  val run =
    EmberServerBuilder
      .default[Task]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .useForever
      .exitCode
}
