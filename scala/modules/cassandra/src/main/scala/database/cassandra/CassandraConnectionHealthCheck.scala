package jap.heart
package database.cassandra

import typeclass.DeferEffect

import com.datastax.oss.driver.api.core.CqlSession

case class CassandraConnectionHealthCheck[F[_]](session: CqlSession)(implicit D: DeferEffect[F])
    extends HealthCheck[F](
      name = "cassandra",
      tags = List("database", "cassandra"),
    ) {
  def check: F[HealthState] =
    D.suspend(HealthState(status = session.execute("SELECT now() FROM system.local").isFullyFetched))
}
