package TCP

import akka.actor.Actor
import akka.io.{IO, Tcp}
import akka.io.Tcp.{CommandFailed, Connect, Connected, ConnectionClosed, Register, Write}
import akka.util.ByteString

import java.net.InetSocketAddress

/**
 * Tcp client
 * @param host client host
 * @param port client port
 * Part of code is from @see [[https://github.com/akka/akka/blob/v2.6.20/akka-docs/src/test/scala/docs/io/IODocSpec.scala#L67-L103]]
 * Also @see [[https://doc.akka.io/docs/akka/current/io-tcp.html]] for additional explanation
 */
class Client (address:String) extends Actor {
  val host: String = address.split(":")(0)
  val port: Int = address.split(":")(1).toInt
  import context.system

  IO(Tcp) ! Connect(new InetSocketAddress(host, port))

  override def receive: Receive = {
    case CommandFailed(_: Connect) =>
      Console.println("Could not connect")
      context.stop(self)

    /** Connecting to remote address*/
    case c@Connected(remote, local) =>
      val connection = sender()
      connection ! Register(self)
      context.become {
        case data: ByteString =>
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          Console.printf("Failed to write data!")
        case _: ConnectionClosed =>
          context.stop(self)
        case "stop" =>
          context.stop(self)
        case _ =>
          Console.println("Connected with unknown message!")
      }
    case _ =>
      Console.println("Unknown message!")
  }
}
