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
package typeclass

import scala.concurrent.*

/** Typeclass that provides error handling capabilities. */
trait ErrorEffect[F[_], E] {
  def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
  def handleError[A](fa: F[A])(f: E => A): F[A]
}

object ErrorEffect {
  def apply[F[_], E](implicit F: ErrorEffect[F, E]): ErrorEffect[F, E] = F

  implicit def toFutureErrorEffect(implicit ec: ExecutionContext): FutureErrorEffect = new FutureErrorEffect

  /** ErrorEffect instance for future. Requires implicit [[scala.concurrent.ExecutionContext]] in scope. */
  class FutureErrorEffect(implicit ec: ExecutionContext) extends ErrorEffect[Future, Throwable] {
    def handleErrorWith[A](fa: Future[A])(f: Throwable => Future[A]): Future[A] =
      fa.recoverWith(PartialFunction.fromFunction(f))
    def handleError[A](fa: Future[A])(f: Throwable => A): Future[A]             =
      fa.recover(PartialFunction.fromFunction(f))
  }
}
