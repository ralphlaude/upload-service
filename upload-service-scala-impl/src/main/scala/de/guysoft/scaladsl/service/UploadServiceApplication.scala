package de.guysoft.scaladsl.service

import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomServer}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

abstract class UploadServiceApplication(context: LagomApplicationContext) extends LagomApplication(context)
  with CassandraPersistenceComponents
  with AhcWSComponents {

  override lazy val lagomServer = LagomServer.forService(
    bindService[UploadService].to(wire[UploadServiceImpl])
  )

  override lazy val jsonSerializerRegistry = UploadServiceJsonSerializerRegistry

}