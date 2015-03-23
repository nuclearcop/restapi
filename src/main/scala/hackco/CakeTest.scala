package hackco

import akka.actor._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.testkit.{TestActorRef, TestProbe}

// Execution context
import scala.concurrent.duration._

/**
 * Created by robert courtney on 13/03/15
 */

class Pollee extends Actor with ActorLogging {
  override def receive: Actor.Receive = {
    case _ => sender() ! "pong"
  }
}

class Poller extends Actor with ActorLogging {
  lazy val child = context.actorOf(Props[Pollee], "pollee")
  override def receive: Actor.Receive = {
    case _ if sender() != child => child ! "ping"
    case reply => log.info("reply: " + reply)
  }
}

trait PollerChild {
  val child: ActorRef
}

trait PollerChildImpl extends PollerChild { this: Actor =>
  val child = context.actorOf(Props[Pollee], "pollee")
}

trait PollerChildTestImpl extends PollerChild { this: Actor =>
  val probe = TestProbe()(context.system)
  val child = probe.ref
}

trait TestablePoller { this: Actor with ActorLogging with PollerChild =>
  override def receive: Actor.Receive = {
    case _ if sender() != child => child ! "ping"
    case reply => log.info("reply: " + reply)
  }
}

class TestablePollerImpl extends Actor with ActorLogging with TestablePoller with PollerChildImpl {}

class TestablePollerTestImpl extends Actor with ActorLogging with TestablePoller with PollerChildTestImpl {}

object testMyApp extends App {
  implicit val system = ActorSystem("mySystem")
  val poller = TestActorRef[TestablePollerTestImpl](Props[TestablePollerTestImpl], "testablePoller")
  poller ! "start"
  val probe = poller.underlyingActor.probe
  probe.expectMsg("ping")
  probe.send(poller, "test pong")
  system.shutdown()
}

object MyBoot extends App {
  implicit val system = ActorSystem("mySystem")
//  val poller = system.actorOf(Props[Poller], "poller")
  val poller = system.actorOf(Props[TestablePollerImpl], "testablePoller")
  system.scheduler.schedule(0 seconds, 2 seconds, poller, "start")
}
