package jap.heart
package client.http4s

import reporter.*
import reporter.TelegramMessageSender.*
import typeclass.Effect

import org.http4s.*
import org.http4s.client.*

import scala.concurrent.duration.*

case class Http4sTelegramMessageSender[F[_]: Effect](
    token: String,
    config: ReportSenderConfig,
    format: ReportFormat = ReportFormat.telegram,
    telegramHost: String = "api.telegram.org",
    toStrictTimeout: FiniteDuration = 10.seconds,
)(implicit
    client: Client[F],
    requestMarshaller: EntityEncoder[F, SendMessageRequest],
    responseUnmarshaller: EntityDecoder[F, TelegramResponse],
) extends TelegramMessageSender[F] {
  def sendRequest(request: SendMessageRequest): F[Unit] =
    Effect[F].map(
      client
        .expect[TelegramResponse](
          Request[F](Method.POST, Uri.unsafeFromString(apiBaseUrl + "SendMessage")).withEntity(request)
        )
    )(processApiResponse)

}
