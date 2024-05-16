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
package json

import config.*
import reporter.SlackMessageSender.*
import reporter.TelegramMessageSender.*

import io.circe.*
import io.circe.generic.semiauto.*

import scala.concurrent.duration.Duration

trait HeartCirce {
  implicit val durationCodec: Codec[Duration] = Codec.from(
    Decoder.decodeLong.map(Duration.fromNanos),
    Encoder.encodeLong.contramap(_.toNanos),
  )

  implicit val healthStatusCodec: Codec[HealthStatus] =
    Codec.from(
      Decoder.decodeString.emap {
        case "Unhealthy" => Right(HealthStatus.Unhealthy)
        case "Healthy"   => Right(HealthStatus.Healthy)
        case "Degraded"  => Right(HealthStatus.Degraded)
        case _           => Left("Unknown status")
      },
      Encoder.encodeString.contramap(_.toString),
    )

  implicit val healthComponentCodec: Codec[HealthComponent]             = deriveCodec[HealthComponent]
  implicit val healthReportCodec: Codec[HealthReport]                   = deriveCodec[HealthReport]
  implicit val healthReportComponentCodec: Codec[HealthReportComponent] = deriveCodec[HealthReportComponent]

  implicit val heartServerAuthEncoder: Encoder[HeartAuth] = Encoder.encodeString.contramap(_.toString)
  implicit val heartServerAuthDecoder: Decoder[HeartAuth] = Decoder.failedWithMessage("Not used")
  implicit val heartStatusToCodeMappingCodec: Codec[HeartStatusToCodeMapping] = deriveCodec[HeartStatusToCodeMapping]
  implicit val heartServerConfigCodec: Codec[HeartServerConfigView]           = deriveCodec[HeartServerConfigView]

  implicit val healthDataDecoder: Decoder[HealthExtra] = Decoder.decodeJson.map(_.foldWith(HealthDataFolder))
  implicit val healthDataEncoder: Encoder[HealthExtra] = Encoder.instance(toCirce)

  def toCirce(data: HealthExtra): Json = data match {
    case NoExtra             => Json.Null
    case IntExtra(value)     => Json.fromInt(value)
    case DoubleExtra(value)  => Json.fromDoubleOrNull(value)
    case StringExtra(value)  => Json.fromString(value)
    case BooleanExtra(value) => Json.fromBoolean(value)
    case ArrayExtra(values)  => Json.fromValues(values.map(toCirce))
    case ObjectExtra(fields) => Json.fromFields(fields.view.mapValues(toCirce))
  }
  object HealthDataFolder extends Json.Folder[HealthExtra] {
    def onBoolean(value: Boolean): HealthExtra    = BooleanExtra(value)
    def onString(value: String): HealthExtra      = StringExtra(value)
    def onArray(value: Vector[Json]): HealthExtra = ArrayExtra(value.map(_.foldWith(this)))
    def onNumber(value: JsonNumber): HealthExtra  = value.toInt.map(IntExtra(_)).getOrElse(DoubleExtra(value.toDouble))
    def onObject(value: JsonObject): HealthExtra  = ObjectExtra(value.toMap.view.mapValues(_.foldWith(this)).toMap)
    def onNull: HealthExtra                       = NoExtra
  }

  // SLACK
  implicit val slackPostMessageRequestCodec: Codec[PostMessageRequest] = deriveCodec[PostMessageRequest]
  implicit val slackResponseCodec: Codec[SlackResponse]                = deriveCodec[SlackResponse]

  // TELEGRAM
  implicit val telegramSendMessageRequestCodec: Codec[SendMessageRequest] = deriveCodec[SendMessageRequest]
  implicit val telegramResponseCodec: Codec[TelegramResponse]             = deriveCodec[TelegramResponse]
}
