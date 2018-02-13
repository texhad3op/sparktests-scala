package test

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{Row, SparkSession}

object JDBCTest {

  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.FATAL)
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()

    val pointsQuery =
      """
    (select login,latitude,longitude  from customers as c inner join tracks as t on c.id = t.customerid
    inner join points as p on t.id = p.trackid ) emp_alias"""

    val jdbcDF = spark.read
      .format("jdbc")
      .option("driver", "org.postgresql.Driver")
      .option("url", "jdbc:postgresql://localhost:5432/jgtracker")
      //.option("dbtable", "public.customers")
      .option("dbtable", pointsQuery)
      .option("user", "postgres")
      .option("password", "rootpassword")
      .load()


    val rdd = jdbcDF.rdd
    println("--------->" + rdd.count())

    rdd.foreach(row => {
      println(row)
    })

    println("---------------------------------------")
    val first: Row = rdd.first()
    println(first.getDecimal(1) + "----" + first.getDecimal(2))


    //    val pairs = rdd.map { row => {
    //      (row.getDecimal(1), row.getDecimal(2))
    //    }
    //    }.collect()
    //
    //    pairs.foreach(row => {
    //      println(row)
    //    })
    //    val last:Row = rdd.take(rdd.count().toInt)(0)
    //    println(last.getDecimal(1)+"----"+last.getDecimal(2))


    //println(rdd.take(rdd.count().toInt)(1))

  }


//  def measure(lat1: Double, lon1: Double, lat2: Double, lon2: Double) { // generally used geo measurement function
//    val R = 6378.137; // Radius of earth in KM
//    val dLat = Double(lat2 * Math.PI / 180 - lat1 * Math.PI / 180);
//    val dLon = Double(lon2 * Math.PI / 180 - lon1 * Math.PI / 180);
//    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
//        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//    val d = R * c;
//    return d * 1000; // meters
//  }

}
