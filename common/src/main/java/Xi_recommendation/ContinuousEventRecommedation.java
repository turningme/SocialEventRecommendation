package Xi_recommendation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author helen ding on 26/09/2020.
 */
public class ContinuousEventRecommedation {

    //migrateT=0.001
    //coupling=0

    public ContinuousEventRecommedation(String MTimeSlotFlist, String path, String outpath, String ext, float migrateT,
                                        String UserInfluFilePath, int coupling) throws IOException {

        parameters para=new parameters();

        float ALPHA=0.4f;
        int MVALUE=10;

        ALPHA=para.ALPHA;
        MVALUE=para.MVALUE;

        int TUNUM=para.TUNUM;
        int KUNUM=para.KUNUM;

        int TFIDF_DIM=para.TFIDF_DIM;
//        int coupling=para.coupling;


        ArrayList<SubEvent> UPEventList = new ArrayList<>();
        ArrayList<UserProfile> UserProfileHashMap = new ArrayList<>();

        ArrayList<EventRecommendation.RecItem> topKUsers = new ArrayList<>();
        int rit = 0;
        EventMigration migrateEvent = new EventMigration();

        SocialEventOperation SEO = new SocialEventOperation();

        String userinfluenceFile = UserInfluFilePath + "UserInfluDictfile_Nepal07(Training15April-24April).txt";
        new EventMigration().loadUserProfileHashMap(userinfluenceFile, UserProfileHashMap);

//        String userlist = "data/UserProfileResult(Training15April-24April).txt";

        String userlist = "data/NepalUserProfile(Training15April-24April).txt";


        new EventMigration().uploadUserProfilesIntoHashMap(userlist, UserProfileHashMap); //locations...

        //========================================
        //load userlist history events

        String userEventfilelist = "data/napelDataFlist(15April-24April).txt";
        String UserHistEventpath = "data/Nepal_UserHistUP/";
        new EventMigration().BulkLoadUserHistoryEvents(UserHistEventpath,UPEventList);

/********************************************
 @author Xi Chen on 26/09/2020.
 */

        ArrayList<SubEvent> Eventclusters = new ArrayList<>();
//        ArrayList<SubEvent> Eventcandidates=new ArrayList<>();

//        ArrayList<SubEvent> UPEventList =new ArrayList<>();

        int addorDel = 0;
        int DeleteNum = Eventclusters.size();

        UpdateUPeventList(UPEventList,Eventclusters, addorDel, DeleteNum);

        EventRecommendation eventRec = new EventRecommendation(ALPHA, UPEventList.size());


//        BufferedReader dataSetDescriptor = new BufferedReader(new FileReader(new File(MTimeSlotFlist)));

        System.out.println(MTimeSlotFlist);
//        System.out.println(dataSetDescriptor.readLine());

//        for (String SlotFileName : dataSetDescriptor.readLine().split(" ")) {
//        String[] fnamelist=new File(MTimeSlotFlist).list();

        for(String SlotFileName: new File(MTimeSlotFlist).list()){
            String FullSlotFileName = MTimeSlotFlist + SlotFileName;

            Eventclusters.clear();
            loadMigrationEventDetectResult(FullSlotFileName, Eventclusters);
/********************************************
 @author Xi Chen on 02/10/2020.
 */
            System.out.println("event generation " + SlotFileName);
            int curEno = UPEventList.size();
            for (int i = 0; i < Eventclusters.size(); i++) {
                Eventclusters.get(i).SetEventNo(curEno);
                System.out.println(Eventclusters.get(i));
                curEno++;
            }

            //===presetting recommendation parameters or variables===================
			/*====Here, The CouplingInforTable information is stored in UserProfileHashMap and UPEventList
			UPEventList should include all the clusters in history and the current Eventclusters*/
            addorDel = 1;  //append
            DeleteNum = 0;  //no deletion
            UpdateUPeventList(UPEventList,Eventclusters, addorDel, DeleteNum);

            String Eventpath = outpath + SlotFileName + ".txt";

//            File resultFile=new File(Eventpath,"a+");
            BufferedWriter resultFile = new BufferedWriter(new FileWriter(new File(Eventpath)));

            int maxi = eventRec.getEventNum();

            for (SubEvent ecit : Eventclusters) {
                System.out.println("clusterNo: " + ecit.GetEventNo());
                //pre compute the similarity between (*ecit) and all the history event clusters in UPEventList
                for (int i = 0; i < maxi; i++) {
                    float curdist = SEO.l2_dist_int((ecit).EHashV.stream().mapToInt(Integer::intValue).toArray(), UPEventList.get(i).EHashV.stream().mapToInt(Integer::intValue).toArray(), MVALUE);
                    if (curdist <= 0.00001) //close to 0,
                        UPEventList.get(i).HistEventSimilarity = 0;//not conflict, not similar
                    else
                        UPEventList.get(i).HistEventSimilarity = eventRec.GetESim(ecit, UPEventList.get(i),UserProfileHashMap);
						/*if (coupling){
							//=================
							int EnNo = (*ecit).GetEventNo();
							int EuNo = UPEventList[i].GetEventNo();
							float UserEventProb = eventRec.GetProbEventClusters(EnNo, EuNo);
							UPEventList[i].HistEventSimilarity *= UserEventProb;
						}*/
                }

                // check (ecit) should be recommended to any users, and output the userid
                for (int i = 0; i < TUNUM; i++) {
//                    if (UserProfileHashMap.get(i).UserInterestEvents.isEmpty()) {
//                        continue;
//                    }
                    //check if ecit should be recommended to user i, UserProfileHashMap[i]
                    float SimEuser = 0;

                    if (coupling!=0)  // With	GetESimUser(...), the recommendation only considers the event migration similarity part == =
                        SimEuser = eventRec.GetESimUser((ecit), UserProfileHashMap.get(i));
						/*else //The following code considers both Coupled event similarity and event migration similarity in recommendation.
						{
							float CoupledSimi = eventRec.GetProbUserEvent((*ecit), (*ecit).GetEventNo(), UserProfileHashMap[i]);
							SimEuser = CoupledSimi;
						}*/

                    //ELIPSE:0.3
                    if (SimEuser < 0.1 || (topKUsers.size() == KUNUM && SimEuser < topKUsers.get(topKUsers.size() - 1).simi)) {
                        continue;
                    }
                    /*==========================================================================================*/
                    EventRecommendation.RecItem item = null;
                    item.UserID = i;  //the ith user
                    item.simi = SimEuser;
                    //insert item into topKUsers;

                    int endPos = topKUsers.size();
                    if (endPos > 0)
                        endPos -= 1;
                    int startPos = 0;
                    while (endPos - startPos > 1) {
                        int midPos = (endPos + startPos) / 2;
                        if (topKUsers.get(midPos).simi > item.simi) {
                            startPos = midPos;
                        } else if (topKUsers.get(midPos).simi < item.simi) {
                            endPos = midPos;
                        } else  //equal
                            break;
                    }
                    rit = endPos;
                    topKUsers.add(rit, item);
                    if (topKUsers.size() > KUNUM)
                        topKUsers.remove(KUNUM - 1);
                }

                resultFile.write("<clusterid> %d </clusterid>\n" + (ecit).GetEventNo());
                List<SocialMSG> msgset = ecit.GetEventMSGs();
//                vector<SocialMSG> msgset = (*ecit).GetEventMSGs();
                for (SocialMSG msgit : msgset) {
                    resultFile.write("<msgid>\t%llu\t</msgid>\n" + msgit.getMSGID());
                }

                //fprintf(resultFile, "<recitem>\t");
                for (rit = 0; rit < topKUsers.size(); rit++) {
                    resultFile.write("<recitem>\t %d %f %llu\t</recitem>\n" + topKUsers.get(rit).UserID + topKUsers.get(rit).simi + UserProfileHashMap.get(topKUsers.get(rit).UserID).userOId);//(*rit).timeslotFile);
                }
                topKUsers.clear();
            }
            resultFile.close();

            addorDel = 0;
            DeleteNum = Eventclusters.size();
            UpdateUPeventList(UPEventList,Eventclusters, addorDel, DeleteNum);
            eventRec.setcurEventNum(UPEventList.size());

            //update the user influence graph
            userinfluenceFile = UserInfluFilePath + "UserInfluDictfile_Nepal07(Training15April-24April).txt";
            migrateEvent.UpdateUserProfileHashMap(userinfluenceFile, UserProfileHashMap);
//            dataSetDescriptor.close();
            Eventclusters.clear();
        }
        for (int i = 0; i < UserProfileHashMap.size(); i++) {
            UserProfileHashMap.get(i).UserInfluenceDistri.clear();
            UserProfileHashMap.get(i).UserInterestEvents.clear();
            UserProfileHashMap.get(i).UserResponseNumbers.clear();
        }
        UserProfileHashMap.clear();
        for (int i = 0; i < UPEventList.size(); i++) {
            UPEventList.get(i).cleansubEvent();
        }
        UPEventList.clear();
    }


    /********************************************
     @author Xi Chen on 26/09/2020.
     */
    public int UpdateUPeventList(ArrayList<SubEvent> UPEventList, ArrayList<SubEvent> incomingEvents, int AddorDel, int DeleteNum) {
        int ret = 0;
        if (AddorDel!=0) {
            UPEventList.addAll(incomingEvents);
        } else {
            // erase the first DeleteNum elements:
            for (int i = 0; i < DeleteNum; i++) {
                UPEventList.remove(i);
            }
            for (int i = 0; i < UPEventList.size(); i++) {
                UPEventList.get(i).SetEventNo(UPEventList.get(i).GetEventNo() - DeleteNum);
            }

            for (int i = 0; i < EventMigrationRecom.UserProfileHashMap.size(); i++) {
                List<SubEvent> eit = EventMigrationRecom.UserProfileHashMap.get(i).GetUserInterestEvents();
                int offset = 0;

                for (int j = 0; j < eit.size(); j++) {
                    if (eit.get(j).GetEventNo() < DeleteNum) {
                        EventMigrationRecom.UserProfileHashMap.get(i).GetUserInterestEvents().remove(j);
                        j = j + offset;
                    } else {
                        eit.get(j).SetEventNo(eit.get(j).GetEventNo() - DeleteNum);
                        offset += 1;
                    }
                }
            }
        }
        ret = UPEventList.size();
        return ret;
    }


    public int loadMigrationEventDetectResult(String filename, ArrayList<SubEvent> eventClusters) throws IOException {
        int ret = 0;
        float[] vec = new float[new parameters().TFIDF_DIM];
        TFIDFEle curTFIDF;
        TimeRange newTR = new TimeRange();
        SpaceRange newSR = new SpaceRange();

        ArrayList<EUserFrePair> euids = new ArrayList<>();

        long msgid = 0;
        
//        unsigned long long msgid = 0;
        int tpno = 0;
        int isMSG = 0;
        int isHashtag = 0;
        SubEvent curEvent = new SubEvent();
        ArrayList<String> msgHashtags = new ArrayList<>();
        int eid = 0;
        long retweetstatus = 0;
//        unsigned long long retweetstatus = 0;

        if (new File(filename).exists()) {

            BufferedReader f_open = new BufferedReader(new FileReader(filename));

            String line = null;
            while ((line = f_open.readLine()) != null) {
//                String[] strtok=line.split("\t|\r|\n");
//                int length=strtok.length;
//                int i=0;

//                while(i<length){
                if (line.contains("<clusterid>")) {
                    if (!curEvent.GetEventMSGs().isEmpty()) {
                        curEvent.SetEventUserIDs();
                        curEvent.SetCluster_ConceptTFIDFVec();
                        //curEvent.SetSpaceRange();
                        curEvent.SetTimeRange();
                        eventClusters.add(curEvent);
                    }
                    isMSG = 0;
                    curEvent.EventReset();
                    curEvent.SetEventNo(Integer.parseInt(line.substring(line.indexOf("<clusterid>") + 11, line.indexOf("</clusterid>")).trim()));
                    //set the user-event interaction recorders between curEvent and user id
                }
                if (line.contains("<migclusterid>")) {
                    curEvent.SetmigEventNo(Integer.parseInt(line.substring(line.indexOf("<migclusterid>") + 14, line.indexOf("</migclusterid>")).trim()));
                }
                if (line.contains("<hashtags>")) {
                    isHashtag = 1;
                    msgHashtags.clear();
                    String[] hashtags = line.substring(line.indexOf("<hashtags>") + 10, line.indexOf("</hashtags>")).trim().split(" ");
                    for (String hashtag : hashtags) {
                        msgHashtags.add(hashtag);
                    }
                }

                if (line.contains("<msgid>")) {
                    isMSG = 1;
                    msgid = Long.parseLong(line.substring(line.indexOf("<msgid>") + 8, line.indexOf("</msgid>")).trim());
                }

                if (line.contains("<rtmsgid>")) {
                    isMSG = 1;
                    retweetstatus = Long.parseLong(line.substring(line.indexOf("<rtmsgid>") + 10, line.indexOf("</rtmsgid>")).trim());
                }

                if (line.contains("<coordinates>")) {

                    newSR.lat = Float.parseFloat(line.substring(line.indexOf("<coordinates>") + 13, line.indexOf("</coordinates>")).trim().split(" ")[0]);
                    newSR.longi = Float.parseFloat(line.substring(line.indexOf("<coordinates>") + 13, line.indexOf("</coordinates>")).trim().split(" ")[1]);
                    newSR.radius = EventMigrationRecom.SPACERADIUST;
                }

                if (line.contains("<timestamp_ms>")) {
                    newTR.TimeStampCentre = Float.parseFloat(line.substring(line.indexOf("<timestamp_ms>") + 14, line.indexOf("</timestamp_ms>")).trim());
                    newTR.range = EventMigrationRecom.TIMERADIUST;
                }

                if (line.contains("<useridlist>")) {
                    EUserFrePair eufPair = new EUserFrePair();
                    String[] useridlist = line.substring(line.indexOf("<useridlist>") + 12, line.indexOf("</useridlist>")).trim().split(" ");
                    System.out.println(line.substring(line.indexOf("<useridlist>") + 12, line.indexOf("</useridlist>")).trim());
                    eufPair.userid = Integer.parseInt(useridlist[0]);
//                    eufPair.frequency = Integer.parseInt(useridlist[1]); // Xi Chen Nov 23
                    euids.add(eufPair);
                }
            }
            //is message, then add it into the messagelist of
            if (isMSG == 1) {

//                System.out.println(newTR.range+" "+newTR.TimeStampCentre);

                SocialMSG newsocialmsg = new SocialMSG(0, vec, newTR, newSR, euids);
//                SocialMSG newsocialmsg=new SocialMSG();
//                newsocialmsg.SocialMSGInitial(0, vec, newTR, newSR, euids);
//                SocialMSG(0, vec, newTR, newSR, euids);

                newsocialmsg.setMSGID(msgid);
                if (isHashtag == 1) {
                    newsocialmsg.hashtaged = isHashtag;
                    newsocialmsg.HashtagList.addAll(0, msgHashtags);
                    isHashtag = 0;
                }
                curEvent.uploadEventMsg(newsocialmsg);
                newsocialmsg.cleanMSG();
                euids.clear();
            }
            eventClusters.add(curEvent);
            curEvent.EventReset();
            f_open.close();
        }
        ret = eventClusters.size();
        return ret;
    }
}

