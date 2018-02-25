package de.guysoft.scaladsl.service

import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents

class UploadServiceApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new UploadServiceApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new UploadServiceApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[UploadService])
}
