package trials

import org.apache.spark.mllib.linalg.Vectors

import scala.runtime.ScalaRunTime._

object Test1 {
  def main(args: Array[String]): Unit = {
    //val evaluations =
    for (impurity <- Array("gini", "entropy");
         depth <- Array(10, 20, 30);
         bins <- Array(40, 300))
      println(s"$impurity   $depth    $bins")


    val aa = Map(10 -> 4, 11 -> 40)
    println(aa)


    println("===============================")
    val line = "2596,51,3,258,0,510,221,232,148,6279,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,5"
    val values = line.split(',').map(_.toDouble)
    println(stringOf(values))
    val wilderness = values.slice(10, 14).indexOf(1.0).toDouble
    println(stringOf(wilderness))
    val soil = values.slice(14, 54).indexOf(1.0).toDouble
    println(stringOf(soil))

    val featureVector =
      Vectors.dense(values.slice(0, 10) :+ wilderness :+ soil)
    println(featureVector)
    val ll = Array(5,4,3,2,1)
    println(ll.indexOf(2))

    val list = List[Product](("dfsgd", 234), ("345345", 345, 456456))
    println(list)
    list.map { tuple =>
      tuple.productIterator.mkString("\t")
    }
  }
}
