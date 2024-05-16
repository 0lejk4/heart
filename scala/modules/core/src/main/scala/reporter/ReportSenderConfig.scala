package jap.heart.reporter

case class ReportSenderConfig(
    channel: String,
    mentions: List[ReportMention] = Nil,
    showComponents: Boolean = true,
    includeHealthy: Boolean = false,
    titleTemplate: String = s"$${server.name} is $${report.status}",
    mentionsTemplate: String = s"Hey $${mentions} can you check please",
    componentsTemplate: String = s"- $${component} is $${status}",
)
