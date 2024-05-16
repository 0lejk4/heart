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
package bot

import bot.HealthBot.logger
import config.*
import reporter.*
import typeclass.*

import org.slf4j.LoggerFactory

final case class HealthBot[F[_]: Effect: HealthFetcher: Ticker](
    config: HealthBotConfig,
    reportSender: ReportSender[F],
) {
  def checkServer(server: HeartClientConfig) = {
    logger.info(s"Check ${server.title} health")

    Effect[F].flatMap(HealthFetcher[F].fetchReport(server)) { report =>
      reportSender.send(server, report)
    }
  }

  def checkAllServers(): F[Unit] =
    Effect[F].unit {
      Effect[F].foreachPar(config.servers)(checkServer)
    }

  def start(): F[Unit] =
    Ticker[F].tick(config.interval) { () =>
      logger.info("Healh tick")
      checkAllServers()
    }
}

object HealthBot {
  val logger = LoggerFactory.getLogger(classOf[HealthBot[Any]])
}
