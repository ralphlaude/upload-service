name := "upload-service"

organization in ThisBuild := "de.guysoft"

version := "1.0"

scalaVersion in ThisBuild := "2.11.8"

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(uploadServiceJavaAPI, uploadServiceJavaImpl, uploadServiceScalaAPI, uploadServiceScalaImpl)

lazy val uploadServiceJavaAPI = (project in file("upload-service-java-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomJavadslApi, lagomJavadslImmutables, lagomLogback)
  )

lazy val uploadServiceJavaImpl = (project in file("upload-service-java-impl"))
  .enablePlugins(LagomJava)
  .settings(commonSettings: _*)
  .settings(lagomForkedTestSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslImmutables,
      lagomJavadslTestKit
    )
  )
  .dependsOn(uploadServiceJavaAPI)

lazy val uploadServiceScalaAPI = (project in file("upload-service-scala-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomScaladslApi, lagomLogback)
  )

lazy val uploadServiceScalaImpl = (project in file("upload-service-scala-impl"))
  .enablePlugins(LagomScala)
  .settings(commonSettings: _*)
  .settings(lagomForkedTestSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
    )
  ) .dependsOn(uploadServiceScalaAPI)


def commonSettings: Seq[Setting[_]] = Seq(
  javacOptions in compile ++=
    Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation", "-parameters")
)

lagomServiceGatewayImpl in ThisBuild := "akka-http"

lagomCassandraCleanOnStart in ThisBuild := true
