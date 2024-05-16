package jap

import jap.heart.typeclass.*

import scala.concurrent.duration.FiniteDuration

package object heart {
  implicit class HealthApiTimeoutOps[F[_]](private val api: HealthApi[F]) extends AnyVal {
    def timeoutTo(to: HealthState, duration: FiniteDuration)(implicit T: TimeoutEffect[F]): HealthApi[F] =
      api.modifyF(T.timeoutTo(_, duration, to))

    def timeoutToDegraded(duration: FiniteDuration)(implicit T: TimeoutEffect[F]): HealthApi[F] =
      timeoutTo(HealthState.degraded(), duration)

    def timeoutToUnhealthy(duration: FiniteDuration)(implicit T: TimeoutEffect[F]): HealthApi[F] =
      timeoutTo(HealthState.unhealthy(), duration)
  }

  implicit class HealthApiErrorOps[F[_]](private val api: HealthApi[F]) extends AnyVal {
    def recoverWith[E](h: E => F[HealthState])(implicit E: ErrorEffect[F, E]): HealthApi[F] =
      api.modifyF(E.handleErrorWith(_)(h))

    def recover[E](h: E => HealthState)(implicit E: ErrorEffect[F, E]): HealthApi[F] =
      api.modifyF(E.handleError(_)(h))

    def recoverToUnhealthy[E](implicit E: ErrorEffect[F, E]): HealthApi[F] =
      recover[E](_ => HealthState.unhealthy())

    def recoverToDegraded[E](implicit E: ErrorEffect[F, E]): HealthApi[F] =
      recover[E](_ => HealthState.degraded())
  }
}
