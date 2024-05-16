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
package server.http4s

import HealthStatus.*
import config.*
import config.HeartAuth.*
import HealthHttp4sEndpoints.*

import cats.data.*
import cats.effect.kernel.Async
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.headers.{Authorization, Location}
import org.http4s.server.*
import org.http4s.server.middleware.authentication.BasicAuth as Http4sBasicAuth
import org.http4s.server.staticcontent.*
import org.typelevel.ci.*
case class HealthHttp4sEndpoints[F[_]](
    healthApi: HealthApiProtocol[F],
    config: HeartServerConfig = HeartServerConfig.default,
)(implicit
    reportOut: EntityEncoder[F, HealthReport],
    componentsOut: EntityEncoder[F, List[HealthComponent]],
    configOut: EntityEncoder[F, HeartServerConfigView],
    F: Async[F],
) {
  private val dsl = new Http4sDsl[F] {}
  import dsl.*

  val HealthyStatus: Status   = Status.fromInt(config.statusMapping.healthy).getOrElse(Status.Ok)
  val DegradedStatus: Status  = Status.fromInt(config.statusMapping.degraded).getOrElse(Status.Ok)
  val UnhealthyStatus: Status = Status.fromInt(config.statusMapping.unhealthy).getOrElse(Status.ServiceUnavailable)
  def statusToCode(status: HealthStatus): Status = status match {
    case Healthy   => HealthyStatus
    case Degraded  => DegradedStatus
    case Unhealthy => UnhealthyStatus
  }

  val configRoute: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / `ConfigPath` =>
      Ok(config.toView)
    }

  val componentsRoute: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / `ComponentsPath` :? HealthQueryParam(query) =>
      Ok(healthApi.components(query))
    }

  val reportRoute: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / `ReportPath` :? HealthQueryParam(query) =>
      healthApi.report(query).map { report =>
        Response[F](
          status = statusToCode(report.status),
          body = reportOut.toEntity(report).body,
        )
      }
    }

  val statusRoute: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / `StatusPath` :? HealthQueryParam(query) =>
      healthApi.status(query).map(statusToCode).map(Response[F](_))
    }

  val authMiddleware: HttpMiddleware[F] =
    config.auth match {
      case Public =>
        identity

      case auth: Secret =>
        route =>
          Kleisli { req =>
            req.headers.get[Authorization] match {
              case Some(Authorization(Credentials.Token(SecretAuthScheme, secret))) if (auth.verify(secret)) =>
                route(req)
              case _ => OptionT.liftF(NotFound())
            }
          }

      case auth: Basic =>
        Http4sBasicAuth[F, Unit](
          HeartRealm,
          c => F.pure(if (auth.verify(c.username, c.password)) Some(()) else None),
        ).compose(k => Kleisli(r => k(r.req)))
    }

  val uiRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] { case request @ GET -> Root =>
      if (!request.uri.path.endsWithSlash)
        TemporaryRedirect(Location(request.uri.withPath(request.uri.path.addEndsWithSlash)))
      else StaticFile.fromResource("/heart-admin/index.html", Some(request)).getOrElseF(NotFound())
    } <+> resourceServiceBuilder[F]("/heart-admin").toRoutes

  val apiRoutes: HttpRoutes[F] = (authMiddleware(componentsRoute <+> statusRoute <+> reportRoute) <+> configRoute)

  val healthRoutes: HttpRoutes[F] =
    Router(
      s"/$HealthPath"             -> apiRoutes,
      s"/$HealthPath/$AdminPath/" -> (if (config.serveAdminUI) uiRoutes else HttpRoutes.empty[F]),
    )
}

object HealthHttp4sEndpoints {
  val SecretAuthScheme = ci"secret"

  object HealthQueryParam {
    def unapply(params: Map[String, collection.Seq[String]]): Option[HealthQuery] = {
      Some(
        HealthQuery(
          includeTags = params.get("includeTags").toList.flatten,
          excludeTags = params.get("excludeTags").toList.flatten,
          includeNames = params.get("includeNames").toList.flatten,
          excludeNames = params.get("excludeNames").toList.flatten,
        )
      )
    }
  }
}
