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

import _root_.zio.*

object zio {
  private[this] val effectInstance0: ZIOEffect[Any, Nothing]         = new ZIOEffect[Any, Nothing]
  private[this] val timeoutInstance0: ZIOTimeoutEffect[Any, Nothing] = new ZIOTimeoutEffect[Any, Nothing]
  private[this] val errorInstance0: ZIOErrorEffect[Any, Nothing]     = new ZIOErrorEffect[Any, Nothing]

  /** [[jap.heart.typeclass.Effect]] and [[jap.heart.typeclass.DeferEffect]] instance for `zio.ZIO` */
  implicit def zioEffect[R, E]: Effect[ZIO[R, E, _]] & DeferEffect[ZIO[R, E, _]] =
    effectInstance0.asInstanceOf[Effect[ZIO[R, E, _]] & DeferEffect[ZIO[R, E, _]]]

  /** [[jap.heart.typeclass.TimeoutEffect]] instance for `zio.ZIO` */
  implicit def zioTimeoutEffect[R, E]: TimeoutEffect[ZIO[R, E, _]] =
    timeoutInstance0.asInstanceOf[TimeoutEffect[ZIO[R, E, _]]]

  /** [[jap.heart.typeclass.ErrorEffect]] instance for `zio.ZIO` */
  implicit def zioErrorEffect[R, E]: ErrorEffect[ZIO[R, E, _], E] =
    errorInstance0.asInstanceOf[ErrorEffect[ZIO[R, E, _], E]]

  implicit def zioDurationToScalaDuration(duration: _root_.zio.Duration): scala.concurrent.duration.FiniteDuration =
    duration.asScala.asInstanceOf[scala.concurrent.duration.FiniteDuration]

  implicit class HealthApiOps[R, E](private val api: HealthApi[ZIO[R, E, *]]) extends AnyVal {
    def catchAll[R1 <: R, E2](h: E => ZIO[R1, E2, HealthState])(implicit ev: CanFail[E]): HealthApi[ZIO[R1, E2, *]] =
      api.transform(_.catchAll(h))
  }
}
