Steps for executing the code in AWS: 
1) Sign up in the aws The EMR cluster and the S3 bucket are created.
2) create a jar file in the Intellij and upload this jar file into S3 bucket.
3) Create the below directory structure and place the required files as shown below:

	sg-cs6350  ->  Assignment2  -> 	part1  ->  407161809_T_ONTIME_REPORTING.csv
					part2  ->  tweets.csv
					part3  ->  catsndogs  ->  train  ->  cats  -> 
									 ->  dogs  ->
							      ->  test	 ->  cats  ->
							      		 ->  dogs  ->
					sparkwordcount_2.11-0.1.jar

4) Add a step in the created cluster in the EMR by using the following configuration:
	a)Page Rank for Airports
	- Step type is Spark Application
	- Name is Page Rank
	- Deploy mode is Cluster
	- Class file is --class "PageRank"
	- Application path is the path of the application uploaded : s3://sg-cs6350/Assignment2/sparkwordcount_2.11-0.1.jar
	- 3 arguments are needed - path of the input file, number of iterations and path of the output file : s3://sg-cs6350/Assignment2/part1/407161809_T_ONTIME_REPORTING.csv 10 s3://sg-cs6350/Assignment2/part1/output
	When this is executed the output file is created in the output folder specified by executing the specified class : s3://sg-cs6350/Assignment2/part1/output

 	b)Tweet Processing & Classification using Pipelines
	- Step type is Spark Application
	- Name is tweets 
	- Deploy mode is Cluster
	- Class file is --class "tweets"
	- Application path is the path of the application uploaded : s3://sg-cs6350/Assignment2/sparkwordcount_2.11-0.1.jar
	- The 2 arguments that are needed are path of the input file and path of the output file : s3://sg-cs6350/Assignment2/part2/tweets.csv s3://sg-cs6350/Assignment2/part2/output
	When this is executed the output file is created in the output folder specified by executing the specified class : s3://sg-cs6350/Assignment2/part2/output

	
	c) Image Processing using Spark Deep Learning Pipelines

	- The URL of Databricks notebook is : https://databricks-prod-cloudfront.cloud.databricks.com/public/4027ec902e239c93eaaa8714f173bcfc/32072069969770/2766025117377373/478845738118579/latest.html
	- NOTE: Create 6.6 ML (includes Apache Spark 2.4.5, Scala 2.11) cluster. It is pre-installed with all required libraries.
	- Image files are already loaded at s3://sg-cs6350/Assignment2/part3 directory and access keys provided in the notebook
	When this is executed it will print the output with the acheived level of accuracy on the test data.

		