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

import scala.concurrent.duration.Duration

case class HealthReportComponent(
    component: String,
    status: HealthStatus,
    duration: Duration,
    message: Option[String] = None,
    extra: Map[String, HealthExtra] = Map.empty,
) {
  def pretty: String =
    (List(component) ++ List(status.toString) ++ message ++ List(duration.toString)).mkString(", ")
}

object HealthReportComponent {
  def apply(component: String, state: HealthState, duration: Duration) =
    new HealthReportComponent(
      component = component,
      status = state.status,
      extra = state.extra,
      message = state.message,
      duration = duration,
    )
}
