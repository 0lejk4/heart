package jap.heart
package server.akka

import config.*

import akka.actor.*
import akka.http.scaladsl.*
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.*
import akka.http.scaladsl.unmarshalling.*

import scala.concurrent.*

final case class AkkaHealthFetcher()(implicit
    system: ActorSystem,
    reportUnmarshaller: FromResponseUnmarshaller[HealthReport],
    statusUnmarshaller: FromResponseUnmarshaller[HealthStatus],
    componentUnmarshaller: FromResponseUnmarshaller[HealthComponent],
) extends HealthFetcher[Future] {
  implicit private val ec: ExecutionContext = system.dispatcher

  private def call[R: FromResponseUnmarshaller](
      server: HeartClientConfig,
      path: HeartClientConfig => String,
  ): Future[R] = {
    val headers = server.headers.toList.flatMap { case (name, value) =>
      HttpHeader.parse(name, value) match {
        case ok: HttpHeader.ParsingResult.Ok => Some(ok.header)
        case _                               => None
      }
    }

    Http()
      .singleRequest(Get(path(server)).withHeaders(headers))
      .flatMap(Unmarshal(_).to[R])
  }

  def fetchReport(server: HeartClientConfig): Future[HealthReport]        =
    call[HealthReport](server, _.reportUrl)
  def fetchStatus(server: HeartClientConfig): Future[HealthStatus]        =
    call[HealthStatus](server, _.statusUrl)
  def fetchComponents(server: HeartClientConfig): Future[HealthComponent] =
    call[HealthComponent](server, _.componentsUrl)
}
