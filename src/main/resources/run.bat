C:\spark-2.2.1-bin-hadoop2.7\bin\spark-submit.cmd --class WordCount --master local[4] C:\tests\sparktests-scala\target\scala-2.11\sparktests-scala_2.11-0.1.jar

C:\spark-2.2.1-bin-hadoop2.7\bin\spark-submit.cmd --driver-class-path C:\pg\postgresql-42.2.1.jar --jars C:\pg\postgresql-42.2.1.jar --class JDBCTest C:\tests\sparktests-scala\target\scala-2.11\sparktests-scala_2.11-0.1.jar

sbt clean package && C:\spark-2.2.1-bin-hadoop2.7\bin\spark-submit.cmd --class test.Classification --driver-memory 12g --master local[*] C:\tests\sparktests-scala\target\scala-2.11\sparktests-scala_2.11-0.1.jar

