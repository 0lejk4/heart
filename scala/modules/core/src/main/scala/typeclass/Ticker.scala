package jap.heart.typeclass

import scala.concurrent.duration.FiniteDuration

trait Ticker[F[_]] {
  def tick(interval: FiniteDuration)(action: () => F[Unit]): F[Unit]
}

object Ticker {
  def apply[F[_]](implicit ticker: Ticker[F]): Ticker[F] = ticker
}
