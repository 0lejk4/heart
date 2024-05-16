package jap.heart
package server.akka

import reporter.*
import reporter.SlackMessageSender.*
import typeclass.Effect.future.*

import akka.actor.*
import akka.http.scaladsl.*
import akka.http.scaladsl.marshalling.*
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.headers.*
import akka.http.scaladsl.unmarshalling.*

import scala.concurrent.*
import scala.concurrent.duration.*

case class AkkaSlackMessageSender(
    token: String,
    config: ReportSenderConfig,
    format: ReportFormat = ReportFormat.slack,
    toStrictTimeout: FiniteDuration = 10.seconds,
    slackApiBaseUrl: String = "https://slack.com/api/",
)(implicit
    system: ActorSystem,
    ec: ExecutionContext,
    requestMarshaller: ToEntityMarshaller[PostMessageRequest],
    responseUnmarshaller: FromEntityUnmarshaller[SlackResponse],
) extends SlackMessageSender[Future] {
  private val http = Http()

  def sendRequest(request: PostMessageRequest): Future[Unit] = {
    Marshal(request)
      .to[MessageEntity]
      .map(entity =>
        HttpRequest(
          method = HttpMethods.POST,
          uri = slackApiBaseUrl + "chat.postMessage",
          headers = Seq(Authorization(OAuth2BearerToken(token))),
          entity = entity,
        )
      )
      .flatMap(http.singleRequest(_))
      .flatMap(response =>
        response.status match {
          case StatusCodes.OK => Unmarshal(response.entity).to[SlackResponse]
          case _              =>
            response.entity
              .toStrict(toStrictTimeout)
              .map(_.data.decodeString("UTF-8"))
              .map(body => throw InvalidResponseError(response.status.intValue, body))
        }
      )
      .map(_ => ())
  }
}
