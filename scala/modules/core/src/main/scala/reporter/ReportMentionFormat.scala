package jap.heart
package reporter

trait ReportMentionFormat  {
  def format(mention: ReportMention): String
}
object ReportMentionFormat {
  import ReportMention.*

  val slack: ReportMentionFormat = {
    case User(id)  => s"<@$id>"
    case Group(id) => s"<!subteam^$id>"
    case Here      => "<!here>"
    case Channel   => "<!channel>"
    case Everyone  => "<!everyone>"
  }

  val telegram: ReportMentionFormat = {
    case User(id)  => s"@$id"
    case Group(id) => s"@$id"
    case Here      => "here"
    case Channel   => "channel"
    case Everyone  => "everyone"
  }
}
