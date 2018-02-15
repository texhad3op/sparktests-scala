package test

import java.sql.Timestamp

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
    //start(sc)
    //start2(sc)
    start3(sc)
  }

  def start3(sparkContext: SparkContext): Unit = {
    val rawData = sparkContext.textFile("C:\\files\\covtype.data")
    val data = rawData.map { line =>
      val values = line.split(',').map(_.toDouble)
      val wilderness = values.slice(10, 14).indexOf(1.0).toDouble
      // Какой из четырех признаков пустынной местности ("wilderness") равен 1
      val soil = values.slice(14, 54).indexOf(1.0).toDouble
      // Аналогично для следующих 40 признаков типа почвы ("soil")
      val featureVector =
        Vectors.dense(values.slice(0, 10) :+ wilderness :+ soil)
      // Добавляем полученные признаки к первым 10
      val label = values.last - 1
      LabeledPoint(label, featureVector)
    }
    val Array(trainData, cvData, testData) = data.randomSplit(Array(0.8, 0.1, 0.1))
    trainData.cache()
    cvData.cache()
    testData.cache()


    println(new Timestamp(System.currentTimeMillis()))
    val evaluations =
      for (impurity <- Array("gini", "entropy");
           depth <- Array(10, 20, 30);
           bins <- Array(40, 300))
        yield {
          val model = DecisionTree.trainClassifier(
            trainData, 7, Map(10 -> 4, 11 -> 40),
            impurity, depth, bins)
          val trainAccuracy = getMetrics(model, trainData).accuracy
          val cvAccuracy = getMetrics(model, cvData).accuracy
          ((impurity, depth, bins), (trainAccuracy, cvAccuracy))
        }
    evaluations.foreach(println)
    println(new Timestamp(System.currentTimeMillis()))
  }

  def start2(sparkContext: SparkContext): Unit = {
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


    val model = DecisionTreeModel.load(sparkContext, "C:\\files\\model1")

    val prd = model.predict(Vectors.dense(2785, 28, 9, 124, -5, 1908, 217, 220, 138, 1654, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
    println(prd + "!!!!!!!!!!!!!")

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

    val prd = model.predict(Vectors.dense(2785, 28, 9, 124, -5, 1908, 217, 220, 138, 1654, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
    println(prd + "!!!!!!!!!!!!!")

    println(metrics.confusionMatrix)
    println("accuracy1:" + metrics.accuracy)

    val model2 = DecisionTree.trainClassifier(
      trainData, 7, Map[Int, Int](), "entropy", 20, 100)
    model2.save(sparkContext, "C:\\files\\model1")

    val metrics2 = getMetrics(model2, cvData)
    println("accuracy2:" + metrics2.accuracy)


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
