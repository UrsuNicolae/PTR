package Subscription

import TCP.Manager
import Topics.{CreateListener, Topic}
import akka.actor.{Actor, ActorRef, ActorSelection, Props}

class Listener() extends Actor {
  var workerRef: ActorSelection = context.actorSelection("../worker")
  var tcpManager: ActorRef = _

  override def receive: Receive = {
    case createListener: CreateListener =>
      tcpManager = context.actorOf(Props(new Manager("localhost", createListener.port)))

    case jsTopic: Topic =>
      workerRef ! jsTopic

    case a =>
      Console.println("unknown topic:", a)
  }
}
