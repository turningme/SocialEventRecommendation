package org.turningme.theoretics

import java.io.{BufferedReader, BufferedWriter, FileReader, FileWriter}
import java.nio.file.Paths
import java.util
import java.util.ArrayList
import scala.util.control.Breaks._
//import scala.util.control.
import Xi_recommendation._

import Xi_recommendation.EventMigrationRecom.UserProfileHashMap


/**
  * @Author Sissi
  */
object ContinuousEventRecommendation {

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

    //    val UPEventList = new util.ArrayList[SubEvent]
    //    val UserProfileHashMap = new util.ArrayList[UserProfile]

    val topKUsers = new util.ArrayList[RecItem]
    var rit = 0
    val migrateEvent = new EventMigration
    val eventmigraterec = new EventMigrationRecom

    val SEO = new SocialEventOperation

    val DataPath = Paths.get("").toAbsolutePath + "/portal/src/main/data_test/";

    var userinfluenceFile: String = DataPath + "UserInfluDictfile_Nepal07(Training15April-24April)_1.txt"
    migrateEvent.loadUserProfileHashMap(userinfluenceFile, UserProfileHashMap)

    var resultPath = Paths.get("").toAbsolutePath + "/portal/src/main/res/"
    val userlist = DataPath + "NepalUserProfile(Training15April-24April)_1.txt"
    migrateEvent.uploadUserProfilesIntoHashMap(userlist, UserProfileHashMap)

    val UserHistEventpath = DataPath + "Nepal_UserHistUP/"
    migrateEvent.BulkLoadUserHistoryEvents(UserHistEventpath, UPEventList)
    val Eventclusters = new ArrayList[SubEvent]


    var addorDel = 0
    var DeleteNum: Int = Eventclusters.size
    migrateEvent.UpdateUPeventList(UPEventList, UserProfileHashMap, Eventclusters, addorDel, DeleteNum)
    printf("+++++")
    printf(Eventclusters.size().toString())

    val eventRec = new EventRecommendation(ALPHA, UPEventList.size)

    var DocCount = 0
    //    val maxDimVal = 0

    val path = DataPath + "Nepal_UserHistUP/"

//    while (true) {
      DocCount = DocCount + 1
      if (!(DocCount % 2 > 0 || DocCount <= 24)) {
      }

      val fnamelist: Array[String] = FileUtil.listFiles(path)

      for (filename <- fnamelist) {
        printf("hhhh")
        printf(filename)
        if (!filename.contains("DS")) {
          //          printf(filename.indexOf("DS").toString)

          //no valid using , plz have a look at this reader
          val br = new BufferedReader(new FileReader(path + filename)) //creates a buffering character input stream
          Eventclusters.clear()
          migrateEvent.loadMigrationEventDetectResultSummary(path + filename, Eventclusters)

          printf("event generation %s\n", filename)

          printf("Eventcluster size %d\n", Eventclusters.size())

          var curEno = UPEventList.size
          for (i <- 0 to Eventclusters.size) {
            //            System.out.println("///////")
            //            System.out.println(Eventclusters.get(i))
            curEno += 1
          }
          DeleteNum = 0 //no deletion
          addorDel = 1

          migrateEvent.UpdateUPeventList(UPEventList, UserProfileHashMap, Eventclusters, addorDel, DeleteNum)

          var Eventpath = resultPath + filename
          val resultFile: BufferedWriter = new BufferedWriter(new FileWriter(Eventpath))
          val maxi = eventRec.getEventNum

          printf(UserProfileHashMap.size().toString)

          for (i <- 0 to Eventclusters.size()-1) {

            var ecit = Eventclusters.get(i)

            System.out.println("clusterNo: " + ecit.GetEventNo)
            //pre compute the similarity between (*ecit) and all the history event clusters in UPEventList

            for (j <- 0 to maxi) {
              UPEventList.get(j).HistEventSimilarity = eventRec.GetESim(ecit, UPEventList.get(j), UserProfileHashMap)
              if (UPEventList.get(j).HistEventSimilarity != 0) {
                System.out.println(UPEventList.get(j).HistEventSimilarity)
              }
            }


            for (j <- 0 to TUNUM-1) {
              //              printf("---\n")
              if (UserProfileHashMap.contains(j)) {

                if (!UserProfileHashMap.get(j).UserInterestEvents.isEmpty) {

                  var SimEuser = 0.0f
                  if (coupling != 0) SimEuser = eventRec.GetESimUser(ecit, UserProfileHashMap.get(j))


                  if (!(SimEuser < 0.1 || (topKUsers.size == KUNUM && SimEuser < topKUsers.get(topKUsers.size - 1).simi))) {
                    //              continue
                    val item = new RecItem()

                    item.UserID = i //the ith user
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
              }
            }

            resultFile.write("<clusterid> "+ecit.GetEventNo().toString+" </clusterid>\n")
//            printf(ecit.GetEventNo().toString)
            rit = 0
            printf(topKUsers.size().toString)
            while (rit != topKUsers.size()) {
              resultFile.write("<recitem>\t"+topKUsers.get(rit).UserID.toString +"\t"+ topKUsers.get(rit).simi.toString +"\t"+  UserProfileHashMap.get(topKUsers.get(rit).UserID).userOId.toString +"\t</recitem>\n") //(*rit).timeslotFile);
              rit += 1
            }
            topKUsers.clear()
            if (i + 1 < Eventclusters.size()) {
              ecit = Eventclusters.get(i + 1)
            }
            //            ecit + 1
          }
          resultFile.close()

          eventRec.setcurEventNum(UPEventList.size)
          //update the user influence graph
//          userinfluenceFile = DataPath + "UserInfluenceFile/" + "UserInfluDictfile_Nepal07(" + filename + ").txt"

          printf(userinfluenceFile)
          migrateEvent.UpdateUserProfileHashMap(userinfluenceFile, UserProfileHashMap)

          Eventclusters.clear()
          br.close()
        }
      }

    }
//  }
}

