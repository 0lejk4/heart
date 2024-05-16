package jap.heart.reporter

sealed trait ReportMention
object ReportMention {
  case class User(id: String)  extends ReportMention
  case class Group(id: String) extends ReportMention
  case object Here             extends ReportMention
  case object Channel          extends ReportMention
  case object Everyone         extends ReportMention
}
