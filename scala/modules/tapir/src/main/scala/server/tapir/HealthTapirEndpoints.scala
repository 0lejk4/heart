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
package server.tapir

import HealthStatus.*
import config.*
import config.HeartAuth.*

import sttp.model.*
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir.*
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.static.ResourcesOptions

case class HealthTapirEndpoints[F[_]](
    healthApi: HealthApiProtocol[F],
    config: HeartServerConfig = HeartServerConfig.default,
)(implicit
    F: typeclass.Effect[F],
    reportOut: JsonCodec[HealthReport],
    componentsOut: JsonCodec[List[HealthComponent]],
    configOut: JsonCodec[HeartServerConfigView],
) {
  import config.*

  private def withHealthCode[H](
      action: HealthQuery => F[H],
      status: H => HealthStatus,
  ): HealthQuery => F[(StatusCode, H)] = { query =>
    F.map(action(query)) { response =>
      val code = status(response) match {
        case Degraded  => statusMapping.degraded
        case Healthy   => statusMapping.healthy
        case Unhealthy => statusMapping.unhealthy
      }

      StatusCode(code) -> response
    }
  }

  val componentsEndpoint =
    HealthTapirEndpoints.componentsEndpoint.serverLogicSuccess[F](healthApi.components)

  val reportEndpoint =
    HealthTapirEndpoints.reportEndpoint.serverLogicSuccess[F](withHealthCode(healthApi.report, _.status))

  val statusEndpoint =
    HealthTapirEndpoints.statusEndpoint.serverLogicSuccess[F](withHealthCode(healthApi.status, identity))

  val configEndpoint =
    HealthTapirEndpoints.configEndpoint.serverLogicSuccess[F](_ => F.pure(config.toView))

  val uiEndpoint = resourcesGetServerEndpoint(HealthPath / AdminPath)(
    this.getClass.getClassLoader,
    "heart-admin",
    ResourcesOptions.default[F].defaultResource(List("index.html")),
  )

  val authMiddleware: ServerEndpoint[Any, F] => ServerEndpoint[Any, F] = {
    val AuthSuccess = Right(())
    val AuthError   = Left(())

    config.auth match {
      case Public => identity

      case auth: Secret =>
        _.prependSecurityPure(
          TapirAuth.http[String]("Secret", WWWAuthenticateChallenge("Secret")),
          statusCode(StatusCode.Forbidden),
        ) { secret =>
          if (auth.verify(secret)) AuthSuccess else AuthError
        }

      case auth: Basic =>
        _.prependSecurityPure(
          TapirAuth.basic[UsernamePassword]().challengeRealm(HeartRealm),
          statusCode(StatusCode.Forbidden),
        ) { credentials =>
          if (auth.verify(credentials.username, credentials.password.getOrElse(""))) AuthSuccess else AuthError
        }
    }
  }

  val healthEndpoints: List[ServerEndpoint[Any, F]] =
    List(statusEndpoint, reportEndpoint, componentsEndpoint).map(authMiddleware)
      ++ (if (config.serveAdminUI) List(uiEndpoint) else Nil)
      :+ configEndpoint
}

object HealthTapirEndpoints {
  private def jsonBody[T](implicit codec: JsonCodec[T]): EndpointIO.Body[String, T] =
    stringBodyUtf8AnyFormat(codec)

  val healthBaseEndpoint = endpoint.get.in(HealthPath)

  val healthQueryInput: EndpointInput[HealthQuery] =
    query[List[String]]("includeTags")
      .and(query[List[String]]("excludeTags"))
      .and(query[List[String]]("includeNames"))
      .and(query[List[String]]("excludeNames"))
      .map((HealthQuery.apply _).tupled)(q => (q.includeTags, q.excludeTags, q.includeNames, q.excludeNames))

  def componentsEndpoint(implicit codec: JsonCodec[List[HealthComponent]]) =
    healthBaseEndpoint
      .in(ComponentsPath)
      .in(healthQueryInput)
      .out(jsonBody[List[HealthComponent]])

  def reportEndpoint(implicit codec: JsonCodec[HealthReport]) =
    healthBaseEndpoint
      .in(ReportPath)
      .in(healthQueryInput)
      .out(statusCode.and(jsonBody[HealthReport]))

  def configEndpoint(implicit codec: JsonCodec[HeartServerConfigView]) =
    healthBaseEndpoint
      .in(ConfigPath)
      .out(jsonBody[HeartServerConfigView])

  val statusEndpoint =
    healthBaseEndpoint
      .in(StatusPath)
      .in(healthQueryInput)
      .out(statusCode.and(plainBody[HealthStatus]))
}
