package test

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object Calc {
  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.FATAL)
    val logFile = "C:\\spark-2.2.1-bin-hadoop2.7\\README.md" // Should be some file on your system
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    val logData = spark.read.textFile(logFile).cache()


    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")
    spark.stop()


    val conf = new SparkConf().setAppName("Simple Application")




//    val blankLines = sc.accumulator(0) // Create an Accumulator[Int] initialized to 0
//    val callSigns = file.flatMap(line => {
//      if (line == "") {
//        blankLines += 1 // Add to the accumulator
//      }l
//        ine.split(" ")
//    })
//    callSigns.saveAsTextFile("output.txt")
//    println("Blank lines: " + blankLines.value)
  }
}
