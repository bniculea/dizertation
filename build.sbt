
name := "dizertation"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.datastax.spark" % "spark-cassandra-connector_2.11" % "2.0.0",
  "org.apache.spark" % "spark-core_2.11" % "2.1.0" % "provided",
  "org.apache.spark" % "spark-sql_2.11" % "2.1.0" % "provided",
  "org.apache.spark" % "spark-streaming_2.11" % "2.1.0" % "provided",
  "org.twitter4j" % "twitter4j-core" % "4.0.4",
  "org.twitter4j" % "twitter4j-stream" % "4.0.4",
  "org.apache.bahir" % "spark-streaming-twitter_2.11" % "2.1.1"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}