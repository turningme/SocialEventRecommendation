package org.turningme.theoretics

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import org.turningme.theoretics.api.PreprocessingHelper
import org.turningme.theoretics.common.beans.UPEventPartition
import org.turningme.theoretics.common.event.SocialEvent

/**
  * Created by jpliu on 2020/2/25.
  */
object StaticSimilarityJoin {
  val LOG: Logger = LoggerFactory.getLogger(StaticSimilarityJoin.getClass);
  val alpha = 0.5f;
  val SimiThreshold = 0;

  def main(args: Array[String]): Unit = {
    //in local mode , the thread num is set to 5 , which is depend on your pc configuration . on cluser please refer official website or google
    val spark = SparkSession.builder().appName("demo_spark").master("local[5]").config("SPARK_LOCAL_IP", "127.0.0.1").getOrCreate()
    val preHelper: PreprocessingHelper = PreprocessingHelper.build().setup().preLoadUsrProfile()

    val rdd: RDD[UPEventPartition] = spark.sparkContext.parallelize(preHelper.getUserProfilePartition().toArray(new Array[UPEventPartition](0)), 10)



    //    //load local static messages by now
    preHelper.preLoadMessageData()

    val rddClusterEvent: RDD[SocialEvent] = spark.sparkContext.parallelize(preHelper.getStaticClusterEvent.toArray(new Array[SocialEvent](0)), 1)

    // preHelper.IncomingEventSubsetIdentification(socialEvent,f,0.0f,0.0f)

    //        rdd.foreach(f => {
    //          rddClusterEvent.foreach(socialEvent => {
    //
    //          })
    //        })


    //IncomingEventSubsetIdentification
    val simRank = rdd.cartesian(rddClusterEvent).filter((e) => {
      preHelper.IncomingEventSubsetIdentification(e._2, e._1, SimiThreshold, alpha).size() > 0
    })
      .map(ff=>{
        preHelper.EventSimilarityJoin(ff._1,ff._2,SimiThreshold)
      })

    simRank.count()

    println("dd " + rdd.getNumPartitions)
    LOG.info("ss " + rdd.getNumPartitions)
    println("ll " + simRank.count())
  }
}
