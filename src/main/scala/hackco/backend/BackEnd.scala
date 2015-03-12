package hackco.backend

import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.cluster.{Member, MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import hackco.frontend.MessageTypes._
import hackco.frontend.MessageTypes.TransformationResult
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import hackco.frontend.MessageTypes.DoubleNumberRequest
import hackco.frontend.MessageTypes.TransformationJob
import hackco.frontend.MessageTypes.DoubleNumberReply
import hackco.frontend.MessageTypes.TransformationResult
import akka.actor.RootActorPath
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import hackco.frontend.MessageTypes.DoubleNumberRequest
import hackco.frontend.MessageTypes.TransformationJob

/**
 * Created by robert courtney on 12/03/15
 */

object BackEnd {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[BackEnd], name = "backend")
  }
}

class BackEnd extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case TransformationJob(text) => sender() ! TransformationResult(text.toUpperCase)
    case DoubleNumberRequest(number) =>
      log.info("Backend received request: " + number)
      sender() ! DoubleNumberReply(number, number*2)
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) => register(m)
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend"))
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") !
        BackendRegistration
}
