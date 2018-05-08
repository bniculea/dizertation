package ro.uvt.lambda

import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import ro.uvt.lambda.Utils._


object PopularHashtags {

  def main(args: Array[String]): Unit ={
    setupTwitter()

    val ssc = new StreamingContext("local[*]", "PopularHashtags", Seconds(1))
    setupLogging()
    val tweets = TwitterUtils.createStream(ssc, None)

    //Extract the text of each status update into DStreams using Map
    val statuses = tweets.map(status=> status.getText)

    //low out each word into a new DStream
    val tweetWords = statuses.flatMap(tweetText=>tweetText.split(" "))

    // eliminate everything which is not a hashtag
    val hashtags = tweetWords.filter(word => word.startsWith("#"))

    //map each hashtag to a key/value pair of (hashtag, 1) so we can count them up by adding the values
    val hashtagKeyValue = hashtags.map(hashtag => (hashtag, 1))

    //now count them up over a 5 minute window sliding every one second

    val hashtagCounts = hashtagKeyValue.reduceByKeyAndWindow( (x, y) => x+y,(x,y)=> x-y, Seconds(300), Seconds(1))

    val sortedResults = hashtagCounts.transform(rdd => rdd.sortBy(x => x._2, false))

    // top words
    val  wordsKeyValues = tweetWords.filter(word => word.length > 4).map(word => (word, 1))
    val wordsCounts = wordsKeyValues.reduceByKeyAndWindow( (x, y)=> x + y, (x,y) => x-y, Seconds(300), Seconds(1))
    val sortedWords = wordsCounts.transform(rdd => rdd.sortBy(x => x._2, false))


    // print the top 10
    sortedResults.print()

    println("and words: ")
    sortedWords.print()

    ssc.checkpoint("../resources/save")
    ssc.start()
    ssc.awaitTermination()
  }

}
