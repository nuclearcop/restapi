package hackco.frontend

import akka.actor._
import akka.io.IO
import com.typesafe.scalalogging.slf4j.Logging
import hackco.frontend.SprayRequests.MyJsonProtocol
import spray.can.Http
import spray.http.MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol
import spray.routing.HttpService
import hackco.frontend.MessageTypes.{FailedRequest, DoubleNumberReply, DoubleNumberRequest}
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Await
import hackco.frontend.MessageTypes.DoubleNumberRequest
import scala.util.Random

/**
 * Created by robert courtney on 12/03/15
 */

class SprayApiServiceActor(frontEnd: ActorRef) extends Actor with ActorLogging with HttpService {
  def actorRefFactory = context
  def receive = runRoute(sprayRoute)

  import MyJsonProtocol._

  implicit val timeout = Timeout.apply(10 seconds) // 10 second timeout
  implicit val execCtx = context.system.dispatcher

  val sprayRoute =
    pathPrefix("double") {
      path(LongNumber) { number =>
        respondWithMediaType(`application/json`) {
          complete {
            val myFuture = frontEnd ? DoubleNumberRequest(number)
            val result = Await.result(myFuture, 10 seconds)
            result match {
              case reply: DoubleNumberReply =>
                log.info("result: " + result.toString)
                reply
              case _ =>
                FailedRequest("Could not double your number. Sorry.")
            }
          }
        }
      } ~
        path("rand") {
          respondWithMediaType(`application/json`) {
            complete {
              val myFuture = frontEnd ? DoubleNumberRequest(Random.nextLong().abs)
              val result = Await.result(myFuture, 10 seconds)
              result match {
                case reply: DoubleNumberReply =>
                  log.info("result: " + result.toString)
                  reply
                case _ =>
                  FailedRequest("Could not double your number. Sorry.")
              }
            }
          }
        }
    }
}

//class SprayApi { }
