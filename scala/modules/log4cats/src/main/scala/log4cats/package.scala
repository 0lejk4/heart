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

import typeclass.Effect

import org.typelevel.log4cats.MessageLogger

package object log4cats {
  def logged[F[_]](
      check: HealthCheck[F]
  )(label: String)(implicit logger: MessageLogger[F], F: Effect[F]): HealthCheck[F] =
    check.transform[F] { doCheck =>
      F.flatMap(logger.debug(s"Checking health for $label")) { _ =>
        F.flatMap(doCheck) { result =>
          F.map(logger.debug(s"Health result for $label: $result")) { _ =>
            result
          }
        }
      }
    }
}
