name := "Notes"
 
version := "1.0" 
      
lazy val `notes` = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  ehcache , ws , specs2 % Test , guice,
  "org.webjars" % "swagger-ui" % "3.24.3",
  "org.postgresql" % "postgresql" % "42.2.14",
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "org.flywaydb" % "flyway-core" % "6.1.4",
  "org.flywaydb" %% "flyway-play" % "6.0.0",
  "com.typesafe.play" %% "play-mailer" % "8.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "8.0.0"
)

swaggerDomainNameSpaces := Seq("model")

//unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

