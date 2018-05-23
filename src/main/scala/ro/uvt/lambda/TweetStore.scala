package ro.uvt.lambda

import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import com.datastax.spark.connector._
import Utils._

object TweetStore {
  def main(args: Array[String]): Unit = {
    setupTwitter()
    val conf = new SparkConf()
    conf.set("spark.cassandra.connection.host", args(0))
    conf.setAppName("LambdaArchitecture")

    val ssc = new StreamingContext(conf, Seconds(10))
    setupLogging()
    val tweets =TwitterUtils.createStream(ssc, None)
    var totalTweets:Long = 0
    tweets.foreachRDD( (rdd, time) => {
      if (rdd.count> 0) {
        val nameAndCountryStream = rdd.map(t => {
          if (t.getUser==null || t.getPlace == null){
            (Some("empty"), Some("empty"))
          }
          else {
            (Some(t.getUser.getName), Some(t.getPlace.getCountry))
          }
        })
        nameAndCountryStream.saveToCassandra("test", "tweets", SomeColumns("uname", "country"))
        totalTweets += rdd.count
        println("tweets so far: " + totalTweets)
        if (totalTweets > 1000) {
          System.exit(0)
        }
      }
    })

    ssc.checkpoint("../resources/save/cass")
    ssc.start()
    ssc.awaitTermination()
  }
}
