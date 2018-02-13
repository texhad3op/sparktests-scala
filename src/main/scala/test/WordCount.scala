package test

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.FATAL)
    val conf = new SparkConf().setAppName("wordCount")
    val sc = new SparkContext(conf)
    // Load our input data.
    val input = sc.textFile("C:\\files\\README.md")
    // Split it up into words.
    val words = input.flatMap(line => line.split(" "))
    // Transform into pairs and count.
    val counts = words.map(word => (word, 1)).reduceByKey { case (x, y) => x + y }
    // Save the word count back out to a text file, causing evaluation.
    counts.persist()
    println(counts.count())
  }
}
