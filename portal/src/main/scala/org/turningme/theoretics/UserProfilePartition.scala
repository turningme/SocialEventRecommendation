package org.turningme.theoretics

import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.{FileSplit, InputSplit, TextInputFormat}
import org.apache.spark.rdd.HadoopRDD
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by jpliu on 2020/2/15.
  * This is a demo , to show how to setup user profile , with RDD , Dataset , to utilize the distributed processors .
  * But no meaningful business logics .
  *
  */
object UserProfilePartition {
  case class UserProfileT (userIdX:String ,profileDetail:String)
  // profile files directory on DFS or local FS
  val userProfileFilePath = "/Users/jpliu/Documents/213_21all/userprofile/"
  // user Dictionary  file  on DFS or local FS
  val userDictionaryFile = "/Users/jpliu/Documents/213_21all/userdic.txt"

  def main(args: Array[String]): Unit ={
    // please define you appName , master while you confirm where you run your program, and other options
    val spark = SparkSession.builder.appName("Simple Application").master("local[4]").config("SPARK_LOCAL_IP","127.0.0.1").getOrCreate()

    val df11 = getLocalFsUserProfile(spark, userProfileFilePath)
    df11.show(11,false)


    val plainUserDict = getPainUserDict(spark,userDictionaryFile)
    println(" plain user dict  count is  " +  plainUserDict.count())
  }


  /**
    * use hadoop api , to detect content's InputSplit meta, then we could find its file information, we need its file name
    * Reference https://www.cnblogs.com/SysoCjs/p/11466103.html
    * @param sparkSession
    * @param path
    */
  def getLocalFsUserProfile(sparkSession: SparkSession ,path :String) :DataFrame ={
    val fileRDD = sparkSession.sparkContext.hadoopFile[LongWritable, Text, TextInputFormat](path)
    val hadoopFileRDD = fileRDD.asInstanceOf[HadoopRDD[LongWritable, Text]]
    val fileAndProfile = hadoopFileRDD.mapPartitionsWithInputSplit((inputSplit, iterator) => {
      val file = inputSplit.asInstanceOf[FileSplit]
      iterator.map(x => {(file.getPath.toString().split("\\.")(0) , x._2.toString)})
    }).reduceByKey((x1,x2)=>{
      x1 + "\n" + x2
    }).map( item => UserProfileT(item._1 , item._2))

    import sparkSession.implicits._
    val userProfileRs = fileAndProfile.toDF()
    userProfileRs
  }


  /**
    *  It is no special , just load user dictionary for demo. Take it easy .
    * @param sparkSession
    * @param file
    * @return
    */
  def getPainUserDict(sparkSession: SparkSession , file :String) :DataFrame ={
    val userDict = sparkSession.read.format("text")
      .load(userDictionaryFile)
    userDict.registerTempTable("tt")
    val res = sparkSession.sqlContext.sql("select split(value,'\t')[0] as originId, split(value,'\t')[1] as userProfileId from tt")
    res
  }

}
