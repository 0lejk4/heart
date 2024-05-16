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

import java.util.Arrays

sealed trait HeartAuth
object HeartAuth {
  val HeartRealm = "heart-health-api"

  case object Public extends HeartAuth

  case class Basic(username: Array[Char], password: Array[Char]) extends HeartAuth {
    def verify(u: String, p: String): Boolean =
      Arrays.equals(u.toCharArray, username) && Arrays.equals(p.toCharArray, password)
      override def toString: String = "Basic"
  }

  object Basic {
    val dummy = Basic("", "")

    def apply(username: String, password: String): Basic = new Basic(username.toCharArray, password.toCharArray)
  }

  case class Secret(secret: Array[Char]) extends HeartAuth {
    def verify(s: String): Boolean = Arrays.equals(s.toCharArray, secret)
    override def toString: String = "Secret"
  }

  object Secret {
    val dummy = Secret("")

    def apply(secret: String): Secret = Secret(secret.toCharArray)
  }
}
