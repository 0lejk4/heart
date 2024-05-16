package jap.heart
package reporter

import config.HeartClientConfig
import reporter.TelegramMessageSender.*
import typeclass.Effect

abstract class TelegramMessageSender[F[_]: Effect] extends MessengerReportSender[F] {
  def token: String
  def telegramHost: String
  def apiBaseUrl                                                            = s"https://$telegramHost/bot$token/"
  def sendRequest(request: SendMessageRequest): F[Unit]
  def sendMessage(server: HeartClientConfig, report: HealthReport): F[Unit] =
    sendRequest(SendMessageRequest(config.channel, format(server, config, report)))
}

object TelegramMessageSender {
  def apply[F[_]](implicit api: TelegramMessageSender[F]): TelegramMessageSender[F] = api

  case class SendMessageRequest(
      chat_id: String,
      text: String,
  )

  case class TelegramResponse(
      ok: Boolean,
      description: Option[String] = None,
      error_code: Option[Int] = None,
  )

  def processApiResponse(response: TelegramResponse): Unit = response match {
    case TelegramResponse(true, _, _)                          => ()
    case TelegramResponse(false, description, Some(errorCode)) =>
      throw TelegramApiException(
        description.getOrElse("Unexpected/invalid/empty response"),
        errorCode,
        None,
      )

    case other =>
      throw new RuntimeException(s"Unexpected API response: $other")
  }

  case class TelegramApiException(
      message: String,
      errorCode: Int,
      cause: Option[Throwable] = None,
  ) extends RuntimeException(message, cause.orNull)
}
