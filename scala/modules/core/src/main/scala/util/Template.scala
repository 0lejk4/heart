package jap.heart.util

object StringTemplate {
  val VariableRegex = "\\$\\{([^\\}]*)\\}".r

  def format(template: String, context: Map[String, String]) =
    VariableRegex.replaceAllIn(
      template,
      m => context(m.group(1)).replace("\\", "\\\\").replace("$", "\\$"),
    )
}
