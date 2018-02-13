package test


case class Person(var id: Int)

object SimpleTest {
  def main(args: Array[String]): Unit = {
    //Seq(1, 2, 3)
    println("It's working!!!")
    var p = Person(5)
    p.id = 45
    println(p)
  }
}
