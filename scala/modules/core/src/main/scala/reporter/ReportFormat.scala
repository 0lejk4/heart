package jap.heart
package reporter

import config.HeartClientConfig
import util.StringTemplate

trait ReportFormat {
  def apply(server: HeartClientConfig, config: ReportSenderConfig, report: HealthReport): String
}

object ReportFormat {
  val slack    = Default(ReportMentionFormat.slack)
  val telegram = Default(ReportMentionFormat.telegram)

  case class Default(mentionFormat: ReportMentionFormat) extends ReportFormat {
    def apply(server: HeartClientConfig, config: ReportSenderConfig, report: HealthReport): String =
      s"""|${formatTitle(server.title, config, report)}
          |
          |${formatComponents(config, report.components)}
          |
          |${formatMentions(config)}""".stripMargin

    def formatTitle(name: String, config: ReportSenderConfig, report: HealthReport) =
      StringTemplate.format(
        config.titleTemplate,
        Map("server.name" -> name, "report.status" -> report.status.toString),
      )

    def formatMentions(config: ReportSenderConfig) =
      if (config.mentions.isEmpty) ""
      else
        StringTemplate.format(
          config.mentionsTemplate,
          Map("mentions" -> config.mentions.map(mentionFormat.format).mkString(" ")),
        )

    def formatComponents(config: ReportSenderConfig, components: List[HealthReportComponent]) =
      components
        .map { c =>
          StringTemplate.format(
            config.componentsTemplate,
            Map("component" -> c.component, "status" -> c.status.toString),
          )
        }
        .mkString("\n")
  }
}
