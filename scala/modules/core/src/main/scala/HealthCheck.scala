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

import typeclass.Effect

abstract class HealthCheck[F[_]](
    val name: String,
    val description: Option[String] = None,
    val tags: List[String] = Nil,
    val extra: Map[String, HealthExtra] = Map.empty,
) { self =>
  def check: F[HealthState]

  val component: HealthComponent = HealthComponent(name, description, tags, extra)
  def transform[K[_]](f: F[HealthState] => K[HealthState]): HealthCheck[K]         =
    new HealthCheck[K](name, description, tags, extra) { def check: K[HealthState] = f(self.check) }
  def modifyM(f: F[HealthState] => F[HealthState]): HealthCheck[F]                 = transform(f)
  def modify(f: HealthState => HealthState)(implicit F: Effect[F]): HealthCheck[F] =
    transform(F.map(_)(f))

  def copyComponent(
      name: String = self.name,
      description: Option[String] = self.description,
      tags: List[String] = self.tags,
      data: Map[String, HealthExtra] = self.extra,
  ): HealthCheck[F] =
    new HealthCheck[F](name, description, tags, data) { def check: F[HealthState] = self.check }

  def name(value: String): HealthCheck[F]                       = copyComponent(name = value)
  def description(value: Option[String]): HealthCheck[F]        = copyComponent(description = value)
  def description(value: String): HealthCheck[F]                = description(Some(value))
  def noDescription: HealthCheck[F]                             = description(None)
  def tags(value: List[String]): HealthCheck[F]                 = copyComponent(tags = value)
  def addTag(value: String): HealthCheck[F]                     = tags(tags :+ value)
  def addTags(value: String*): HealthCheck[F]                   = tags(tags ++ value)
  def noTags: HealthCheck[F]                                    = tags(Nil)
  def extra(value: Map[String, HealthExtra]): HealthCheck[F]    = copyComponent(data = value)
  def addExtra(key: String, value: HealthExtra): HealthCheck[F] = extra(self.extra + (key -> value))
  def noExtra: HealthCheck[F]                                   = extra(Map.empty)
}
