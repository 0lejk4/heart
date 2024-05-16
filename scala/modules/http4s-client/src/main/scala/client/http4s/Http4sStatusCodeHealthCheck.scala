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
package client.http4s

import cats.Functor
import cats.syntax.all.*
import org.http4s.Request
import org.http4s.client.Client

case class Http4sStatusCodeHealthCheck[F[_]: Functor](call: Request[F])(implicit client: Client[F])
    extends HealthCheck[F](name = s"${call.method} ${call.uri}") {
  def check: F[HealthState] = client.status(call).map(_.isSuccess).map(HealthState.healthyIf)
}
