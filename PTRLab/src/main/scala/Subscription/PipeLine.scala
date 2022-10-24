package Subscription

import Topics.CreateListener
import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable.ListBuffer

class PipeLine extends Actor {
  val worker: ActorRef = context.actorOf(Props[Worker], "worker")
  var listener_list = new ListBuffer[ActorRef]()

  override def receive: Receive = {
    case createListener: CreateListener =>
      val new_listener = context.actorOf(Props[Listener])
      new_listener ! createListener
      listener_list += new_listener

    case _ =>
      Console.println("Unknown message")
  }
}
