package org.turningme.theoretics

import org.apache.spark.{SparkConf, SparkContext}
import org.turningme.theoretics.api.Utils

/**
  * Created by jpliu on 2019/7/1.
  */
object RddDemo {
  def main(args: Array[String]): Unit ={
    loadLocalFile()
  }






  def test1(): Unit ={
    val words = Array("a","b","v")
    val conf = new SparkConf()
      .set("spark.driver.bindAddress","127.0.0.1")
      .setAppName("tet").setMaster("local")

    val sc = new SparkContext(conf)
    val rdd = sc.parallelize(words)
    val rdd2 = sc.parallelize(words)
    rdd.union(rdd2)

    rdd.map(x => (x,1)).reduceByKey( (vv,vxx) =>{
      vv + vxx

    }).collect().foreach(println)
  }


  /**
    * describe how to load local file / hdfs/s3 , utilizing the core spark rdd api
    */
  def loadLocalFile():Unit ={
    val conf = new SparkConf()
      .set("spark.driver.bindAddress","127.0.0.1")
      .setAppName("loadLocalFile").setMaster("local")

    val sc = new SparkContext(conf)

    //loadFileInClassPath is a helper class that helping  find absolute path from jvm classpath with a filename
    //here you can specify a local file path , or hdfs file path
    val f1 = Utils.loadFileInClassPath("testFile1")
    val f2 = Utils.loadFileInClassPath("testFile2")

    val rdd1 = sc.textFile(f1)
    val rdd2 = sc.textFile(f2)

    val count = rdd1.union(rdd2).count()
    println(s"count is $count")


    //generate rdd from a file path . local /hdfs /s3
    val rdd2fs = sc.wholeTextFiles(Utils.loadFileInClassPathParent(f1))
    val count2fs = rdd2fs.count()
    println(s"count is $count2fs")
    //we see that this rdd's element is a 2-d tuple ,first param is the file name ,second is the file content line
    rdd2fs.foreach(f => {println(f._1 + "," +f._2)})

    //with flat map , we get each lines ,from two files , remove the file named rdds
    rdd2fs.flatMap(f=>f._2.split("\n")).foreach(f=>{
      println(s"file content lines   , ${f}")
    })

    rdd2fs.map(f => f._1).foreach(f=>{
      println(s" file name is ${f}")
    })
  }


  def frameworkDemo ={
    val conf = new SparkConf()
      .set("spark.driver.bindAddress","127.0.0.1")
      .setAppName("tet").setMaster("local")

    val sc = new SparkContext(conf)

    //we generate one rdd  , representing the user profile partition
    val rdd1 = sc.parallelize( Array("a","b","v"))
    val rdd2 = sc.parallelize(Array(1,2,3))

    //simple way,first filter , then join
    //with other transformations
    // it is a cartesian and cost much
    rdd1.filter(f => !"".equals(f))
      .cartesian(rdd2).map(x=>x).count()


    //use broad cast , make that userPartition is placed a copy on each machine only once, and no need to shuffle for cartesian reason
    val userPartition =sc.broadcast(Array(1, 2, 3))
    rdd1.filter(f => !"".equals(f)).flatMap(f=>{
      userPartition.value.foreach(e =>{
        // sim join computation here ???
      })

      //final result
      ""
    }).count()

  }

}
