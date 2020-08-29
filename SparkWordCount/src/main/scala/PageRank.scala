import java.io.File

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

import scala.reflect.io.Directory

object PageRank {
  //main method
  def main(args: Array[String]): Unit = {

    //Check if the number of arguments are correct
    if (args.length != 3) {
      println("args: [Input_File No_of_Iterations Output_Folder]")
    }

    // Instantiate the configuration
    val config = new SparkConf()
                        .setAppName("Page Rank")
    //.setMaster("local[*]")  //Uncomment this to run on local machine

    //Create context and session objects
    val sc = new SparkContext(config)
    val ss = new SparkSession
                        .Builder()
                        .config(config)
                        .getOrCreate()

    // Read the ORIGIN, DEST data from the csv file
    val data = ss
      .read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(args(0))

    //Create tuples of ORIGIN and DEST pairs
    val connections = data
                      .rdd
                      .map(x=>(x.get(0),x.get(1)))
                      .collect()

    //Convert DataFrame into an RDD
    val connectionsPerAirport = sc
      .parallelize(connections)
      .groupByKey()

    //Calculate the number of connections per airport
    val connectionsCount = connectionsPerAirport
      .count()

    // Initializing PageRank with initial value 10
    var pageRanks = connectionsPerAirport
                    .map{ case (value, iterable) => (value,10.0)}

    //The value of alpha is initialized
    val a = 0.15

    // Update the PageRank for n iterations
    for (i <- 0 to args(1).toInt-1) {

      val airportPageRank = connectionsPerAirport
                          .join(pageRanks)
                          .values
                          .flatMap{case(c,pr)=>c.map((_,pr/c.size))}

      pageRanks = airportPageRank
                  .reduceByKey((x,y) => x + y)
                  .mapValues(a * 1 / connectionsCount + (1-a) * _)
    }

    // Sort PageRanks in decreasing fashio
    val descendingRanks = pageRanks.sortBy(-_._2)

    // Delete the directory if it exists
    new Directory(new File(args(2))).deleteRecursively()

    //Write to the destination directory
    descendingRanks.saveAsTextFile(args(2))
  }
}