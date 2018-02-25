package de.guysoft.scaladsl.service

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.mvc.Result

trait UploadService extends Service {

  def uploadMedia(userId: String): ServiceCall[NotUsed, Result]

  final override def descriptor: Descriptor = Service.named("SCALA_UPLOAD_SERVICE").withCalls(
    Service.restCall(Method.POST, "/scala/service/media/users/:userId/upload", uploadMedia _)
  )
  .withAutoAcl(true)

}
