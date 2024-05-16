package jap.heart
package reporter

import config.HeartClientConfig
import typeclass.Effect

abstract class MessengerReportSender[F[_]: Effect] extends ReportSender[F] {
  def config: ReportSenderConfig
  def format: ReportFormat

  def sendMessage(server: HeartClientConfig, report: HealthReport): F[Unit]
  def send(server: HeartClientConfig, report: HealthReport): F[Unit] =
    if (!config.includeHealthy && report.status.isHealthy) Effect[F].unit
    else Effect[F].unit(sendMessage(server, report))
}
