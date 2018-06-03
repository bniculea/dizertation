package ro.uvt.lambda

import java.util.{Calendar, Date}

import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import com.datastax.spark.connector._
import Utils._
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import twitter4j.Status

object TweetStore {
  def main(args: Array[String]): Unit = {
    setupTwitter(args(1))
    val streamingContext = setupStreamingContext(args(0))
    setupLogging()
    val tweets: ReceiverInputDStream[Status] =TwitterUtils.createStream(streamingContext, None)
    var totalTweets:Long = 0
    tweets.foreachRDD( (rdd, time) => {
      if (rdd.count> 0) {
        val stream = rdd.map(t => ( Calendar.getInstance().getTime, extractHour(t.getCreatedAt), extractDeviceType(t.getSource)))
        stream.saveToCassandra("streaming", "tweets", SomeColumns("insertdate", "hour", "device"))
        totalTweets += rdd.count
        println("tweets so far: " + totalTweets)
      }
    })
    streamingContext.checkpoint("../resources/save/cass")
    streamingContext.start()
    streamingContext.awaitTermination()
  }

  def extractDeviceType(source: String): String = {
      val firstIndexOfRightChevron = source.indexOf('>')
      val lastIndexOfLeftChevron = source.lastIndexOf('<')
      source.slice(firstIndexOfRightChevron+1, lastIndexOfLeftChevron)
  }

  def extractHour(date:Date): Int={
    date.getHours()
  }

  def setupStreamingContext(cassandraSeedIp:String):StreamingContext={
    val conf = new SparkConf()
    conf.set("spark.cassandra.connection.host", cassandraSeedIp)
    conf.setAppName("LambdaArchitecture")
    new StreamingContext(conf, Seconds(2))
  }
}
