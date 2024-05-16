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
package client.sttp

import typeclass.Effect

import _root_.sttp.client3.{Request, SttpBackend}

case class SttpStatusCodeHealthCheck[F[_], T](call: Request[T, Nothing])(implicit
    backend: SttpBackend[F, Nothing],
    F: Effect[F],
) extends HealthCheck[F](name = s"${call.method} ${call.uri}") {
  def check: F[HealthState] =
    F.map(backend.send(call)) { response =>
      HealthState(status = !response.isClientError && !response.isServerError)
    }
}
