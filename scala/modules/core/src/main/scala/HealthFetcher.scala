package jap.heart

import config.HeartClientConfig

trait HealthFetcher[F[_]] {
  def fetchReport(server: HeartClientConfig): F[HealthReport]
  def fetchStatus(server: HeartClientConfig): F[HealthStatus]
  def fetchComponents(server: HeartClientConfig): F[HealthComponent]
}

object HealthFetcher {
  def apply[F[_]](implicit fetcher: HealthFetcher[F]): HealthFetcher[F] = fetcher
}
