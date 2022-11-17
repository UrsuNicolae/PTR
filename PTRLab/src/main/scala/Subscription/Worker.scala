package Subscription

import TCP.Client
import Topics.{Subscribe, JsonMessage, Unsubscribe}
import akka.actor.{Actor, ActorRef, Props}
import akka.util.ByteString

import scala.collection.mutable.ArrayBuffer

/** Actor which will perform subscription and unsubcription*/
class Worker() extends Actor {
  var topicsConsumer: Map[String, ArrayBuffer[String]] = Map[String, ArrayBuffer[String]]()
  var consumerTopics: Map[String, ArrayBuffer[String]] = Map[String, ArrayBuffer[String]]()
  var consumerActors: Map[String, ActorRef] = Map[String, ActorRef]()

  override def receive: Receive = {
    case subscribe: Subscribe =>
      // Check consumer topic count if it exists
      if (!consumerTopics.contains(subscribe.consumerAddress)) {
        // If it doesn't exist, create a new count
        consumerTopics += (subscribe.consumerAddress -> ArrayBuffer[String]())
        val consumer_actor = context.actorOf(Props(new Client(subscribe.consumerAddress)), subscribe.consumerAddress)
        consumerActors += (subscribe.consumerAddress -> consumer_actor)
      }

      for (topic <- subscribe.topicArray) {
        consumerTopics(subscribe.consumerAddress) += topic

        if (topicsConsumer.contains(topic)) {
          topicsConsumer(topic) += subscribe.consumerAddress
        } else {
          topicsConsumer += (topic -> ArrayBuffer(subscribe.consumerAddress))
        }
      }

    case unsubscribe: Unsubscribe =>
      for (topic <- unsubscribe.topicArray) {
        topicsConsumer(topic) -= unsubscribe.consumerAddress
        consumerTopics(unsubscribe.consumerAddress) -= topic
      }

      if (consumerTopics(unsubscribe.consumerAddress).isEmpty) {
        topicsConsumer -= unsubscribe.consumerAddress
        consumerActors(unsubscribe.consumerAddress) ! "stop"
        consumerTopics -= unsubscribe.consumerAddress
        consumerActors -= unsubscribe.consumerAddress
      }

    case message: JsonMessage =>
      val topic = message.getTopic
      // Check if topic is already present in the map
      if (topicsConsumer.contains(topic)) {
        // Get the array of consumers for the topic
        val consumer_arr = topicsConsumer(topic)
        // For each consumer, send the message
        for (consumer_address <- consumer_arr) {
          val bytes = message.json.toString.getBytes("utf-8")
          consumerActors(consumer_address) ! ByteString.fromArray(bytes)
        }
      }
  }}

