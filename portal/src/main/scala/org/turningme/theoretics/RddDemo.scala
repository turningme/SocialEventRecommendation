package org.turningme.theoretics

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by jpliu on 2019/7/1.
  */
object RddDemo {
  def main(args: Array[String]): Unit ={
    test1()
  }






  def test1(): Unit ={
    val words = Array("a","b","v")
    val conf = new SparkConf()
      .set("spark.driver.bindAddress","127.0.0.1")
      .setAppName("tet").setMaster("local")

    val sc = new SparkContext(conf)
    val rdd = sc.parallelize(words)


    rdd.map(x => (x,1)).reduceByKey( (vv,vxx) =>{
      vv + vxx

    }).collect().foreach(println)
  }

}
