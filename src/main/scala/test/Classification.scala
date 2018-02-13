package test

import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Classification {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.FATAL)
    //    val sparkSession = SparkSession.builder.appName("Simple Application").getOrCreate()
    Logger.getLogger("org").setLevel(Level.FATAL)
    val conf = new SparkConf().setAppName("wordCount")
    val sc = new SparkContext(conf)
    start(sc)
  }

  def start(sparkContext: SparkContext): Unit = {
    val rawData = sparkContext.textFile("C:\\files\\covtype.data")
    println(rawData.count())
    val data = rawData.map { line =>
      val values = line.split(',').map(_.toDouble)
      val featureVector = Vectors.dense(values.init)
      val label = values.last - 1
      LabeledPoint(label, featureVector)
    }
    val Array(trainData, cvData, testData) = data.randomSplit(Array(0.8, 0.1, 0.1))
    trainData.cache()
    cvData.cache()
    testData.cache()

    val model = DecisionTree.trainClassifier(
      trainData, 7, Map[Int, Int](), "gini", 4, 100)
    val metrics = getMetrics(model, cvData)

    println(metrics.confusionMatrix)
    println(metrics.precision)
    println(metrics.accuracy)
    (0 until 7).map(
      cat => (metrics.precision(cat), metrics.recall(cat))
    ).foreach(println)

    println("======================")
    val trainPriorProbabilities = classProbabilities(trainData)
    val cvPriorProbabilities = classProbabilities(cvData)
    val f = trainPriorProbabilities.zip(cvPriorProbabilities).map {
      // Соединяем попарно вероятности в обучающей последовательности,
      // наборе CV и суммируем
      case (trainProb, cvProb) => trainProb * cvProb
    }.sum

    println(f)
  }

  def getMetrics(model: DecisionTreeModel, data: RDD[LabeledPoint]): MulticlassMetrics = {
    val predictionsAndLabels = data.map(example =>
      (model.predict(example.features), example.label)
    )
    new MulticlassMetrics(predictionsAndLabels)
  }

  def classProbabilities(data: RDD[LabeledPoint]): Array[Double] = {
    val countsByCategory = data.map(_.label).countByValue()
    // Подсчитываем (категория,количество) в данных
    val counts = countsByCategory.toArray.sortBy(_._1).map(_._2)
    // Упорядочиваем количества по категориям и извлекаем их
    counts.map(_.toDouble / counts.sum)
  }
}
