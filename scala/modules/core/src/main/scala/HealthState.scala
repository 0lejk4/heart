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

import HealthStatus.*

case class HealthState(
    status: HealthStatus,
    message: Option[String] = None,
    extra: Map[String, HealthExtra] = Map.empty,
) {
  def status(value: HealthStatus): HealthState = copy(status = value)
  def message(value: Option[String]): HealthState = copy(message = value)
  def message(value: String): HealthState = message(Some(value))
  def noMessage: HealthState = message(None)
  def extra(extra: Map[String, HealthExtra]): HealthState = copy(extra = extra)
  def addExtra(key: String, value: HealthExtra): HealthState = copy(extra = extra + (key -> value))
}

object HealthState {
  def healthyIf(predicate: Boolean): HealthState   = HealthState(predicate)
  def unhealthyIf(predicate: Boolean): HealthState = HealthState(!predicate)
  def healthy(message: Option[String] = None, extra: Map[String, HealthExtra] = Map.empty): HealthState   =
    HealthState(status = Healthy, message = message, extra = extra)
  def unhealthy(message: Option[String] = None, extra: Map[String, HealthExtra] = Map.empty): HealthState =
    HealthState(status = Unhealthy, message = message, extra = extra)
  def degraded(message: Option[String] = None, extra: Map[String, HealthExtra] = Map.empty): HealthState  =
    HealthState(status = Degraded, message = message, extra = extra)
}
