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
package server

import HealthStatus.{withName as _, *}
import config.*

import sttp.tapir.*
import sttp.tapir.Codec.PlainCodec

package object tapir {
  implicit val healthStatusCodec: PlainCodec[HealthStatus]        = Codec.parsedString(HealthStatus.withName(_).get)
  implicit lazy val healthStatusSchema: Schema[HealthStatus]      = Schema.derivedEnumeration[HealthStatus]()
  implicit lazy val healthyStatusSchema: Schema[Healthy.type]     = Schema.string[Healthy.type]
  implicit lazy val degradedStatusSchema: Schema[Degraded.type]   = Schema.string[Degraded.type]
  implicit lazy val unhealthyStatusSchema: Schema[Unhealthy.type] = Schema.string[Unhealthy.type]
  implicit lazy val healthDataSchema: Schema[HealthExtra] = Schema(SchemaType.SCoproduct(Nil, None)(_ => None), None)
  implicit lazy val healthReportComponentSchema: Schema[HealthReportComponent] = Schema.derived[HealthReportComponent]
  implicit lazy val healthComponentSchema: Schema[HealthComponent]             = Schema.derived[HealthComponent]
  implicit lazy val healthReportSchema: Schema[HealthReport]                   = Schema.derived[HealthReport]

  implicit lazy val heartStatusToCodeMappingSchema: Schema[HeartStatusToCodeMapping] =
    Schema.derived[HeartStatusToCodeMapping]
  implicit lazy val heartServerConfigSchema: Schema[HeartServerConfigView] = Schema.derived[HeartServerConfigView]
}
