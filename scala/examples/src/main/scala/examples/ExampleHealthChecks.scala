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
package examples

import zio.*

case class PostgresHealthCheck(jdbcUrl: String)
    extends HealthCheck[Task](
      name = "postgres",
      description = Some("Checks that connection with Postgres is alright"),
      tags = List("database", "postgres"),
      extra = Map("jdbcUrl" -> StringExtra(jdbcUrl)),
    ) {
  val check: Task[HealthState] =
    ZIO.ifZIO(Random.nextBoolean)(
      onTrue = ZIO.succeed(HealthState.healthy()),
      onFalse = ZIO.fail(new RuntimeException("Cannot connect to postgres")),
    )
}

case class DelayedHealthCheck(url: String)
    extends HealthCheck[Task](
      name = "delayed",
      description = Some("Delayed check"),
      tags = List("delayed"),
      extra = Map("delay" -> StringExtra("1 second")),
    ) {
  val check: Task[HealthState] =
    ZIO
      .succeed(HealthState.healthy(message = Some("All looks good")))
      .delay(1.second)
}
