package jap.heart
package reporter

trait MessageSender[F[_]] {
  def sendMessage(channel: String, message: String): F[Unit]
}
