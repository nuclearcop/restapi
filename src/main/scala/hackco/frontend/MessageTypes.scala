package hackco.frontend

/**
 * Created by robert courtney on 12/03/15
 */

object MessageTypes {
  final case class TransformationJob(text: String)
  final case class TransformationResult(text: String)
  final case class JobFailed(reason: String)
  final case class DoubleNumberRequest(number: Long)
  final case class DoubleNumberReply(number: Long, result: Long)
  final case class FailedRequest(error: String)
  case object BackendRegistration
}
