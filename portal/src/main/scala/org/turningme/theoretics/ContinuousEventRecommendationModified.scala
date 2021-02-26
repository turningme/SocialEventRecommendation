package org.turningme.theoretics

import java.util

import org.apache.hadoop.io.LongWritable
import org.apache.spark.sql.SparkSession
import org.turningme.theoretics.api.{IncomingSubEventInputFormat, RecommendResult, SubEventInputFormat, UserProfileInputFormat, UserProfileInputFormatV2}
import Xi_recommendation._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.turningme.theoretics.ContinuousEventRecommendation.{UserProfileHashMap, coupling}

import scala.util.control.Breaks.break

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
    val spark = SparkSession.builder.appName("Simple Application").master("local[4]").config("SPARK_LOCAL_IP", "127.0.0.1").getOrCreate()
    val sc = spark.sparkContext

    //1. load first user profile rdd
    val dataBaseDir = "/Users/jpliu/Downloads/SocialEventRecommendation-master/portal/src/main/data_test/"
    val useInfluenceFile = dataBaseDir + "UserInfluDictfile_Nepal07(Training15April-24April)_1.txt"
    val userlist = dataBaseDir + "NepalUserProfile(Training15April-24April)_1.txt"
    val UserHistEventpath = dataBaseDir + "Nepal_UserHistUP/"

    //** load text file into rdd
    //    val rddUserInfluence = spark.sparkContext.textFile(useInfluenceFile,1)
    val rddUserInfluence = spark.sparkContext.hadoopFile(useInfluenceFile, classOf[UserProfileInputFormat], classOf[LongWritable], classOf[UserProfile], 1)
      .map(f => (f._1.get(), f._2))


    val cnt = rddUserInfluence.count()
    println(s"sssss $cnt")
    //**
    rddUserInfluence.foreach((x) => {
      println(x._2.UserInfluenceDistri.size())
    })


    //2. load second user profile , load and replace
    val rddUserInfluenceSecond = spark.sparkContext.hadoopFile(userlist, classOf[UserProfileInputFormatV2], classOf[LongWritable], classOf[UserProfile], 1)
      .map(f => (f._1.get(), f._2))
    val cnt1 = rddUserInfluenceSecond.count()
    println(s"sssss $cnt1")
    rddUserInfluenceSecond.foreach((xx) => {
      println(xx._2.PostNum)
    })


    val rddSubEvents = spark.sparkContext.hadoopFile(UserHistEventpath, classOf[SubEventInputFormat], classOf[LongWritable], classOf[SubEvent], 1)
      .map(f => (f._1.get(), f._2))
    val cnt2 = rddSubEvents.count()
    println(s"sssss $cnt2")
    rddSubEvents.foreach((y) => {
      println(y._2.eventno)
    })


    val mergedRddUser = rddUserInfluence.leftOuterJoin(rddUserInfluenceSecond).map(f => {
      if (!f._2._2.isEmpty) {
        (f._1, f._2._2.get)
      } else {
        (f._1, f._2._1)
      }
    })

    //merge two user profile
    val cntMerged = mergedRddUser.count()
    println(s"sssss $cntMerged")


    //initial  UpdateUPeventList , nop since addorDel = 0 and DeleteNum = 0
    // this following section would be annotation
    /*   val addorDel = 0
       val DeleteNum = 0
       val Eventclusters = new Array[SubEvent](0)
       spark.sparkContext.parallelize(Eventclusters)
       updateHistoryEventsFromIncoming(rddSubEvents, rddSubEvents, addorDel, DeleteNum)
       updateUserProfileFromIncoming(mergedRddUser,rddSubEvents,addorDel,DeleteNum)
   */

    //simulate incoming events summary batch , simply read one incoming events file as a batch
    val incomingEventPath = dataBaseDir + "Nepal_UserHistUP/statuses.log.2015-04-25-01.UPdata"
    val incomingEventsRdd = loadMigrationEventDetectResultSummary(spark.sparkContext, incomingEventPath, true)


    //I do not get the point why use this code snippet , and why use curEno
    /*var curEno = UPEventList.size
    for (i <- 0 to Eventclusters.size) {
      curEno += 1
    }*/


    //updating historical events and user profile data if possible or condition satisfied
    val DeleteNum1 = 0
    val addorDel1 = 1
    val rddSubEventsUpdated = updateHistoryEventsFromIncoming(rddSubEvents, incomingEventsRdd, addorDel1, DeleteNum1)
    val mergedRddUserUpdated = updateUserProfileFromIncoming(mergedRddUser, incomingEventsRdd, addorDel1, DeleteNum1)

    //broadcast user profile
    val UserProfileHashMap = new util.HashMap[java.lang.Long, UserProfile]

    mergedRddUserUpdated.collect().foreach(e => UserProfileHashMap.put(e._1, e._2))
    println(s"---- ${UserProfileHashMap.size()}")
    val UserProfileHashMapBroadcastVar = sc.broadcast(UserProfileHashMap)


    val eventRec = new EventRecommendation(ALPHA, rddSubEvents.count().toInt)


    // I am wrong , the following is not the iterative compuation , block these codes
    /*val cartesianRdd = incomingEventsRdd.map(_._2).cartesian(rddSubEventsUpdated.map(_._2))

    val HistEventSimilarityUpdatedRdd = cartesianRdd.map(f=>{
      (f._2.eventno,(f._2,f._1))
    }).reduceByKey((e1,e2)=>{
      if (e1._1.isFlag){ // e1._1 is the accumulative, e2._2 is the new one
         val HistEventSimilarity = eventRec.GetESimV2(e2._2, e1._1, UserProfileHashMapBroadcastVar.value)
         e1._1.HistEventSimilarity = HistEventSimilarity
        (e1._1,e2._2)
      }else{// e2._1 is the accumulative,e1._2 is the new one
        e2._1.setFlag(true)
        val HistEventSimilarity = eventRec.GetESimV2(e1._2,e2._1, UserProfileHashMapBroadcastVar.value)
        e1._1.HistEventSimilarity = HistEventSimilarity
        (e2._1,e1._2)
      }
    }).map(_._1)*/


    val minIncomingEventNo = incomingEventsRdd.map(_._1).min()
    val maxIncomingEventNo = incomingEventsRdd.map(_._1).max()
    println(s"minIncomingEventNo = $minIncomingEventNo and maxIncomingEventNo = $maxIncomingEventNo")

    var rddSubEventsUpdatedValues = rddSubEventsUpdated.map(_._2)
    var recommendResultRddMerge = sc.emptyRDD.asInstanceOf[RDD[RecommendResult]]
    for (index <- minIncomingEventNo to maxIncomingEventNo) {
      val HistEventSimilarityUpdatedRddPair = incomingEventsRdd.filter(_._1 == index).map(_._2).cartesian(rddSubEventsUpdatedValues)
        .map(f => {
          val HistEventSimilarity = eventRec.GetESimV2(f._1, f._2, UserProfileHashMapBroadcastVar.value)
          f._2.HistEventSimilarity = HistEventSimilarity
          f
        })

      rddSubEventsUpdatedValues = HistEventSimilarityUpdatedRddPair.map(_._2)

      val recommendResultRdd = HistEventSimilarityUpdatedRddPair.map(f => {
        var SimEuser = 0.0f
        var rit = 0
        val topKUsers = new util.ArrayList[RecItem]

        val iter =  UserProfileHashMapBroadcastVar.value.values().iterator()
        while(iter.hasNext){
          val userProfile = iter.next()
          if (coupling != 0) SimEuser = eventRec.GetESimUserV2(f._1, userProfile, f._2)
          if (!(SimEuser < 0.1 || (topKUsers.size == KUNUM && SimEuser < topKUsers.get(topKUsers.size - 1).simi))) {
            //              continue
            val item = new RecItem()

            item.UserID = f._1.GetEventNo() //the ith user
            item.simi = SimEuser
            //insert item into topKUsers;
            var endPos = topKUsers.size
            if (endPos > 0) endPos -= 1
            var startPos = 0

            while (endPos - startPos > 0) {
              val midPos = (endPos + startPos) / 2
              var flagEnd = 0
              if (midPos == startPos) flagEnd = 1

              if (topKUsers.get(midPos).simi > item.simi) startPos = midPos
              else if (topKUsers.get(midPos).simi < item.simi)
                endPos = midPos
              if (flagEnd != 0) break
            }

            rit = 0 + endPos
            topKUsers.set(rit, item)
            if (topKUsers.size > KUNUM) topKUsers.remove(0 + KUNUM - 1)
          }


        }

       /* UserProfileHashMapBroadcastVar.value.values().stream().forEach(userProfile => {
          if (coupling != 0) SimEuser = eventRec.GetESimUserV2(f._1, userProfile, f._2)
          if (!(SimEuser < 0.1 || (topKUsers.size == KUNUM && SimEuser < topKUsers.get(topKUsers.size - 1).simi))) {
            //              continue
            val item = new RecItem()

            item.UserID = f._1.GetEventNo() //the ith user
            item.simi = SimEuser
            //insert item into topKUsers;
            var endPos = topKUsers.size
            if (endPos > 0) endPos -= 1
            var startPos = 0

            while (endPos - startPos > 0) {
              val midPos = (endPos + startPos) / 2
              var flagEnd = 0
              if (midPos == startPos) flagEnd = 1

              if (topKUsers.get(midPos).simi > item.simi) startPos = midPos
              else if (topKUsers.get(midPos).simi < item.simi)
                endPos = midPos
              if (flagEnd != 0) break
            }

            rit = 0 + endPos
            topKUsers.set(rit, item)
            if (topKUsers.size > KUNUM) topKUsers.remove(0 + KUNUM - 1)
          }


        })*/
        val rr = new RecommendResult
        rr.append("<clusterid> " + f._1.GetEventNo().toString + " </clusterid>\n")
        rit = 0
        printf(topKUsers.size().toString)
        while (rit != topKUsers.size()) {
          rr.append("<recitem>\t" + topKUsers.get(rit).UserID.toString + "\t" + topKUsers.get(rit).simi.toString + "\t" + UserProfileHashMap.get(topKUsers.get(rit).UserID).userOId.toString + "\t</recitem>\n") //(*rit).timeslotFile);
          rit += 1
        }
        rr
      })

      recommendResultRddMerge.union(recommendResultRdd)

    }

    val resCnt = recommendResultRddMerge.count()
    println(s"recommendResultRdd count =  $resCnt")
    recommendResultRddMerge.foreach(println)
  }


  /**
    * load incoming events
    *
    * @param sc
    * @param path
    * @param debug
    */
  def loadMigrationEventDetectResultSummary(sc: SparkContext, path: String, debug: Boolean = false): RDD[(Long, SubEvent)] = {
    val res = sc.hadoopFile(path, classOf[IncomingSubEventInputFormat], classOf[LongWritable], classOf[SubEvent], 1)
      .map(f => (f._1.get(), f._2))
    if (debug) {
      val cnt = res.count()
      println(s"loadMigrationEventDetectResultSummary load Item count $cnt")
    }

    res
  }


  /**
    * It is derived from method    migrateEvent.UpdateUPeventList(UPEventList, UserProfileHashMap, Eventclusters, addorDel, DeleteNum)
    * which separate the logics for history events update
    *
    * @param UPEventList
    * @param incomingEvents
    * @param AddorDel
    * @param DeleteNum
    * @return
    */
  def updateHistoryEventsFromIncoming(UPEventList: RDD[(Long, SubEvent)], incomingEvents: RDD[(Long, SubEvent)], AddorDel: Int, DeleteNum: Int): RDD[(Long, SubEvent)] = {
    if (AddorDel != 0) {
      UPEventList.union(incomingEvents)
    } else {
      if (DeleteNum != 0) {
        UPEventList.filter(_._1 < DeleteNum)
          .map(f => {
            val eventNo = f._1 - DeleteNum
            f._2.SetEventNo(eventNo.toInt)

            (eventNo, f._2)
          })
      } else {
        UPEventList
      }
    }

  }

  /**
    *
    * * It is derived from method    migrateEvent.UpdateUPeventList(UPEventList, UserProfileHashMap, Eventclusters, addorDel, DeleteNum)
    * * which separate the logics for user profile update
    *
    * @param userProfileList
    * @param incomingEvents
    * @param AddorDel
    * @param DeleteNum
    * @return
    */
  def updateUserProfileFromIncoming(userProfileList: RDD[(Long, UserProfile)], incomingEvents: RDD[(Long, SubEvent)], AddorDel: Int, DeleteNum: Int): RDD[(Long, UserProfile)] = {
    if (AddorDel == 0 && DeleteNum != 0) {
      userProfileList.map(f => {
        val eit = f._2.getUserInterestEvents
        var offset: Int = 0
        var j = 0
        while ( {
          j < eit.size
        }) {
          if (eit.get(j).GetEventNo < DeleteNum) {
            eit.remove(j)
            j = j + offset
          }
          else {
            eit.get(j).SetEventNo(eit.get(j).GetEventNo - DeleteNum)
            offset += 1
          }

          j += 1
        }

        f
      })
    } else {
      userProfileList
    }

  }

}

