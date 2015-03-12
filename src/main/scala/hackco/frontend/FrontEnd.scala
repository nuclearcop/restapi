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
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.load())

    implicit val system = ActorSystem("ClusterSystem", config)
    val frontend = system.actorOf(Props[FrontEnd], name = "frontend")
    val sprayApiActor = system.actorOf(Props(classOf[SprayApiServiceActor], frontend), "sprayApiActor")
    IO(Http) ! Http.Bind(sprayApiActor, interface = "localhost", port = 9001)

//    val counter = new AtomicInteger
//    import system.dispatcher
//    system.scheduler.schedule(2.seconds, 2.seconds) {
//      implicit val timeout = Timeout(5 seconds)
  //      (frontend ? TransformationJob("hello-" + counter.incrementAndGet())) onSuccess {
//        case result => println(result)
//      }
//    }

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
