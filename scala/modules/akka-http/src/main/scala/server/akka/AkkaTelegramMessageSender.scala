package jap.heart
package server.akka

import reporter.*
import reporter.TelegramMessageSender.*
import typeclass.Effect

import akka.actor.*
import akka.http.scaladsl.*
import akka.http.scaladsl.marshalling.*
import akka.http.scaladsl.model.*
import akka.http.scaladsl.unmarshalling.*

import scala.concurrent.*
import scala.concurrent.duration.*

case class AkkaTelegramMessageSender(
    token: String,
    config: ReportSenderConfig,
    format: ReportFormat = ReportFormat.telegram,
    telegramHost: String = "api.telegram.org",
    toStrictTimeout: FiniteDuration = 10.seconds,
)(implicit
    system: ActorSystem,
    requestMarshaller: ToEntityMarshaller[SendMessageRequest],
    responseUnmarshaller: FromEntityUnmarshaller[TelegramResponse],
) extends TelegramMessageSender[Future]()(Effect.future.toFutureEffect(system.dispatcher)) {
  implicit private val ec: ExecutionContext = system.dispatcher
  private val http                          = Http()

  def sendRequest(request: SendMessageRequest): Future[Unit] =
    Marshal(request)
      .to[RequestEntity]
      .map { entity =>
        HttpRequest(HttpMethods.POST, Uri(apiBaseUrl + "SendMessage"), entity = entity)
      }
      .flatMap(http.singleRequest(_))
      .flatMap(r => Unmarshal(r.entity).to[TelegramResponse])
      .map(processApiResponse)
}
