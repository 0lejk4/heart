package jap.heart
package client.http4s

import reporter.*
import reporter.SlackMessageSender.*
import typeclass.Effect

import cats.effect.*
import cats.implicits.toFunctorOps
import org.http4s.*
import org.http4s.client.*
import org.http4s.headers.*

case class Http4sSlackMessageSender[F[_]: Effect: Concurrent](
    token: String,
    config: ReportSenderConfig,
    format: ReportFormat = ReportFormat.slack,
    slackApiBaseUrl: String = "https://slack.com/api/",
)(implicit
    client: Client[F],
    requestMarshaller: EntityEncoder[F, PostMessageRequest],
    responseUnmarshaller: EntityDecoder[F, SlackResponse],
) extends SlackMessageSender[F] {
  def sendRequest(request: PostMessageRequest): F[Unit] = {
    client
      .run(
        Request[F](
          Method.POST,
          Uri.unsafeFromString(slackApiBaseUrl + "chat.postMessage"),
          headers = Headers(Authorization(Credentials.Token(AuthScheme.OAuth, token))),
        ).withEntity(request)
      )
      .use { response =>
        response.status match {
          case Status.Ok => response.as[SlackResponse]
          case _         =>
            EntityDecoder
              .decodeText(response)
              .map[SlackResponse](body => throw InvalidResponseError(response.status.code, body))
        }
      }
      .void
  }

}
