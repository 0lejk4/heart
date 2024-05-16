package jap.heart
package interop

import typeclass.*

import _root_.zio.*

import scala.concurrent.duration.FiniteDuration

private class ZIOEffect[R, E] extends Effect[ZIO[R, E, _]] with DeferEffect[ZIO[R, E, _]] {
  private type F[A] = ZIO[R, E, A]
  def pure[A](a: => A): F[A]                                       = ZIO.succeed(a)
  def suspend[A](a: => A): F[A]                                    = ZIO.succeed(a)
  def defer[A](a: => F[A]): F[A]                                   = ZIO.suspendSucceed(a)
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]                  = fa.flatMap(f)
  def map[A, B](fa: F[A])(f: A => B): F[B]                         = fa.map(f)
  def sequencePar[A](list: List[ZIO[R, E, A]]): ZIO[R, E, List[A]] = ZIO.collectAllPar(list)
}

private class ZIOTimeoutEffect[R, E] extends TimeoutEffect[ZIO[R, E, _]] {
  private type F[A] = ZIO[R, E, A]
  def timeoutTo[A](effect: F[A], timeout: FiniteDuration, to: => A): F[A] =
    effect.timeoutTo(to)(identity)(Duration.fromScala(timeout))
}

private class ZIOErrorEffect[R, E] extends ErrorEffect[ZIO[R, E, _], E] {
  private type F[A] = ZIO[R, E, A]
  def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A] = fa.catchAll(f)
  def handleError[A](fa: F[A])(f: E => A): F[A]        = fa.catchAll(f.andThen(ZIO.succeed(_)))
}
