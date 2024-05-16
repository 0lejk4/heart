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

import config.HeartServerConfig
import interop.zio.*
import json.circe.*
import server.akka.*
import typeclass.Effect.future.*

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.*
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport.*
import zio.*

import scala.concurrent.Future
import scala.io.StdIn

object AkkaExample {
  def main(args: Array[String]): Unit = {
    implicit val system           = ActorSystem(Behaviors.empty, "heart")
    implicit val executionContext = system.executionContext

    val healthApi: HealthApi[Future] = HealthApi(
      PostgresHealthCheck("jdbc:postgresql://localhost/jap?user=pavel&password=kravec&ssl=true").name("pgdb"),
      DelayedHealthCheck("https://jap-company.github.io/fields/"),
    ).recoverToUnhealthy
      .timeoutTo(HealthState.degraded(message = Some("Timeout bro")), 1.second)
      .transform(r => Unsafe.unsafe(implicit u => Runtime.default.unsafe.runToFuture(r).future))

    val healthEndpoints = HealthAkkaEndpoints(
      healthApi = healthApi,
      config = HeartServerConfig.default.enableAdminUI,
      // .secretAuth("secret"),
    )
    val corsSettings    = CorsSettings.default(system.classicSystem).withAllowGenericHttpRequests(true)
    val bindingFuture   = Http().newServerAt("localhost", 8080).bind(cors(corsSettings)(healthEndpoints.healthRoutes))
    println(s"Server now online. Please navigate to http://localhost:8080/health\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
