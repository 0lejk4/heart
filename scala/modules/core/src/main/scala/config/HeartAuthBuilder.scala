package jap.heart
package config

trait HeartAuthBuilder[T] {
  def withAuth(auth: HeartAuth): T

  def publicAuth: T =
    withAuth(HeartAuth.Public)

  def basicAuth(username: String, password: String): T =
    withAuth(HeartAuth.Basic(username, password))

  def secretAuth(secret: String): T =
    withAuth(HeartAuth.Secret(secret))
}
