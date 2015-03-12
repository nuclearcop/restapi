package hackco.frontend

import spray.json.DefaultJsonProtocol
import hackco.frontend.MessageTypes.{FailedRequest, DoubleNumberReply}

/**
 * Created by robert courtney on 12/03/15
 */

case class Person(name: String, age: Int)
case class DoubleReply(number: Long, result: Long)

object SprayRequests {
  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val personFormat = jsonFormat2(Person)
    implicit val doubleReplyFormat = jsonFormat2(DoubleNumberReply)
    implicit val failedRequestFormat = jsonFormat1(FailedRequest)
  }
}

class SprayRequests {}
