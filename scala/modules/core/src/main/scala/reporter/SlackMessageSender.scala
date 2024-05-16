package jap.heart
package reporter

import config.HeartClientConfig
import reporter.SlackMessageSender.*
import typeclass.Effect

abstract class SlackMessageSender[F[_]: Effect] extends MessengerReportSender[F] {
  def sendRequest(request: PostMessageRequest): F[Unit]
  def sendMessage(server: HeartClientConfig, report: HealthReport): F[Unit] =
    sendRequest(PostMessageRequest(config.channel, format(server, config, report)))
}

object SlackMessageSender {
  def apply[F[_]](implicit api: SlackMessageSender[F]): SlackMessageSender[F] = api

  case class PostMessageRequest(channel: String, text: String)
  case class SlackResponse(ok: Boolean, ts: String, error: Option[String])

  case class InvalidResponseError(status: Int, body: String) extends Exception(s"Bad status code from Slack: $status")
  case class ApiError(code: String)                          extends Exception(code)
}
