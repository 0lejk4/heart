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

import typeclass.Effect

trait HealthApiProtocol[F[_]] {
  def queryHealthChecks(query: HealthQuery = HealthQuery.empty): List[HealthCheck[F]]
  def components(query: HealthQuery = HealthQuery.empty): F[List[HealthComponent]]
  def isHealthy(query: HealthQuery = HealthQuery.empty): F[Boolean]
  def status(query: HealthQuery = HealthQuery.empty): F[HealthStatus]
  def report(query: HealthQuery = HealthQuery.empty): F[HealthReport]
}

final case class HealthApi[F[_]](healthChecks: List[HealthCheck[F]])(implicit F: Effect[F])
    extends HealthApiProtocol[F] {
  def queryHealthChecks(query: HealthQuery = HealthQuery.empty): List[HealthCheck[F]] =
    healthChecks.filter(h => query(h.component))

  def components(query: HealthQuery = HealthQuery.empty): F[List[HealthComponent]] =
    F.pure(queryHealthChecks(query).map(_.component))

  def isHealthy(query: HealthQuery = HealthQuery.empty): F[Boolean] =
    F.map(report(query))(_.status == HealthStatus.Healthy)

  def status(query: HealthQuery = HealthQuery.empty): F[HealthStatus] =
    F.map(report(query))(_.status)

  def report(query: HealthQuery = HealthQuery.empty): F[HealthReport] = {
    def checkedComponents =
      F.foreachPar(queryHealthChecks(query)) { h =>
        F.map(F.timed(h.check)) { case (duration, state) =>
          HealthReportComponent(
            component = h.component.name,
            state = state,
            duration = duration,
          )
        }
      }

    F.map(F.timed(checkedComponents)) { case (totalDuration, components) =>
      HealthReport(
        status = components.map(_.status).minOption.getOrElse(HealthStatus.Healthy),
        components = components,
        totalDuration = totalDuration,
      )
    }
  }

  def transform[M[_]: Effect](f: F[HealthState] => M[HealthState]): HealthApi[M] =
    new HealthApi[M](healthChecks.map(_.transform[M](f)))

  def modifyF(f: F[HealthState] => F[HealthState]): HealthApi[F] =
    new HealthApi[F](healthChecks.map(_.transform[F](f)))
  def modify(f: HealthState => HealthState): HealthApi[F]        =
    modifyF(F.map(_)(f))
}

object HealthApi {
  def apply[F[_]](healthChecks: HealthCheck[F]*)(implicit F: Effect[F]): HealthApi[F] =
    new HealthApi[F](healthChecks.toList)
}
