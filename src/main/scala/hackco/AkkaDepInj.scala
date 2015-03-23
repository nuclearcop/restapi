package hackco

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by robertcourtney on 13/03/15
 */


class Pollee1 extends Actor with ActorLogging {
  override def receive: Actor.Receive = {
    case _ => sender() ! "pong"
  }
}

class Poller1(childFactory: (ActorContext, String) => ActorRef) extends Actor with ActorLogging {
  lazy val child = childFactory(context, "pollee")
  override def receive: Actor.Receive = {
    case _ if sender() != child => child ! "ping"
    case reply => log.info("reply: " + reply)
  }
}

object AkkaDepInjBoot extends App {
  implicit val system = ActorSystem("mySystem")
  val childFactory = (ctx: ActorContext, name: String) => ctx.actorOf(Props[Pollee1], name)
  val poller = system.actorOf(Props(classOf[Poller1], childFactory), "testablePoller")
  system.scheduler.schedule(0 seconds, 2 seconds, poller, "start")
}
