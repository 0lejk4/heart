package jap.heart
package config

case class HeartClientConfig(
    url: String,
    auth: HeartAuth = HeartAuth.Public,
    headers: Map[String, String] = Map.empty,
    params: Map[String, String] = Map.empty,
    name: Option[String] = None,
) extends HeartAuthBuilder[HeartClientConfig] {
  val title: String                                = name.getOrElse(url)
  val reportUrl: String                            = url + "/health/" + ReportPath
  val statusUrl: String                            = url + "/health/" + StatusPath
  val componentsUrl: String                        = url + "/health/" + ComponentsPath
  def withAuth(auth: HeartAuth): HeartClientConfig = copy(auth = auth)
}
