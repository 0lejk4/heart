package jap.heart
package bot

import config.*

import scala.concurrent.duration.*

case class HealthBotConfig(
    servers: List[HeartClientConfig],
    interval: FiniteDuration = 10.seconds,
)
