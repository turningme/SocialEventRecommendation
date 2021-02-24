package org.turningme.theoretics

import java.io.{BufferedReader, BufferedWriter, FileReader, FileWriter}
import java.nio.file.Paths
import java.util
import java.util.ArrayList

import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.TextInputFormat
import org.apache.spark.sql.SparkSession
import org.turningme.theoretics.api.{SubEventInputFormat, SubEventRecordReader, UserProfileInputFormat, UserProfileInputFormatV2}

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
  .map(f =>(f._1.get(),f._2))


    val cnt = rddUserInfluence.count()
    println(s"sssss $cnt")
    //**
    rddUserInfluence.foreach((x)=>{
      println(x._2.UserInfluenceDistri.size())
    })


    //2. load second user profile , load and replace
    val rddUserInfluenceSecond = spark.sparkContext.hadoopFile(userlist, classOf[UserProfileInputFormatV2], classOf[LongWritable], classOf[UserProfile] , 1)
      .map(f=>(f._1.get(),f._2))
    val cnt1 = rddUserInfluenceSecond.count()
    println(s"sssss $cnt1")
    rddUserInfluenceSecond.foreach((xx)=>{
      println(xx._2.PostNum)
    })


    val rddSubEvents = spark.sparkContext.hadoopFile(UserHistEventpath, classOf[SubEventInputFormat], classOf[LongWritable], classOf[SubEvent] , 1)
    val cnt2 = rddSubEvents.count()
    println(s"sssss $cnt2")
    rddSubEvents.foreach((y)=>{
      println(y._2.eventno)
    })


    val mergedRddUser = rddUserInfluence.leftOuterJoin(rddUserInfluenceSecond).map(f => {
      if(!f._2._2.isEmpty){
        f._2._2
      }else{
        f._2._1
      }
    })

    //merge two user profile
    val cntMerged = mergedRddUser.count()
    println(s"sssss $cntMerged")




  }
}

