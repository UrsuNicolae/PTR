import play.api.libs.json._
import scala.collection.mutable.ArrayBuffer

case class Topic(stringTopic: String, sender: String = ""){
  /** @see [[https://github.com/playframework/play-json]]*/
  val json: JsValue = Json.parse(stringTopic)

  def getStringTopic: String = {
    stringTopic
  }

  /** @see [[https://github.com/playframework/play-json]]*/
  def getTopic: String = {
    (json \ "message" \ "tweet" \ "user" \ "time_zone").getOrElse(JsNull).toString()
  }

  /** @see [[https://github.com/playframework/play-json]]*/
  def getField(field: String): JsValue = {
    (json \ field).getOrElse(JsNull)
  }
}

case class Subscribe(consumerAddress: String, topicArray: ArrayBuffer[String])

case class Unsubscribe(consumerAddress: String, topicArray: ArrayBuffer[String])

case class CreateListener(port: Int)