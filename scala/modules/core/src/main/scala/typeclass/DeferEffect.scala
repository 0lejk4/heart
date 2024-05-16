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

/** Typeclass that provides Monad capabilities. */
trait DeferEffect[F[_]] {

  /** Lazily lifts value into Effect */
  def suspend[A](a: => A): F[A]

  /** Makes effect lazy */
  def defer[A](a: => F[A]): F[A]
}

object DeferEffect {
  def apply[F[_]](implicit F: DeferEffect[F]): DeferEffect[F] = F

  /** DeferEffect for Sync */
  type Sync[A] = A
  implicit object SyncDeferEffect extends DeferEffect[Sync] {
    def suspend[A](a: => A): A = a
    def defer[A](a: => A): A   = a
  }

  implicit def toFutureDeferEffect(implicit ec: ExecutionContext): FutureDeferEffect = new FutureDeferEffect

  /** DeferEffect instance for future. Requires implicit [[scala.concurrent.ExecutionContext]] in scope. */
  class FutureDeferEffect(implicit ec: ExecutionContext) extends DeferEffect[Future] {
    def defer[A](a: => Future[A]): Future[A] = suspend(a).flatten
    def suspend[A](a: => A): Future[A]       = Future(a)
  }
}
