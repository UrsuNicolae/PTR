package Subscription

import TCP.Manager
import Topics.{Subscribe, Topic, Unsubscribe}
import akka.actor.{Actor, ActorRef, ActorSelection, Props}

import scala.collection.mutable.ArrayBuffer

class Subscriber extends Actor {
  val Manager: ActorRef = context.actorOf(Props(new Manager("localhost", 9999)))
  val worker: ActorSelection = context.actorSelection("")//todo add path to pipeline actor

  override def receive: Receive = {
    case topic: Topic =>
      var operation_type = ""
      try {
        /** GetOperation type*/
        operation_type = topic.getField("operation").toString().replace("\"", "")
      } catch {
        case e: Exception =>
          Console.printf(s"Error: ${e.getMessage}")
      }

      if (operation_type == "subscribe") {
        val topic_arr = topic.getField("topic").toString().replace("[", "").replace("]", "").split(',')
        val consumer_address = topic.getField("consumer_address").toString().replace("\"", "")

        /** subscribe worker*/
        worker ! Subscribe(consumer_address, topic_arr.to(ArrayBuffer))
      }
      else if (operation_type == "unsubscribe") {
        val topic_arr = topic.getField("topic").toString().replace("[", "").replace("]", "").split(',')
        val consumer_address = topic.getField("consumer_address").toString().replace("\"", "")

        /** unsubscribe worker*/
        worker ! Unsubscribe(consumer_address, topic_arr.to(ArrayBuffer))
      }
      else {
        /** unknown operation type*/
        Console.printf("Error: Unknown operation type")
      }
  }
}
