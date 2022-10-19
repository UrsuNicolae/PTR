package TCP

import akka.actor.Actor
import akka.io.Tcp.{ConnectionClosed, Received, Write}
import akka.util.ByteString

/**
 * Tcp Handler
 * Purpose of this class is to handle tcp requests
 * * Also @see [[https://doc.akka.io/docs/akka/current/io-tcp.html]] for additional explanation
 */
class Handler extends Actor{
  var remote: String = ""

  override def receive: Actor.Receive = {
    /** Receive encoded data*/
    case Received(data) =>
      val decoded = data.decodeString("utf-8")
      //todo handle decoded data
      sender() ! Write(ByteString("OK"))
    case remote: String =>
      this.remote = remote
    case _: ConnectionClosed =>
      Console.printf("Connection has been closed")
      context stop self
  }
}
