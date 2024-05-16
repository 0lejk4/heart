/*
 * Copyright 2022 Jap
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jap.heart
package interop

import typeclass.*

import _root_.cats.*
import _root_.cats.effect.kernel.GenTemporal

import scala.concurrent.duration.FiniteDuration

object cats {

  /** [[jap.heart.typeclass.Effect]] instance for any F[_] that has `cats.Monad` and `cats.Parallel` instance */
  implicit def fromCatsMonad[F[_]: Monad: Parallel]: Effect[F] = new Effect[F] {
    def pure[A](a: => A): F[A]                       = Monad[F].pure(a)
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]  = Monad[F].flatMap(fa)(f)
    def map[A, B](fa: F[A])(f: A => B): F[B]         = Monad[F].map(fa)(f)
    def sequencePar[A](list: List[F[A]]): F[List[A]] = Parallel.parSequence(list)
  }

  /** [[jap.heart.typeclass.DeferEffect]] instance for any F[_] that has `cats.Defer` and `cats.Applicative` instance */
  implicit def fromCatsMonadDefer[F[_]: Defer: Applicative]: DeferEffect[F] = new DeferEffect[F] {
    def defer[A](a: => F[A]): F[A] = Defer[F].defer(a)
    def suspend[A](a: => A): F[A]  = defer(Applicative[F].pure(a))
  }

  /** [[jap.heart.typeclass.TimeoutEffect]] instance for any F[_] that has `cats.effect.kernel.GenTemporal` instance */
  implicit def fromCatsGenTemporal[F[_], E](implicit F: GenTemporal[F, E]): TimeoutEffect[F] =
    new TimeoutEffect[F] {
      def timeoutTo[A](effect: F[A], timeout: FiniteDuration, to: => A): F[A] =
        F.timeoutTo(effect, timeout, F.pure(to))
    }

  /** [[jap.heart.typeclass.ErrorEffect]] instance for any F[_] that has `cats.ApplicativeError` instance */
  implicit def fromCatsApplicativeError[F[_], E](implicit F: ApplicativeError[F, E]): ErrorEffect[F, E] =
    new ErrorEffect[F, E] {
      def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A] = F.handleErrorWith(fa)(f)

      def handleError[A](fa: F[A])(f: E => A): F[A] = F.handleError(fa)(f)
    }
}
