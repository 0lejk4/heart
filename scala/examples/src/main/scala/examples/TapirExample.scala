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

import config.*
import interop.zio.*
import json.circe.*
import server.tapir.*
// import jap.heart.client.sttp._

import com.comcast.ip4s.*
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
// import sttp.client3._
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

object TapirExample extends ZIOAppDefault {
  // implicit val backend = akka

  val healthApi: HealthApi[Task] = HealthApi(
    PostgresHealthCheck("jdbc:postgresql://localhost/jap?user=pavel&password=kravec&ssl=true").name("pgdb"),
    DelayedHealthCheck("https://jap-company.github.io/fields/"),
    // statusCodeHealthCheck(basicRequest),
  )
    .recover[Throwable](e => HealthState.unhealthy(message = Some(e.getMessage)))
    .timeoutTo(HealthState.degraded(message = Some("Timeout bro")), 1.second)

  val healthConfig =
    HeartServerConfig.default.enableAdminUI
    // .basicAuth("admin", "nimda")
    // .secretAuth("secret")
    // .publicAuth

  val endpoints        = HealthTapirEndpoints(healthApi, healthConfig).healthEndpoints
  val swaggerEndpoints = SwaggerInterpreter().fromServerEndpoints[Task](endpoints, "Heart Tapir App", "1.0")

  val httpApp: HttpRoutes[Task] = Http4sServerInterpreter[Task]().toRoutes(endpoints ++ swaggerEndpoints)

  def run =
    EmberServerBuilder
      .default[Task]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(CORS.policy(httpApp).orNotFound)
      .build
      .useForever
      .exitCode
}
