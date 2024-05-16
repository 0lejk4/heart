package jap.heart
package server.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.*

object implicits {
  implicit def akkaStreamTicker(implicit system: ActorSystem) = AkkaStreamTicker()
  implicit def akkaHealthFetcher(implicit
      system: ActorSystem,
      reportUnmarshaller: FromResponseUnmarshaller[HealthReport],
      statusUnmarshaller: FromResponseUnmarshaller[HealthStatus],
      componentUnmarshaller: FromResponseUnmarshaller[HealthComponent],
  ) = AkkaHealthFetcher()
}
