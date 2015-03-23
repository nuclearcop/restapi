package hackco.frontend

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import java.util.concurrent.atomic.AtomicInteger
import hackco.frontend.MessageTypes.{DoubleNumberRequest, BackendRegistration, JobFailed, TransformationJob}
import scala.concurrent.duration._
import akka.io.IO
import spray.can.Http

/**
 * Created by robert courtney on 12/03/15
 */

object FrontEnd {
  case class ListeningPorts(seedNodePort: Int, httpPort: Int)

  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val ports = args.size match {
      case i: Int if i == 0 => ListeningPorts(0, 9000)
      case i: Int if i == 1 => ListeningPorts(args(0).toInt, 9000)
      case i: Int if i > 1  => ListeningPorts(args(0).toInt, args(1).toInt)
    }
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=" + ports.seedNodePort).
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.load())

    implicit val system = ActorSystem("ClusterSystem", config)
    val frontend = system.actorOf(Props[FrontEnd], "frontend")
    val sprayApiActor = system.actorOf(Props(classOf[SprayApiServiceActor], frontend), "sprayApiActor")
    IO(Http) ! Http.Bind(sprayApiActor, interface = "0.0.0.0", port = ports.httpPort)
  }
}

class FrontEnd extends Actor with ActorLogging {

  var backends = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  def receive = {
    case job: TransformationJob if backends.isEmpty =>
      sender() ! JobFailed("Service unavailable, try again later")

    case job: TransformationJob =>
      jobCounter += 1
      backends(jobCounter % backends.size) forward job

    case job: DoubleNumberRequest if backends.isEmpty =>
      sender() ! JobFailed("Service unavailable, try again later")

    case job: DoubleNumberRequest =>
      log.info("Frontend received request: " + job.number)
      jobCounter += 1
      backends(jobCounter % backends.size) forward job

    case BackendRegistration if !backends.contains(sender()) =>
      log.info("New backend registration " + sender())
      context watch sender()
      backends = backends :+ sender()

    case Terminated(a) =>
      backends = backends.filterNot(_ == a)
  }
}
