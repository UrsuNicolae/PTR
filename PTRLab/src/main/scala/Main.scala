import Subscription.{PipeLine, Subscriber}
import Topics.CreateListener
import akka.actor.{ActorSystem, Props}

object Main {
  def main(args: Array[String]): Unit ={
    Console.println("Starting message broker");
    val system = ActorSystem()
    val subscriber = system.actorOf(Props[Subscriber], "subscriber")
    val pipeline = system.actorOf(Props[PipeLine], "pipeline")
    var port = 9000
    pipeline ! CreateListener(port)
    port += 1
    pipeline ! CreateListener(port)
    
  }
}
