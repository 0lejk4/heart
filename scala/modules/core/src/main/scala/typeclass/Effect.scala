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
import scala.concurrent.duration.*

/** Typeclass that provides with Monad capabilities. */
trait Effect[F[_]] {

  def pure[A](a: => A): F[A]

  /** FlatMap one effect into another using `f` function */
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  /** Map effect into another using `f` function */
  def map[A, B](fa: F[A])(f: A => B): F[B]

  def sequencePar[A](list: List[F[A]]): F[List[A]]

  def unit[A](fa: F[A]): F[Unit] = map(fa)(_ => ())

  def unit: F[Unit] = pure(())

  def foreachPar[A, B](list: List[A])(f: A => F[B]): F[List[B]] = sequencePar(list.map(f))

  def timed[A](action: F[A]): F[(Duration, A)] = {
    val start = System.nanoTime
    map(action) { result =>
      val end = System.nanoTime
      Duration.fromNanos(end - start) -> result
    }
  }
}

object Effect {
  def apply[F[_]](implicit F: Effect[F]): Effect[F] = F

  /** Sync [[jap.heart.typeclass.Effect]] */
  type Sync[A] = A
  implicit object SyncEffect extends Effect[Sync] {
    def pure[A](a: => A): A                    = a
    def flatMap[A, B](fa: A)(f: A => B): B     = f(fa)
    def map[A, B](fa: A)(f: A => B): B         = f(fa)
    def sequencePar[A](list: List[A]): List[A] = list

  }

  object future {
    implicit def toFutureEffect(implicit ec: ExecutionContext): Effect[Future] = new FutureEffect

    /** Future [[jap.heart.typeclass.Effect]]. Requires implicit [[scala.concurrent.ExecutionContext]] in scope.
      */
    class FutureEffect(implicit ec: ExecutionContext) extends Effect[Future] {
      def pure[A](a: => A): Future[A]                                = Future.successful(a)
      def flatMap[A, B](fa: Future[A])(f: A => Future[B]): Future[B] = fa.flatMap(f)
      def map[A, B](fa: Future[A])(f: A => B): Future[B]             = fa.map(f)
      def sequencePar[A](list: List[Future[A]]): Future[List[A]]     = Future.sequence(list)
    }
  }
}
