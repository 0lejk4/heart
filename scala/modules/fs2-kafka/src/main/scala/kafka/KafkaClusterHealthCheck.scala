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
package kafka

import cats.*
import cats.implicits.*
import fs2.kafka.KafkaAdminClient
import org.apache.kafka.common.errors.TimeoutException

case class KafkaClusterHealthCheck[F[_]](client: KafkaAdminClient[F])(implicit F: MonadThrow[F])
    extends HealthCheck[F](
      name = "kafka-cluster",
      tags = List("kafka"),
    ) {
  def check: F[HealthState] =
    client.describeCluster.clusterId
      .as(HealthState.healthy())
      .recover { case _: TimeoutException => HealthState.unhealthy() }
}
