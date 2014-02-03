name := "play2-elasticsearch-tags-heroku"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

libraryDependencies += "postgresql" % "postgresql" % "9.1-901.jdbc4"

libraryDependencies += "org.elasticsearch" % "elasticsearch" % "0.90.10"

play.Project.playScalaSettings
