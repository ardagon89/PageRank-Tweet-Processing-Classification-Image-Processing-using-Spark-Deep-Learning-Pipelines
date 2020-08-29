import org.apache.spark.SparkConf
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Row, SaveMode, SparkSession}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{HashingTF, StopWordsRemover, StringIndexer, Tokenizer}

object tweets {
  def main(args: Array[String]) {
    //Check if the number of arguments are correct
    if (args.length != 2) {
      println("args: [Input_File Output_Folder]")
    }

    // Using spark configuration, spark context is created
    val config = new SparkConf()
      .setAppName("Tweet Classification")
      //.setMaster("local[*]")  //Uncomment this to run on local machine

    //Create a new spark session
    val ss = new SparkSession
      .Builder()
      .config(config)
      .getOrCreate()

    // Creation of DataFrame for tweets by reading tweets.csv
    val tweets = ss
      .read
      .option("header", true)
      .option("delimiter", ",")
      .option("inferSchema", true)
      .csv(args(0))

    // Preprocessing by removal of columns that will not be used
    val validTweets = tweets
        .filter(col("text")
        .isNotNull)

    // Dividing the dataset into 80% train dataset and 20% test dataset
    val trainTestSplits = validTweets
      .randomSplit(Array(0.8, 0.2), 24)
    val trainData = trainTestSplits(0)
    val testData = trainTestSplits(1)

    //Stages of Pipelines:
    //1.Tokenization Stage
    val tokenizer = new Tokenizer()
      .setInputCol("text")
      .setOutputCol("words")

    //2.Removal of the StopWords using StopWordsRemover()
    val stopWordsRemover = new StopWordsRemover()
      .setInputCol(tokenizer.getOutputCol)
      .setOutputCol("filtered")

    //3.Hashing term Frequency Stage
    val hashingTF = new HashingTF()
      .setNumFeatures(1000)
      .setInputCol(stopWordsRemover.getOutputCol)
      .setOutputCol("features")

    //4.Conversion to Index Column using StringIndexer
    val stringIndexer = new StringIndexer()
      .setInputCol("airline_sentiment")
      .setOutputCol("label")

    //5.Linear Regression model
    val logisticRegression = new LogisticRegression()
      .setMaxIter(30)
      .setRegParam(0.1)
      .setElasticNetParam(0.3)

    // Creating Pipeline for all the operations
    val pipeline = new Pipeline()
      .setStages(Array(tokenizer,
        stopWordsRemover,
        hashingTF,
        stringIndexer,
        logisticRegression))

    // A list of parameters are created using ParamGridBuilder()
    val paraGridBuilder = new ParamGridBuilder()
      .addGrid(hashingTF.numFeatures, Array(10, 100, 1000))
      .addGrid(logisticRegression.regParam, Array(0.1, 0.05, 0.01))
      .addGrid(logisticRegression.elasticNetParam, Array(0.3, 0.5, 0.7))
      .build()

    // By using the paramGrid the best model is found by defining CrossValidator
    val crossVal = new CrossValidator()
      .setEstimator(pipeline)
      .setEvaluator(new MulticlassClassificationEvaluator)
      .setEstimatorParamMaps(paraGridBuilder)
      .setNumFolds(9)

    // Best set of parameters are determined by the use of the CrossValidation function
    val crossValModel = crossVal.fit(trainData)

    // Predicted results are determined using the test documents
    import ss.implicits._
    var predRes = crossValModel
      .bestModel
      .transform(testData)

    //Save predicted labels
    val predLabels = predRes
      .select("prediction", "label")
      .map { case Row(label: Double, prediction: Double) => (prediction, label)}

    // Evaluation metrics are obtained by the use of MulticlassMetrics
    val evalMetrics = new MulticlassMetrics(predLabels.rdd)

    //Create a Classification Metric Evaluation DataFrame
    val metDataFrame = Seq(
                      ("Accuracy",evalMetrics.accuracy),
                      ("Weighted Precision",evalMetrics.weightedPrecision),
                      ("Weighted recall",evalMetrics.weightedRecall),
                      ("Weighted FMeasure",evalMetrics.weightedFMeasure),
                      ("Weighted False Positive Rate",evalMetrics.weightedFalsePositiveRate),
                      ("Weighted True Positive Rate",evalMetrics.weightedTruePositiveRate)
                    )
                    .toDF("Measure", "Value")

    //Write to a csv file in the output directory
    metDataFrame.coalesce(1)
      .write
      .option("header", "true")
      .format("csv")
      .mode(SaveMode.Overwrite)
      .save(args(1))
  }
}
