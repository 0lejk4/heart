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

case class HealthQuery(
    includeTags: List[String] = Nil,
    excludeTags: List[String] = Nil,
    includeNames: List[String] = Nil,
    excludeNames: List[String] = Nil,
) extends ((HealthComponent => Boolean)) {
  def apply(c: HealthComponent): Boolean = {
    (includeNames.isEmpty || includeNames.contains(c.name)) &&
    (excludeNames.isEmpty || !excludeNames.contains(c.name)) &&
    (includeTags.isEmpty || c.tags.exists(includeTags.contains)) &&
    (excludeTags.isEmpty || !c.tags.exists(excludeTags.contains))
  }
}

object HealthQuery {
  val empty = HealthQuery()
}
