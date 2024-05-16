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
package config

case class HeartServerConfig(
    statusMapping: HeartStatusToCodeMapping = HeartStatusToCodeMapping.default,
    auth: HeartAuth = HeartAuth.Public,
    serveAdminUI: Boolean = false,
) extends HeartAuthBuilder[HeartServerConfig] {
  def withAuth(value: HeartAuth): HeartServerConfig                               = copy(auth = value)
  def withStatusToCodeMapping(value: HeartStatusToCodeMapping): HeartServerConfig = copy(statusMapping = value)
  def enableAdminUI: HeartServerConfig                                            = copy(serveAdminUI = true)
  def disableAdminUI: HeartServerConfig                                           = copy(serveAdminUI = false)

  def toView: HeartServerConfigView = HeartServerConfigView(statusMapping)
}

object HeartServerConfig {
  val default = HeartServerConfig()
}

case class HeartServerConfigView(statusMapping: HeartStatusToCodeMapping)
