package Xi_recommendation;

import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.util.*;

//import static Xi_recommendation.EventMigrationRecom.UserProfileHashMap;
import static org.turningme.theoretics.common.Constants.TFIDF_DIM;
//import static Xi_recommendation.ContinuousEventRecommedation.UserProfileHashMap;


/**
 * @author helen ding on 26/09/2020.
 */
public class EventMigration {

    public int loadUserProfileHashMap(String fileName, ArrayList<UserProfile> UserProfileHashMap) throws IOException {
        UserProfileHashMap.clear();
        BufferedReader br = new BufferedReader(new FileReader(fileName));  //creates a buffering character input stream
        String line = br.readLine();
        while (line != null) {
            if (line.contains("<USERID>")) {
                int uid = Integer.parseInt(line.substring(9, line.indexOf("</USERID>")).trim());
                line = br.readLine();
                UserProfile up = new UserProfile();
                String[] userdistri = line.substring(22, line.indexOf("</USERINFLUENCEDISTRI>")).trim().split("\t");
                for (int i = 0; i < userdistri.length; i++) {
                    int userid = Integer.parseInt(userdistri[i].split(" ")[0]);
                    float value = Float.parseFloat(userdistri[i].split(" ")[1]);
                    UPInfluDistriEle upide = new UPInfluDistriEle();
                    upide.userid = userid;
                    upide.userInflu = value;
                    up.userId = userid;
                    up.UserInfluenceDistri.add(upide);
                }
                UserProfileHashMap.add(uid, up);
            }
            line = br.readLine();
        }
        return UserProfileHashMap.size();
    }

    public int uploadUserProfilesIntoHashMap(String fileName, ArrayList<UserProfile> UserProfileHashMap) throws IOException {
        String userProfilename = fileName;
        try {
            BufferedReader br = new BufferedReader(new FileReader(userProfilename));  //creates a buffering character input stream
            String line = br.readLine();
            while (line != null) {
                UserProfile userProfile = new UserProfile();
                if (line.contains("<userid>")) {
                    userProfile.userId = Integer.valueOf(line.substring(line.indexOf("<userid>") + 9, line.indexOf("</userid>")).trim());
                    userProfile.userOId = line.substring(line.indexOf("<userOid>") + 10, line.indexOf("</userOid>")).trim();
                }
                line = br.readLine();

                if (line.contains("<location>")) {
                    userProfile.UserPhysicalLocation = new SpaceRange();
                    userProfile.UserPhysicalLocation.lat = Float.valueOf(line.substring(line.indexOf("<location>") + 11, line.indexOf("</location>")).trim().split(" ")[0]);
                    userProfile.UserPhysicalLocation.longi = Float.valueOf(line.substring(line.indexOf("<location>") + 11, line.indexOf("</location>")).trim().split(" ")[1]);

                }
                line = br.readLine();

                if (line.contains("<MSGNUM>")) {
                    userProfile.PostNum = Integer.valueOf(line.substring(line.indexOf("<MSGNUM>") + 8, line.indexOf("</MSGNUM>")).trim());

                }
                line = br.readLine();
                if (line.contains("<RESPONSENUMBERS>")) {

                    String[] responsenumber = line.substring(line.indexOf("<RESPONSENUMBERS>") + 17, line.indexOf("</RESPONSENUMBERS>")).trim().split("\t");
                    for (int i = 0; i < responsenumber.length; i++) {
                        int userid = Integer.parseInt(responsenumber[i].split(" ")[0]);
                        int value = Integer.parseInt(responsenumber[i].split(" ")[1]);

                        UPRespnseEle UPRele = new UPRespnseEle();
                        UPInfluDistriEle UPIde = new UPInfluDistriEle();
                        UPRele.userid = Integer.valueOf(userid);
                        UPRele.userResponse = Integer.valueOf(value);
                        userProfile.UserResponseNumbers.add(UPRele);

                        if (userProfile.userId == UPIde.userid)
                            UPIde.userInflu = 1;
                        else
                            UPIde.userInflu = 0;//initialization to 0.
                        userProfile.UserInfluenceDistri.add(UPIde);

                    }
                }
                UserProfile deepCopy = (UserProfile) SerializationUtils.clone(userProfile);
                UserProfileHashMap.set(userProfile.userId, deepCopy);

                line = br.readLine();

            }
            br.close();    //closes the stream and release the resources
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int BulkLoadUserHistoryEvents(String path, ArrayList<SubEvent> UPEventList) throws IOException {
        int StartClusterNo = 0;// 7672;
        String[] fnamelist = FileUtil.listFiles(path);

        for (int i = 0; i < fnamelist.length; i++) {
            String filename = fnamelist[i];
            System.out.println(filename);
            if (filename.contains("DS")) {
                continue;
            } else {
                String FullSlotFileName = path + filename;
                LoadUserProfileEvents(FullSlotFileName, StartClusterNo, UPEventList);
            }
        }
        return 0;
    }

    public int LoadUserProfileEvents(String fileName, int StartClusterNo, ArrayList<SubEvent> UPEventList) throws IOException {
        SubEvent curEvent = new SubEvent();
        TimeRange newTR = new TimeRange();
//        new TimeRange();
        SpaceRange newSR = new SpaceRange();
        List<EUserFrePair> euids = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));  //creates a buffering character input stream
            String line = br.readLine();
            while (line != null) {
                if (line.contains("<clusterid>")) {
                    String value = line.substring(line.indexOf("<clusterid>") + 12, line.indexOf("</clusterid>")).trim();
                    curEvent.SetEventNo(Integer.valueOf(value) - StartClusterNo);
                }

                line = br.readLine();
                if (line.contains("<msgcontfidf>")) {
                    String[] value = line.substring(line.indexOf("<msgcontfidf>") + 14, line.indexOf("</msgcontfidf>")).trim().split("\t");
                    for (int i = 0; i < value.length; i++) {
                        curEvent.addConceptTFIDFVec(Float.parseFloat(value[i]));
                    }
                }
                line = br.readLine();

//                if (line.contains("<msgcontfidf>")) {
////                    String value=line.substring(line.indexOf("<msgcontfidf>")+14,line.indexOf("</msgcontfidf>"));
//                    String s = "";
//                    s = s + line.substring(line.indexOf("<msgcontfidf>") + 14, (line.length() - 1));
//                    while (true) {
//                        line = br.readLine();
//                        if (line.indexOf("</msgcontfidf>") != -1) {
//                            break;
//                        } else {
//                            s = s + "\t" + line;
////                            System.out.println(s);
//                        }
//                    }
//
////                    System.out.println(s);
//                    s = s + "\t" + line.substring(0, line.indexOf("</msgcontfidf>") - 1);
//
//                    String[] tfidf = s.split("\t");
//                    for (int i = 0; i < tfidf.length; i++) {
////                        System.out.println(tfidf[i]);
//                        if (tfidf[i].split(" ").length > 1) {
////                            System.out.println(tfidf[i]);
//                            curEvent.addConceptTFIDFVec(Float.parseFloat(tfidf[i].split(" ")[1]));
//                        }
//                    }
//                }
//                if (line.contains("<HashkeyValues>")){
//                    String value=line.substring(line.indexOf("<HashkeyValues>")+16,line.indexOf("</HashkeyValues>"));
//                    curEvent.EHashV.add(Integer.valueOf(value));
//                    System.out.println(value);
//                }

                if (line.contains("<SpaceRange>")) {
                    String[] value = line.substring(line.indexOf("<SpaceRange>") + 13, line.indexOf("</SpaceRange>")).trim().split("\t");
                    newSR.lat = Float.valueOf(value[0]);
                    newSR.longi = Float.valueOf(value[1]);
                    newSR.radius = Float.valueOf(value[2]);
                    curEvent.Cluster_SR = newSR;
                }
                line = br.readLine();

                if (line.contains("<TimeRange>")) {
                    String[] value = line.substring(line.indexOf("<TimeRange>") + 12, line.indexOf("</TimeRange>")).trim().split("\t");

                    newTR.TimeStampCentre = Float.valueOf(value[0]);
                    newTR.range = Float.valueOf(value[1]);
                    curEvent.Cluster_TR = newTR;
                }
                line = br.readLine();


                if (line.contains("<SpaceRangeSet>")) {
                    String[] SpaceRangeSet = line.substring(line.indexOf("<SpaceRangeSet>") + 16, line.indexOf("</SpaceRangeSet>")).trim().split(" ");
                    SpaceRange srtmp = new SpaceRange();
                    srtmp.lat = Float.parseFloat(SpaceRangeSet[0]);
                    srtmp.longi = Float.parseFloat(SpaceRangeSet[1]);
//                    srtmp.radius = Float.parseFloat(SpaceRangeSet[2]);
                    curEvent.uploadmsgSRset(srtmp);
                }
                line = br.readLine();

                if (line.contains("<TimeRangeSet>")) {
                    String[] TimeRangeSet = line.substring(line.indexOf("<TimeRangeSet>") + 14, line.indexOf("</TimeRangeSet>")).trim().split(" ");
//                    System.out.println(TimeRangeSet[0]);
                    TimeRange trtmp = new TimeRange();
                    trtmp.TimeStampCentre = Float.parseFloat(TimeRangeSet[0]);
                    trtmp.range = Float.parseFloat(TimeRangeSet[1].split("\t")[0]);
                    curEvent.uploadmsgTRset(trtmp);
                }
                line = br.readLine();

                if (line.contains("<useridlist>")) {
                    String[] value = line.substring(line.indexOf("<useridlist>") + 13, line.indexOf("</useridlist>")).trim().split("\t");
                    for (int i = 0; i < value.length; i++) {
                        EUserFrePair eufPair = new EUserFrePair();
                        eufPair.userid = Integer.valueOf(value[i].split(" ")[0]);
                        eufPair.frequency = Integer.valueOf(value[i].split(" ")[1]);
                        euids.add(eufPair);
                    }
                    Collections.sort(euids, new Comparator<EUserFrePair>() {
                        @Override
                        public int compare(EUserFrePair o1, EUserFrePair o2) {
                            return o1.userid - o2.userid;
                        }
                    });
                    curEvent.setEventUserIDs(euids);
                    euids.clear();
                }


                if (UPEventList.size() <= curEvent.GetEventNo()) {
                    UPEventList.add(curEvent);
                }
//                    continue;
                line = br.readLine();
            }
            br.close();    //closes the stream and release the resources
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UPEventList.size();
    }

    public int UpdateUserProfileHashMap(String UIfname, ArrayList<UserProfile> UserProfileHashMap) throws IOException //input the user influence filename, and load the user influence information into UserProfileHashMap
    {
        int ret = 0;

        BufferedReader fileDescriptor = new BufferedReader(new FileReader(new File(UIfname)));
        UserProfile up;

        String line;
        int uid = 0;

        line = fileDescriptor.readLine();
        while (line != null) {
//            String[] strtok = line.split("\t|\r|\n");
            if ((line.length()) <= 0) {
                System.out.println("Invalid (empty) line!\n");
                return 1;
            }

            if (line.contains("<USERID>")) {
                uid = Integer.parseInt(line.substring(line.indexOf("<USERID>") + 8, line.indexOf("</USERID>")).trim());
                UserProfileHashMap.get(uid).userId = uid;
            }
            line = fileDescriptor.readLine();
            if (line.contains("<USERINFLUENCEDISTRI>")) {
                String[] temp = line.substring(line.indexOf("<USERINFLUENCEDISTRI>") + 21, line.indexOf("</USERINFLUENCEDISTRI>")).trim().split("\t");

                UserProfileHashMap.get(uid).UserInfluenceDistri.clear();
                for(int i=0; i<temp.length;i++){
                    UPInfluDistriEle upide = new UPInfluDistriEle();
                    upide.userid = Integer.parseInt(temp[i].split(" ")[0]);
                    upide.userInflu = Float.parseFloat(temp[i].split(" ")[1]);
                    UserProfileHashMap.get(uid).UserInfluenceDistri.add(upide);
                }
            }
            line = fileDescriptor.readLine();
        }
        fileDescriptor.close();
        return ret;
    }

//    public int UpdateUPeventList(ArrayList<SubEvent> incomingEvents, int AddorDel, int DeleteNum) {
//        int ret = 0;
//        if (AddorDel != 0) {
//            EventMigrationRecom.UPEventList.addAll(incomingEvents);
//        } else {
//            // erase the first DeleteNum elements:
//            for (int i = 0; i < DeleteNum; i++) {
//                EventMigrationRecom.UPEventList.remove(i);
//            }
//            for (int i = 0; i < EventMigrationRecom.UPEventList.size(); i++) {
//                EventMigrationRecom.UPEventList.get(i).SetEventNo(EventMigrationRecom.UPEventList.get(i).GetEventNo() - DeleteNum);
//            }
//
//            for (int i = 0; i < EventMigrationRecom.UserProfileHashMap.size(); i++) {
//                List<SubEvent> eit = EventMigrationRecom.UserProfileHashMap.get(i).GetUserInterestEvents();
//                int offset = 0;
//
//                for (int j = 0; j < eit.size(); j++) {
//                    if (eit.get(j).GetEventNo() < DeleteNum) {
//                        EventMigrationRecom.UserProfileHashMap.get(i).GetUserInterestEvents().remove(j);
//                        j = j + offset;
//                    } else {
//                        eit.get(j).SetEventNo(eit.get(j).GetEventNo() - DeleteNum);
//                        offset += 1;
//                    }
//                }
//            }
//        }
//        ret = EventMigrationRecom.UPEventList.size();
//        return ret;
//    }

    public int UpdateUPeventList(ArrayList<SubEvent> UPEventList, ArrayList<UserProfile> UserProfileHashMap, ArrayList<SubEvent> incomingEvents, int AddorDel, int DeleteNum) {
        int ret = 0;
        if (AddorDel != 0) {
            UPEventList.addAll(incomingEvents);
        } else {
            // erase the first DeleteNum elements:
            for (int i = 0; i < DeleteNum; i++) {
                UPEventList.remove(i);
            }
            for (int i = 0; i < UPEventList.size(); i++) {
                UPEventList.get(i).SetEventNo(UPEventList.get(i).GetEventNo() - DeleteNum);
            }

            for (int i = 0; i < UserProfileHashMap.size(); i++) {
                List<SubEvent> eit = UserProfileHashMap.get(i).GetUserInterestEvents();
                int offset = 0;

                for (int j = 0; j < eit.size(); j++) {
                    if (eit.get(j).GetEventNo() < DeleteNum) {
                        UserProfileHashMap.get(i).GetUserInterestEvents().remove(j);
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

    public int loadMigrationEventDetectResultSummary(String filename, ArrayList<SubEvent> eventClusters) throws IOException {
        int ret = 0;

        System.out.println("loading summary");

//        float[] vec = new float[TFIDF_DIM];
//
//        TimeRange newTR = new TimeRange();
//        SpaceRange newSR = new SpaceRange();
//        ArrayList<EUserFrePair> euids = new ArrayList<>();
//        ArrayList<EUserFrePair> euit=new ArrayList<>();

        String line;
//        SubEvent curEvent = new SubEvent();
        int eid = 0;

        if (new File(filename).exists()) {
            BufferedReader f_open = new BufferedReader(new FileReader(filename));
            line = f_open.readLine();
            while (line != null) {

                if (line.length() <= 0) {
                    System.out.println("Invalid (empty) line!\n");
                    return 1;
                }
//                System.out.println(line);
                SubEvent curEvent = new SubEvent();
                float[] vec = new float[TFIDF_DIM];

                TimeRange newTR = new TimeRange();
                SpaceRange newSR = new SpaceRange();
                ArrayList<EUserFrePair> euids = new ArrayList<>();

                if (line.contains("<clusterid>")) {
                    curEvent.SetEventNo(Integer.parseInt(line.substring(line.indexOf("<clusterid>") + 11, line.indexOf("</clusterid>")).trim()));
                }

                line = f_open.readLine();

                if (line.contains("<msgcontfidf>")) {
//                    System.out.println(line.substring(line.indexOf("<msgcontfidf>") + 13, line.indexOf("</msgcontfidf>")).trim());
                    String[] tfidfstr = line.substring(line.indexOf("<msgcontfidf>") + 13, line.indexOf("</msgcontfidf>")).trim().split("\t");
                    for (int i = 0; i < tfidfstr.length; i++) {
                        vec[i] = Float.parseFloat(tfidfstr[i]);
                    }
                    curEvent.setConceptTFIDFVec(vec);
                }

                line = f_open.readLine();

//                if(line.contains("<HashkeyValues>")) {
//                    String[] Hashvalues = line.substring(line.indexOf("<HashkeyValues>") + 15, line.indexOf("</HashkeyValues>")).trim().split(" ");
//                    for (int i = 0; i < Hashvalues.length; i++) {
//                        for (int j = 0; j < MVALUE; j++) {
//                            curEvent.EHashV.set(j, Integer.parseInt(Hashvalues[i]));
//                        }
//                    }
//                }

                if (line.contains("<SpaceRange>")) {
                    String[] SpaceRange = line.substring(line.indexOf("<SpaceRange>") + 12, line.indexOf("</SpaceRange>")).trim().split("\t");
                    newSR.lat = Float.parseFloat(SpaceRange[0]);
                    newSR.longi = Float.parseFloat(SpaceRange[1]);
                    newSR.radius = Float.parseFloat(SpaceRange[2]);
                    curEvent.setSpaceRange(newSR);
                }
                line = f_open.readLine();

                if (line.contains("<TimeRange>")) {
                    String[] TimeRange = line.substring(line.indexOf("<TimeRange>") + 11, line.indexOf("</TimeRange>")).trim().split("\t");
                    newTR.TimeStampCentre = Float.parseFloat(TimeRange[0]);
                    newTR.range = Float.parseFloat(TimeRange[1]);
                    curEvent.setTimeRange(newTR);
                }

                line = f_open.readLine();

                if (line.contains("<SpaceRangeSet>")) {
                    String[] SpaceRangeSet = line.substring(line.indexOf("<SpaceRangeSet>") + 15, line.indexOf("</SpaceRangeSet>")).trim().split(" ");
                    SpaceRange srtmp = new SpaceRange();
                    srtmp.lat = Float.parseFloat(SpaceRangeSet[0]);
                    srtmp.longi = Float.parseFloat(SpaceRangeSet[1]);
//                    srtmp.radius = Float.parseFloat(SpaceRangeSet[2]);
                    curEvent.uploadmsgSRset(srtmp);
                }

                line = f_open.readLine();

                if (line.contains("<TimeRangeSet>")) {
//                    String temp=line.substring(line.indexOf("<TimeRangeSet>") + 14, line.indexOf("</TimeRangeSet>")).trim();
//                    System.out.println(temp);

                    String[] TimeRangeSet = line.substring(line.indexOf("<TimeRangeSet>") + 14, line.indexOf("</TimeRangeSet>")).trim().split(" ");
//                    System.out.println(TimeRangeSet[0]);
                    TimeRange trtmp = new TimeRange();
                    trtmp.TimeStampCentre = Float.parseFloat(TimeRangeSet[0]);
                    trtmp.range = Float.parseFloat(TimeRangeSet[1].split("\t")[0]);
                    curEvent.uploadmsgTRset(trtmp);
                }

                line = f_open.readLine();

                if (line.contains("<useridlist>")) {
                    String[] useridlist = line.substring(line.indexOf("<useridlist>") + 12, line.indexOf("</useridlist>")).trim().split("\t");
                    for (int i = 0; i < useridlist.length; i++) {

                        EUserFrePair eufPair = new EUserFrePair();
                        eufPair.userid = Integer.parseInt(useridlist[i].split(" ")[0]);
                        eufPair.frequency = Integer.parseInt(useridlist[i].split(" ")[1]);

                        int euit = 0;
                        while (euit < euids.size()) {
                            if (euids.get(euit).userid > eufPair.userid) {
                                euids.set(euit, eufPair);
                                euit = 0;
                                break;
                            }
                            euit++;
                        }
                        if (euit == euids.size() - 1)
                            euids.add(eufPair);
                    }
                }
                curEvent.setEventUserIDs(euids);
//                euids.clear();

                eventClusters.add(curEvent);
//                i++;
                //reset curEvent;
//                curEvent.EventReset();
                line = f_open.readLine();
            }
            f_open.close();
        }
        ret = eventClusters.size();
        return ret;
    }

    public static float EventMigrationProb(SubEvent seFirst, SubEvent seMigrate, ArrayList<UserProfile> UserProfileHashMap) {
        float Probr = 0; //if seMigrate is the migration of social event seFirst, then ret=1, otherwise ret=0;

        int seFirstULen = seFirst.GetEventUserIDs().size();
        int migrateULen = seMigrate.GetEventUserIDs().size();

        for (int i = 0; i < seFirst.GetEventUserIDs().size(); i++) {
            EUserFrePair sfit = seFirst.GetEventUserIDs().get(i);
            for (int j = 0; j < seMigrate.GetEventUserIDs().size(); j++) {
                EUserFrePair sefit = seMigrate.GetEventUserIDs().get(j);
                Probr += GetUserSimi(UserProfileHashMap.get(sefit.userid), UserProfileHashMap.get(sefit.userid));
            }
        }

        int lenTimes = seFirstULen * migrateULen;
        if (lenTimes == 0)
            Probr = 0;
        else
            Probr /= (seFirstULen * migrateULen);
        return Probr;
    }

    public static float GetUserSimi(UserProfile up1, UserProfile up2) {
        float ret = 0;
        for (int i = 0; i < up1.UserInfluenceDistri.size(); i++) {
            UPInfluDistriEle upidit = up1.UserInfluenceDistri.get(i);
            if (upidit.userid == up2.userId) {
                ret = upidit.userInflu;
                break;
            }
        }
        return ret;
    }


}

