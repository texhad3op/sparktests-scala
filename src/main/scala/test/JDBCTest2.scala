package test

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}

case class Record(key: Int, value: String)

case class PointRecord(login: String, latitude: BigDecimal, longitude: BigDecimal)

object JDBCTest2 {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.FATAL)
    val sparkSession = SparkSession.builder.appName("Simple Application").getOrCreate()
    //operationsDB(sparkSession)
    //operationsJSON(sparkSession)
    //secOperations(sparkSession)
    //operationsDBDataFrames(sparkSession)
    othersOperations(sparkSession)
    sparkSession.stop()
  }

  case class Person(var id: Int, var name: String)

  case class Sim(var id: Int, var number: String)

  def othersOperations(sparkSession: SparkSession): Unit = {
    import sparkSession.implicits._
    val df = getDataFrame(sparkSession)
    val persons = Seq(Person(1, "jura"), Person(2, "marina"), Person(3, "maksim")).toDF()
    val sims = Seq(Sim(1, "+37065675451"), Sim(2, "+37065675450"), Sim(3, "+37060166394")).toDF()

    val res1 = persons.join(sims, Seq("id"), "leftouter")
    res1.show()

    res1.cube($"id").count().show()
    res1.cube($"name").count().show()
    res1.cube($"id", $"name").count().show()


    persons.crossJoin(sims).show()

    //sqlDF.crossJoin()
    //cogroup
    //coalesce
    //groupByKey([numTasks])
    //sqlDF.agg()


  }

  def operationsDBDataFrames(sparkSession: SparkSession): Unit = {
    import sparkSession.implicits._
    val df = getDataFrame(sparkSession)
    df.createOrReplaceTempView("logs")
    val sqlDF = sparkSession.sql("SELECT * FROM logs where login = \"maksim\"")

    sqlDF.show()

    println("====================")
    val newdf = sqlDF.as[PointRecord]
    newdf.show()
    import org.apache.spark.sql.functions._
    val func = udf((col1: String, col2: String) => {
      val combine = col1 + " " + col2
      combine
    })

    val newdf2 = newdf.withColumn("newField", func(newdf("login"), newdf("longitude")))
    newdf2.show


    val func2 = udf((col1: String, col2: String) => {
      col1 + " " + col2
    })

    val newdf3 = newdf.withColumn("newField333", func2(newdf("login"), newdf("longitude")))
    newdf3.show


  }


  //val parse_city: (Column) => Column = (x:Column) => { x }
  //val parse_city: Column => Column = (x:Column,y:Column) => { x*y }


  def operationsDB(sparkSession: SparkSession): Unit = {
    import sparkSession.implicits._
    val df = getDataFrame(sparkSession)
    df.printSchema()
    df.show()
    val logins = df.select("login")
    logins.show()
    val countsByAge = df.groupBy("login").count()
    countsByAge.show()

    val df2 = df.select($"login", $"latitude", $"latitude" + 1 as "jjjj", $"longitude")
    df2.show
    df2.filter($"login" === "texhad3op" && $"latitude" > 54.6877).show()
    df2.groupBy("login").count.show
    val df3 = df2.groupBy("login").max()
    df3.show
    val row = df2.groupBy("login").count().orderBy($"count".desc).collect().head
    println(row.getLong(1))
  }

  def operationsJSON(sparkSession: SparkSession): Unit = {
    import sparkSession.implicits._
    val json = getJsonDataFrame(sparkSession)
    json.show

    val processed = json.select("action", "timestamp")
    val processed2 = processed.na.drop(Array("action"))

    processed2.show

    val processed3 = processed2.select($"action", $"timestamp")
    processed3.show()
  }

  def getJsonDataFrame(sparkSession: SparkSession): DataFrame = {
    sparkSession.read.json("C:\\files\\1.json")
  }


  def getDataFrame(sparkSession: SparkSession): DataFrame = {
    sparkSession.read
      .format("jdbc")
      .option("driver", "org.postgresql.Driver")
      .option("url", "jdbc:postgresql://localhost:5432/jgtracker")
      .option("dbtable", getQuery)
      .option("user", "postgres")
      .option("password", "rootpassword")
      .load()
  }

  def getQuery(): String = {
    """
    (select login,latitude,longitude from customers as c
      inner join tracks as t on c.id = t.customerid
      inner join points as p on t.id = p.trackid ) emp_alias"""
  }

  def secOperations(sparkSession: SparkSession): Unit = {
    val df = sparkSession.createDataFrame((1 to 100).map(i => Record(i, s"val_$i")))
    df.createOrReplaceTempView("records")
    df.show()
    val df2 = sparkSession.sql("SELECT * FROM records").collect().foreach(println)
  }
}
