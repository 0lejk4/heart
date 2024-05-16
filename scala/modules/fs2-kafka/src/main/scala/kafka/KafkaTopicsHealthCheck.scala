package jap.heart
package kafka

import cats.MonadThrow
import cats.implicits.*
import fs2.kafka.KafkaAdminClient
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException

case class KafkaTopicsHealthCheck[F[_]](client: KafkaAdminClient[F])(topicNames: String*)(implicit F: MonadThrow[F])
    extends HealthCheck[F](
      name = s"kafka-topics[${topicNames.mkString(",")}]",
      tags = List("kafka"),
    ) {
  def check: F[HealthState] =
    client
      .describeTopics(topicNames.toList)
      .map(result => topicNames.forall(result.keySet.contains))
      .recover { case _: UnknownTopicOrPartitionException => false }
      .map(HealthState.healthyIf)
}
