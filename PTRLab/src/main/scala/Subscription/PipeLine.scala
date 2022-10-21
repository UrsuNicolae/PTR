package Subscription

import Topics.CreateListener
import akka.actor.{Actor, ActorRef}

import scala.collection.mutable.ListBuffer

class PipeLine extends Actor {
  //todo add invocation to worker actor
  var listener_list = new ListBuffer[ActorRef]()

  override def receive: Receive = {
    case createListener: CreateListener =>
      //todo add subscription to listener actor

    case _ =>
      Console.println("Unknown message")
  }
}
