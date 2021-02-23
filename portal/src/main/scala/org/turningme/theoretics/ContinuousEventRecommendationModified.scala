package org.turningme.theoretics

import java.io.{BufferedReader, BufferedWriter, FileReader, FileWriter}
import java.nio.file.Paths
import java.util
import java.util.ArrayList

import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.TextInputFormat
import org.apache.spark.sql.SparkSession
import org.turningme.theoretics.api.{UserProfileInputFormat, UserProfileInputFormatV2}

import scala.util.control.Breaks._
//import scala.util.control.
import Xi_recommendation._

/**
  * @Author turningme
  */
object ContinuousEventRecommendationModified {

  val SimiThreshold = 0;
  var ALPHA = 0.4f
  var MVALUE = 10

  val KUNUM = 100
  val TUNUM = 135782 //total number of users in training dataset

  val TFIDF_DIM = 50
  val MAXFLOAT = 0.12f
  val TIMERADIUST = 0.5f
  val coupling = 0

  val MAXEVENTNUM = 20000 //20  //The maximal number of incoming events in a time slot
  val UPEventList = new util.ArrayList[SubEvent]
  val UserProfileHashMap = new util.ArrayList[UserProfile]

  def main(args: Array[String]): Unit = {
    // please define you appName , master while you confirm where you run your program, and other options
    val spark = SparkSession.builder.appName("Simple Application").master("local[4]").config("SPARK_LOCAL_IP","127.0.0.1").getOrCreate()

    //1. load first user profile rdd
    val dataBaseDir = "/Users/jpliu/Downloads/SocialEventRecommendation-master/portal/src/main/data_test/"
    val useInfluenceFile  = dataBaseDir + "UserInfluDictfile_Nepal07(Training15April-24April)_1.txt"
    val userlist = dataBaseDir + "NepalUserProfile(Training15April-24April)_1.txt"
    val UserHistEventpath = dataBaseDir + "Nepal_UserHistUP/"

    //** load text file into rdd
//    val rddUserInfluence = spark.sparkContext.textFile(useInfluenceFile,1)
    val rddUserInfluence = spark.sparkContext.hadoopFile(useInfluenceFile, classOf[UserProfileInputFormat], classOf[LongWritable], classOf[UserProfile],1)

    val cnt = rddUserInfluence.count()
    println(s"sssss $cnt")
    //**
    rddUserInfluence.foreach((x)=>{
      println(x._2.UserInfluenceDistri.size())
    })


    //2. load second user profile , load and replace
    val rddUserInfluenceSecond = spark.sparkContext.hadoopFile(userlist, classOf[UserProfileInputFormatV2], classOf[LongWritable], classOf[UserProfile] , 1)
    val cnt1 = rddUserInfluenceSecond.count()
    println(s"sssss $cnt1")
    rddUserInfluenceSecond.foreach((x)=>{
      println(x._2.PostNum)
    })
  }
}

