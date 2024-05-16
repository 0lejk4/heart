package jap.heart
package reporter

import config.HeartClientConfig
import typeclass.Effect

trait ReportSender[F[_]] {
  def send(server: HeartClientConfig, report: HealthReport): F[Unit]
}

object ReportSender {
  def apply[F[_]](implicit service: ReportSender[F]): ReportSender[F] = service

  case class Multiple[F[_]: Effect](senders: List[ReportSender[F]]) extends ReportSender[F] {
    def send(server: HeartClientConfig, report: HealthReport): F[Unit] =
      Effect[F].unit {
        Effect[F].foreachPar(senders)(_.send(server, report))
      }
  }
}
