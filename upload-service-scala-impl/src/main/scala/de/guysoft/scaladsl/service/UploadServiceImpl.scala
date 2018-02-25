package de.guysoft.scaladsl.service

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import play.api.mvc.{Result, Results}

import scala.concurrent.{ExecutionContext, Future}

class UploadServiceImpl (implicit val executionContext: ExecutionContext) extends UploadService {

  /*
   * This code does not compile and i would like to fix the problem.
   * At the end I want to write my service in the scala dsl.
   *
   * Thanks for helping.
   */

  override def uploadMedia(userId: String): ServiceCall[NotUsed, Result] = ServerServiceCall { request =>
    Future.successful(Results.Ok("file successfully uploaded"))
  }

}
