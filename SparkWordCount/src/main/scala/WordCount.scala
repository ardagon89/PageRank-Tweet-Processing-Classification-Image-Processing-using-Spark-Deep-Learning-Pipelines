import java.io.File

import org.apache.spark.{SparkConf, SparkContext}

import scala.reflect.io.Directory

object WordCount {
  def main(args: Array[String]): Unit = {

    if (args.length != 2) {
      println("Usage: WordCount InputDir OutputDir")
    }
    // create Spark context with Spark configuration
    val sc = new SparkContext(new SparkConf()
                                  .setAppName("Spark Count"))
    //.setMaster("local[*]")  //Uncomment this to run on local machine

    // read in text file and split each document into words
    val tokenized = sc.textFile(args(0)).flatMap(_.split(" "))

    // count the occurrence of each word
    val wordCounts = tokenized.map((_, 1)).reduceByKey(_ + _)

    val sorted = wordCounts.sortBy(-_._2)

    // Delete the directory if it exists
    new Directory(new File(args(1))).deleteRecursively()

    sorted.saveAsTextFile(args(1))
  }

}
