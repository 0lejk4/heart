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

sealed trait HealthStatus {
  def isHealthy: Boolean   = this == HealthStatus.Healthy
  def isDegraded: Boolean  = this == HealthStatus.Degraded
  def isUnhealthy: Boolean = this == HealthStatus.Unhealthy

  def parse(string: String): Option[this.type] = if (string == this.toString) Some(this) else None
}

object HealthStatus {
  implicit def fromBoolean(healthy: Boolean): HealthStatus = if (healthy) Healthy else Unhealthy

  case object Healthy   extends HealthStatus
  case object Degraded  extends HealthStatus
  case object Unhealthy extends HealthStatus

  def withName(name: String): Option[HealthStatus] = name match {
    case "Healthy"   => Some(Healthy)
    case "Degraded"  => Some(Degraded)
    case "Unhealthy" => Some(Unhealthy)
    case _           => None
  }

  implicit val ordering: Ordering[HealthStatus] = Ordering.by {
    case Healthy   => 2
    case Degraded  => 1
    case Unhealthy => 0
  }
}
