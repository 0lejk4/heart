package jap.heart
package server.akka

import typeclass.Ticker

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

final case class AkkaStreamTicker()(implicit system: ActorSystem) extends Ticker[Future] {
  implicit private val ec = system.dispatcher

  def tick(interval: FiniteDuration)(action: () => Future[Unit]): Future[Unit] =
    Source
      .tick(Duration.Zero, interval, ())
      .mapAsync(1)(_ => action().recover(e => system.log.error(e, "Unchecked error in tick")))
      .run()
      .map(_ => ())
}
