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
package server.akka

import HealthStatus.*
import config.*
import config.HeartAuth.*

import _root_.akka.http.scaladsl.marshalling.*
import _root_.akka.http.scaladsl.model.*
import _root_.akka.http.scaladsl.server.*
import _root_.akka.http.scaladsl.server.Directives.*
import _root_.akka.http.scaladsl.server.directives.*
import akka.http.scaladsl.model.headers.*

import scala.concurrent.Future
import scala.util.{Failure, Success}

case class HealthAkkaEndpoints(
    healthApi: HealthApiProtocol[Future],
    config: HeartServerConfig = HeartServerConfig.default,
)(implicit
    reportOut: ToEntityMarshaller[HealthReport],
    componentsOut: ToEntityMarshaller[List[HealthComponent]],
    configOut: ToEntityMarshaller[HeartServerConfigView],
) {
  val HealthyStatus: StatusCode   = StatusCodes.getForKey(config.statusMapping.healthy).getOrElse(StatusCodes.OK)
  val DegradedStatus: StatusCode  = StatusCodes.getForKey(config.statusMapping.degraded).getOrElse(StatusCodes.OK)
  val UnhealthyStatus: StatusCode =
    StatusCodes.getForKey(config.statusMapping.unhealthy).getOrElse(StatusCodes.ServiceUnavailable)
  def statusToCode(status: HealthStatus): StatusCode = status match {
    case Healthy   => HealthyStatus
    case Degraded  => DegradedStatus
    case Unhealthy => UnhealthyStatus
  }

  val healthQueryParams: Directive[Tuple1[HealthQuery]] = parameters(
    "includeTags".repeated,
    "excludeTags".repeated,
    "includeNames".repeated,
    "excludeNames".repeated,
  ).tmap { case (includeTags, excludeTags, includeNames, excludeNames) =>
    HealthQuery(
      includeTags = includeTags.toList,
      excludeTags = excludeTags.toList,
      includeNames = includeNames.toList,
      excludeNames = excludeNames.toList,
    )
  }

  val componentsRoute: Route = path(ComponentsPath) {
    pathEndOrSingleSlash {
      healthQueryParams { query =>
        onComplete(healthApi.components(query)) {
          case Success(components) => complete(components)
          case Failure(_)          => complete(UnhealthyStatus)
        }
      }
    }
  }

  val reportRoute: Route = path(ReportPath) {
    pathEndOrSingleSlash {
      healthQueryParams { query =>
        onComplete(healthApi.report(query)) {
          case Success(report) => complete(statusToCode(report.status) -> report)
          case Failure(_)      => complete(UnhealthyStatus)
        }
      }
    }
  }

  val configRoute: Route = path(ConfigPath) {
    pathEndOrSingleSlash {
      complete(config.toView)
    }
  }

  val statusRoute: Route = path(StatusPath) {
    pathEndOrSingleSlash {
      healthQueryParams { query =>
        onComplete(healthApi.status(query)) {
          case Success(status) => complete(statusToCode(status) -> status.toString)
          case Failure(_)      => complete(UnhealthyStatus)
        }
      }
    }
  }

  val uiRoute: Route = pathPrefix(AdminPath) {
    pathEndOrSingleSlash {
      redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect) {
        getFromResource("heart-admin/index.html")
      }
    } ~ getFromResourceDirectory("heart-admin")
  }

  val authDirective: Directive0 =
    config.auth match {
      case Public       => Directive.Empty
      case auth: Secret =>
        authenticateOrRejectWithChallenge[GenericHttpCredentials, Unit] { cred =>
          Future.successful {
            if (cred.exists(c => c.scheme == "Secret" && auth.verify(c.token))) AuthenticationResult.success(())
            else AuthenticationResult.failWithChallenge(HttpChallenge("Secret", HeartRealm))
          }
        }.map(identity)
      case auth: Basic  =>
        authenticateBasicPF(
          realm = HeartRealm,
          authenticator = { case p: Credentials.Provided if p.verify(auth.password.toString) => () },
        ).map(identity)
    }

  val healthRoutes: Route =
    pathPrefix(HealthPath) {
      authDirective {
        componentsRoute ~
          reportRoute ~
          statusRoute
      } ~
        configRoute ~
        (if (config.serveAdminUI) uiRoute else reject)
    }
}
