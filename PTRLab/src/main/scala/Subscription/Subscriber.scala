package Subscription

import TCP.Manager
import Topics.{Subscribe, JsonMessage, Unsubscribe}
import akka.actor.{Actor, ActorRef, ActorSelection, Props}

import scala.collection.mutable.ArrayBuffer

class Subscriber extends Actor {
  val Manager: ActorRef = context.actorOf(Props(new Manager("localhost", 9999)))
  val worker: ActorSelection = context.actorSelection("akka://default/user/pipeline/worker")

  override def receive: Receive = {
    case message: JsonMessage =>
      var operation_type = ""
      try {
        /** GetOperation type*/
        operation_type = message.getField("operation").toString().replace("\"", "")
      } catch {
        case e: Exception =>
          Console.println(s"Error: ${e.getMessage}")
      }

      if (operation_type == "subscribe") {
        val topic_arr = message.getField("topic").toString().replace("[", "").replace("]", "").split(',')
        val consumer_address = message.getField("consumer_address").toString().replace("\"", "")

        /** subscribe worker*/
        worker ! Subscribe(consumer_address, topic_arr.to(ArrayBuffer))
      }
      else if (operation_type == "unsubscribe") {
        val topic_arr = message.getField("topic").toString().replace("[", "").replace("]", "").split(',')
        val consumer_address = message.getField("consumer_address").toString().replace("\"", "")

        /** unsubscribe worker*/
        worker ! Unsubscribe(consumer_address, topic_arr.to(ArrayBuffer))
      }
      else {
        /** unknown operation type*/
        Console.println("Error: Unknown operation type")
      }
  }
}
