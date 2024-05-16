package jap.heart
package doobie

import _root_.doobie.*
import _root_.doobie.implicits.*
import cats.effect.kernel.MonadCancelThrow

import scala.concurrent.duration.FiniteDuration

case class DoobieConnectionHealthCheck[F[_]](
    tx: Transactor[F],
    timeout: Option[FiniteDuration] = None,
)(implicit F: MonadCancelThrow[F])
    extends HealthCheck[F](
      name = "postgresql",
      tags = List("database"),
    ) {
  // zero means infinite in JDBC
  val actualTimeoutSeconds: Int = timeout.fold(0)(_.toSeconds.toInt)
  def check: F[HealthState]     = F.map(FC.isValid(actualTimeoutSeconds).transact(tx))(HealthState.healthyIf)
}
