package com.edu.neu.csye7200.finalproject

import com.edu.neu.csye7200.finalproject.util.{ALSUtil, DataUtil}

/**
  * Created by IntelliJ IDEA.
  * User: dsnfz
  * Date: 2019-04-02
  * Time: 15:13
  */
object Main extends App {
  override def main(args: Array[String]): Unit = {
      if (args.length != 1){
          println("Usage: data dir")
          sys.exit(1)
      }
      if(args.length > 0){
          //load the data and get the rating data of specific user
          val dir = args.head
          //RDD[long, Rating]
          val ratings = DataUtil.getAllRating(dir + "ratings_small.csv")

          val moviesArray = DataUtil.getMovies(dir + "movies_metadata.csv")

          val links = DataUtil.getLinkData(dir + "links_small.csv")
          val movies = DataUtil.getCandidatesAndLink(moviesArray, links)

          val userRatingRDD = DataUtil.getRatingByUser(dir + "ratings_small.csv", 1)
          val userRatingMovie = userRatingRDD.map(x => x.product).collect

          val numRatings = ratings.count()
          val numUser = ratings.map(_._2.user).distinct().count()
          val numMovie = ratings.map(_._2.product).distinct().count()

          println("rating: " + numRatings + " movies: " + numMovie
            + " user: " + numUser)

          // Split data into train(60%), validation(20%) and test(20%)
          val numPartitions = 10

          val trainSet = ratings.filter(x => x._1 < 6).map(_._2).
            union(userRatingRDD).repartition(numPartitions).persist()
          val validationSet = ratings.filter(x => x._1 >= 6 && x._1 < 8)
            .map(_._2).persist()
          val testSet = ratings.filter(x => x._1 >= 8).map(_._2).persist()

          val numTrain = trainSet.count()
          val numValidation = validationSet.count()
          val numTest = testSet.count()

          println("Training data: " + numTrain + " Validation data: " + numValidation
            + " Test data: " + numTest)

          //      Train model and optimize model with validation set
          ALSUtil.trainAndRecommendation(trainSet, validationSet, testSet, movies, userRatingRDD)
      }

      DataUtil.spark.stop()
  }
}
