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

sealed trait HealthExtra

object HealthExtra {
  implicit def fromInt(value: Int): IntExtra = IntExtra(value)
  implicit def fromDouble(value: Double): DoubleExtra = DoubleExtra(value)
  implicit def fromString(value: String): StringExtra = StringExtra(value)
  implicit def fromBoolean(value: Boolean): BooleanExtra = BooleanExtra(value)
  implicit def fromList(value: List[IntExtra]): ArrayExtra = ArrayExtra(value.toVector)
  implicit def fromObject(value: Map[String, HealthExtra]): ObjectExtra = ObjectExtra(value)
}

case object NoExtra                                      extends HealthExtra
case class IntExtra(value: Int)                          extends HealthExtra
case class DoubleExtra(value: Double)                    extends HealthExtra
case class StringExtra(value: String)                    extends HealthExtra
case class BooleanExtra(value: Boolean)                  extends HealthExtra
case class ArrayExtra(values: Vector[HealthExtra])       extends HealthExtra
case class ObjectExtra(fields: Map[String, HealthExtra]) extends HealthExtra
