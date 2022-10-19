package TCP

import akka.actor.{Actor, Props}
import akka.io.Tcp.{Bind, Bound, Connected, Message, Register}
import akka.io.{IO, Tcp}

import java.net.InetSocketAddress

/**
 * Tcp manager
 * @param host client host
 * @param port client port
 * Part of code is from @see [[https://github.com/akka/akka/blob/v2.6.20/akka-docs/src/test/scala/docs/io/IODocSpec.scala#L29-L53]]
 * Also @see [[https://doc.akka.io/docs/akka/current/io-tcp.html]] for additional explanation
 */
class Manager(host: String, port: Int) extends Actor{
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress(host, port))

  override def receive: Receive = {
    case Bound(local) =>
      Console.printf(s"Server listening on $local")
    case Connected(remote, local) =>
      Console.printf(s"New connnection: $local -> $remote")
      val handler = context.actorOf(Props[Handler])
      sender() ! Register(handler)
      handler ! remote.toString
    case jsonMessage: Message =>
      context.parent ! jsonMessage
  }
}
