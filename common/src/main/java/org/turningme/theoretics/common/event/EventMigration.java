package org.turningme.theoretics.common.event;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.turningme.theoretics.common.RecContext;
import org.turningme.theoretics.common.beans.EUserFrePair;
import org.turningme.theoretics.common.beans.SocialMSG;
import org.turningme.theoretics.common.beans.SpaceRange;
import org.turningme.theoretics.common.beans.TimeRange;
import org.turningme.theoretics.common.beans.UPInfluDistriEle;
import org.turningme.theoretics.common.beans.UPRespnseEle;
import org.turningme.theoretics.common.beans.UserProfile;

import static org.turningme.theoretics.common.Constants.SPACERADIUST;
import static org.turningme.theoretics.common.Constants.TFIDF_DIM;
import static org.turningme.theoretics.common.Constants.TIMERADIUST;

/**
 * Created by jpliu on 2020/2/24.
 */


// load user data naive ?
public class EventMigration {

    Map<Integer, UserProfile> UserProfileHashMap = new HashMap<>();

    RecContext recContext;

    public EventMigration() {
    }

    public EventMigration(RecContext recContext) {
        this.recContext = recContext;
        recContext.setEventMigration(this);
    }


    public int loadUserProfileHashMap(String UIfname) {
        File fileDescriptor = new File(UIfname);
        if (!fileDescriptor.exists()) {
            System.out.printf("load msg error \n");
            System.out.printf("The file 'crt_fopen_s.c' was not opened: %s\n", UIfname);
            System.exit(-1);
        } else {

            BufferedReader bufferedReader = null;
            try {
                int uid = 0;

                bufferedReader = new BufferedReader(new FileReader(fileDescriptor));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    StringTokenizer stringTokenizer = new StringTokenizer(line, " \t\r\n");
                    List<String> tokens = new ArrayList<>();
                    while (stringTokenizer.hasMoreElements()) {
                        tokens.add(stringTokenizer.nextToken());
                    }

                    int length = tokens.size();
                    if (length <= 0) {
                        System.out.printf("Invalid (empty) line!\n");
                        return 1;
                    }

                    int i = 0;
                    while (i < length) {

                        if ("<USERID>".equals(tokens.get(i))) {
                            i++;

                            while (!"</USERID>".equals(tokens.get(i))) {
                                uid = Integer.parseInt(tokens.get(i));
                                UserProfileHashMap.putIfAbsent(uid, new UserProfile());
                                UserProfileHashMap.get(uid).userId = uid;
                                i++;
                            }

                            i++;
                            continue;
                        }


                        if ("<USERINFLUENCEDISTRI>".equals(tokens.get(i))) {
                            i++;

                            while (!"</USERINFLUENCEDISTRI>".equals(tokens.get(i))) {
                                UPInfluDistriEle upInfluDistriEle = new UPInfluDistriEle();

                                upInfluDistriEle.userid = Integer.parseInt(tokens.get(i));
                                i++;


                                upInfluDistriEle.userInflu = Float.parseFloat(tokens.get(i));
                                i++;

                                UserProfileHashMap.putIfAbsent(uid, new UserProfile());
                                UserProfileHashMap.get(uid).UserInfluenceDistri.add(upInfluDistriEle);
                            }

                            i++;
                            continue;
                        }

                        i++;
                    }


                }
            } catch (Exception e) {
                System.out.printf("error%", e);
                return 1;
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.printf("buffer Reader error", e);
                }
            }

        }


        return 0;
    }


    // load datailed user  profile data
    public int uploadUserProfilesIntoHashMap(String flist) {
        File fileDescriptor = new File(flist);
        if (!fileDescriptor.exists()) {
            System.out.printf("load msg error \n");
            System.out.printf("The file 'crt_fopen_s.c' was not opened: %s\n", flist);
            System.exit(-1);
        } else {


            BufferedReader bufferedReader = null;


            try {


                bufferedReader = new BufferedReader(new FileReader(fileDescriptor));
                String line;
                UserProfile up = UserProfile.createDefaultUP();


                while ((line = bufferedReader.readLine()) != null) {
                    StringTokenizer stringTokenizer = new StringTokenizer(line, " \t\r\n");
                    List<String> tokens = new ArrayList<>();
                    while (stringTokenizer.hasMoreElements()) {
                        tokens.add(stringTokenizer.nextToken());
                    }

                    int length = tokens.size();
                    if (length <= 0) {
                        System.out.printf("Invalid (empty) line!\n");
                        return 1;
                    }

                    int i = 0;
                    while (i < length) {
                        if ("<userid>".equals(tokens.get(i))) {
                            i++;
                            int j = 0;
                            while (!"</userid>".equals(tokens.get(i))) {
                                up.userId = Integer.parseInt(tokens.get(i));
                                i++;
                            }
                            i++;
                            continue;
                        }


                        if ("<location>".equals(tokens.get(i))) {
                            i++;
                            int j = 0;


                            while (!"</location>".equals(tokens.get(i))) {
                                if (j == 0) {
                                    up.UserPhysicalLocation.lat = Float.parseFloat(tokens.get(i));
                                    j++;
                                } else {
                                    up.UserPhysicalLocation.longi = Float.parseFloat(tokens.get(i));
                                }
                                i++;
                            }

                            up.UserPhysicalLocation.radius = SPACERADIUST;
                            i++;
                            continue;
                        }


                        if ("<MSGNUM>".equals(tokens.get(i))) {
                            i++;
                            while (!"</MSGNUM>".equals(tokens.get(i))) {
                                up.PostNum = Integer.parseInt(tokens.get(i));
                                i++;
                            }
                            i++;
                            continue;
                        }


                        if ("<RESPONSENUMBERS>".equals(tokens.get(i))) {
                            i++;
                            while (!"</RESPONSENUMBERS>".equals(tokens.get(i))
                                    && !"<</RESPONSENUMBERS>".equals(tokens.get(i))) {
                                UPRespnseEle UPRele = new UPRespnseEle();
                                UPRele.userid = Integer.parseInt(tokens.get(i));
                                i++;
                                UPRele.userResponse = Integer.parseInt(tokens.get(i));
                                i++;
                                up.UserResponseNumbers.add(UPRele);

                                UPInfluDistriEle UPIde = new UPInfluDistriEle();
                                UPIde.userid = UPRele.userid;
                                if (up.userId == UPIde.userid)
                                    UPIde.userInflu = 1;
                                else
                                    UPIde.userInflu = 0;//initialization to 0.
                                up.UserInfluenceDistri.add(UPIde);
                            }
                            i++;
                            ///===finish a user profile record and insert it into the hashmap

                            UserProfileHashMap.get(up.userId).userId = up.userId;
                            UserProfileHashMap.get(up.userId).PostNum = up.PostNum;
                            UserProfileHashMap.get(up.userId).UserPhysicalLocation.lat = up.UserPhysicalLocation.lat;
                            UserProfileHashMap.get(up.userId).UserPhysicalLocation.longi = up.UserPhysicalLocation.longi;
                            UserProfileHashMap.get(up.userId).UserPhysicalLocation.radius = up.UserPhysicalLocation.radius;

                            // modify from c++ vector insert which I think is the same
                            UserProfileHashMap.get(up.userId).UserResponseNumbers.addAll(up.UserResponseNumbers);
                            UserProfileHashMap.get(up.userId).Inserted = 0;
                            //reset up for next user profile reading.
                            up.reset();
                            ///////==========================
                            continue;
                        }

                        i++;


                    }


                }

            } catch (Exception e) {
                System.out.printf("error in XXX %s \n", e);
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.printf("error in YYY %s \n ", e);
                }
            }

        }


        return 0;
    }


    //load user history events
    public List<SocialEvent> BulkLoadUserHistoryEventsForSpark(String userflist) {
        List<SocialEvent> result = new ArrayList<>();
        String base = "/Users/jpliu/CLionProjects/EventRecoHelper/VirtualHisEventPath/";
        String contentPath = "%s/%s%s";
        File fileDescriptor = new File(userflist);
        if (!fileDescriptor.exists()) {
            System.out.printf("load msg error \n");
            System.out.printf("The file 'crt_fopen_s.c' was not opened: %s\n", userflist);
            System.exit(-1);
        } else {


            BufferedReader bufferedReader = null;

            try {
                bufferedReader = new BufferedReader(new FileReader(fileDescriptor));
                String line;


                while ((line = bufferedReader.readLine()) != null) {
                    int userid = Integer.parseInt(line);
                    String fName = String.format(contentPath, base, userid, ".UPdata");
                    if (userid % 1000 == 0)
                        System.out.printf("userid=%d\n", userid);

                    result.addAll(LoadUserHistoryEventsForSpark(fName, userid));
                }
            } catch (Exception e) {
                System.out.printf("%s \n", e);
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.printf("%s \n", e);
                }
            }


        }

        return result;
    }


    public List<SocialEvent> LoadUserHistoryEventsForSpark(String UEventfname, int userId) {
        List<SocialEvent> elist = new ArrayList<>();
        File fileDescriptor = new File(UEventfname);
        if (!fileDescriptor.exists()) {
            System.out.printf("load msg error \n");
            System.out.printf("The file 'crt_fopen_s.c' was not opened: %s\n", UEventfname);
            System.exit(-1);
        } else {


            BufferedReader bufferedReader = null;


            try {
                bufferedReader = new BufferedReader(new FileReader(fileDescriptor));
                String line;

                float[] vec = new float[TFIDF_DIM];
                TimeRange newTR = new TimeRange(0.0f, 0);
                SpaceRange newSR = new SpaceRange(0.0f, 0.0f, 0.0f);
                List<EUserFrePair> euids = new ArrayList<>();
                int msgid = 0;

                int isMSG = 0;
                int isHashtag = 0;
                SocialEvent curEvent = new SocialEvent();
                curEvent.EventReset();


                while ((line = bufferedReader.readLine()) != null) {

                    StringTokenizer stringTokenizer = new StringTokenizer(line, " \t\r\n");
                    List<String> tokens = new ArrayList<>();
                    while (stringTokenizer.hasMoreElements()) {
                        tokens.add(stringTokenizer.nextToken());
                    }

                    int length = tokens.size();
                    if (length <= 0) {
                        System.out.printf("Invalid (empty) line!\n");
                        System.exit(-1);
                    }

                    int i = 0;
                    while (i < length) {
                        if ("<clusterid>".equals(tokens.get(i))) {
                            isMSG = 0;
                            //=====================
                            if (curEvent.GetEventMSGs().size() > 0)  //not the first event, then insert the previous event
                            {
                                //set the user-event interaction recorders between curEvent and user id
                                curEvent.SetEventUserIDs();
                                curEvent.SetCluster_ConceptTFIDFVec();
                                curEvent.SetSpaceRange();
                                curEvent.SetTimeRange();
                                //===========================================
                                elist.add(curEvent);
                                //reset curEvent;
                                curEvent = new SocialEvent();
                                curEvent.EventReset();
                            }
                            //=====================
                            i++;
                            while (!"</clusterid>".equals(tokens.get(i))) {
                                curEvent.SetEventNo(Integer.parseInt(tokens.get(i)));
                                i++;
                            }
                            i++;
                            continue;
                        }


                        if ("<hashtags>".equals(tokens.get(i))) {
                            isMSG = 1;
                            i++;
                            while (!"</hashtags>".equals(tokens.get(i))) {
                                isHashtag = Integer.parseInt(tokens.get(i));
                                i++;
                            }
                            i++;
                            continue;
                        }


                        if ("<msgcontfidf>".equals(tokens.get(i))) {
                            isMSG = 1;
                            int j = 0;
                            i++;
                            while (!"</msgcontfidf>".equals(tokens.get(i))) {
                                vec[j] = Float.parseFloat(tokens.get(i));
                                i++;
                                j++;
                            }
                            i++;
                            continue;
                        }


                        if ("<msgid>".equals(tokens.get(i))) {
                            isMSG = 1;
                            i++;
                            while (!"</msgid>".equals(tokens.get(i))) {
                                msgid = Integer.parseInt(tokens.get(i));
                                i++;
                            }
                            i++;
                            continue;
                        }


                        if ("<coordinates>".equals(tokens.get(i))) {
                            isMSG = 1;
                            i++;
                            int j = 0;
                            while (!"</coordinates>".equals(tokens.get(i))) {
                                if (j == 0) {
                                    newSR.lat = Float.parseFloat(tokens.get(i));
                                    j++;
                                } else {
                                    newSR.longi = Float.parseFloat(tokens.get(i));
                                }
                                i++;
                            }
                            newSR.radius = SPACERADIUST;
                            i++;
                            continue;
                        }


                        if ("<timestamp_ms>".equals(tokens.get(i))) {
                            isMSG = 1;
                            i++;
                            while (!"</timestamp_ms>".equals(tokens.get(i))) {
                                newTR.TimeStampCentre = Float.parseFloat(tokens.get(i).substring(5, 5 + 5)) / 60;
                                newTR.range = TIMERADIUST;
                                i++;
                            }
                            i++;
                            continue;
                        }


                        if ("<useridlist>".equals(tokens.get(i))) {
                            isMSG = 1;
                            i++;
                            while (!"</useridlist>".equals(tokens.get(i))) {
                                EUserFrePair eufPair = new EUserFrePair();
                                eufPair.userid = Integer.parseInt(tokens.get(i));

                                int kk = 0;
                                for (kk = 0; kk < euids.size(); kk++) {
                                    if (euids.get(kk).userid == eufPair.userid)
                                        break;
                                }

                                if (euids.size() > 0 && kk < euids.size()) {
                                    euids.get(kk).frequency++;
                                } else {
                                    eufPair.frequency = 1;
                                    euids.add(eufPair);
                                }
                                i++;
                            }
                            i++;
                            continue;
                        }

                    }

                    if (isMSG == 1) {  //is message, then add it into the messagelist of
                        SocialMSG newsocialmsg = new SocialMSG(vec, newTR, newSR, euids);
                        newsocialmsg.setMSGID(msgid);
                        if (isHashtag != 0) {
                            newsocialmsg.hashtaged = isHashtag;
                            isHashtag = 0;
                        }
                        curEvent.uploadEventMsg(newsocialmsg);
                        euids.clear();
                    }


                }//end of while reading file

                if (curEvent.GetEventMSGs().size() > 0)  // insert the last event
                {
                    //set the user-event interaction recorders between curEvent and user id
                    curEvent.SetEventUserIDs();
                    curEvent.SetCluster_ConceptTFIDFVec();
                    curEvent.SetSpaceRange();
                    curEvent.SetTimeRange();

                    elist.add(curEvent);
                    //reset curEvent;
                    curEvent.EventReset();
                }
            } catch (Exception e) {
                System.out.printf(" %s \n", e);
                e.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.printf(" %s \n", e);
                }
            }


        }

        return elist;

    }


    void loadMessageSlotForTraining(String FullSlotFileName, List<SocialMSG> HashTagedMSGlist,
            List<SocialMSG> NonHashTagedMSGlist, int startMSGno) {

        List<SocialEvent> elist = new ArrayList<>();
        File fileDescriptor = new File(FullSlotFileName);
        if (!fileDescriptor.exists()) {
            System.out.printf("load msg error \n");
            System.out.printf("The file 'crt_fopen_s.c' was not opened: %s\n", FullSlotFileName);
            System.exit(-1);
        } else {


            BufferedReader bufferedReader = null;


            try {
                bufferedReader = new BufferedReader(new FileReader(fileDescriptor));
                String line;

                float[] vec = new float[TFIDF_DIM];
                TimeRange newTR = new TimeRange(0.0f, 0);
                SpaceRange newSR = new SpaceRange(0.0f, 0.0f, 0.0f);
                List<EUserFrePair> euids = new ArrayList<>();
                String msgid = "";

                int isMSG = 0;
                int isHashtag = 0;
                boolean hflag = false;
                List<String> hashtags = new ArrayList<>();
                int msgno = startMSGno;

                SocialEvent curEvent = new SocialEvent();
                curEvent.EventReset();


                while ((line = bufferedReader.readLine()) != null) {

                    StringTokenizer stringTokenizer = new StringTokenizer(line, " \t\r\n");
                    List<String> tokens = new ArrayList<>();
                    while (stringTokenizer.hasMoreElements()) {
                        tokens.add(stringTokenizer.nextToken());
                    }

                    if (msgno == 7625)
                        System.out.printf("find null userlist\n");


                    int length = tokens.size();
                    if (length <= 0) {
                        System.out.printf("Invalid (empty) line!\n");
                        System.exit(-1);
                    }

                    int i = 0;
                    while (i < length) {
                        if ("<hashtags>".equals(tokens.get(i))) {
                            i++;
                            while (!"</hashtags>".equals(tokens.get(i))) {
                                hflag = true;
                                i++;
                            }
                            i++;
                            continue;
                        }


                        if ("<msgcontfidf>".equals(tokens.get(i))) {
                            int j = 0;
                            i++;
                            while (!"</msgcontfidf>".equals(tokens.get(i))) {
                                vec[j] = Float.parseFloat(tokens.get(i));
                                i++;
                                j++;
                            }
                            i++;
                            continue;
                        }


                        if ("<msgid>".equals(tokens.get(i))) {
                            i++;
                            while (!"</msgid>".equals(tokens.get(i))) {
                                msgid = tokens.get(i);
                                i++;
                            }
                            i++;
                            continue;
                        }


                        if ("<coordinates>".equals(tokens.get(i))) {
                            i++;
                            int j = 0;
                            while (!"</coordinates>".equals(tokens.get(i))) {
                                if (j == 0) {
                                    newSR.lat = Float.parseFloat(tokens.get(i));
                                    j++;
                                } else {
                                    newSR.longi = Float.parseFloat(tokens.get(i));
                                }
                                i++;
                            }
                            newSR.radius = SPACERADIUST;
                            i++;
                            continue;
                        }


                        if ("<timestamp_ms>".equals(tokens.get(i))) {
                            i++;
                            while (!"</timestamp_ms>".equals(tokens.get(i))) {
                                newTR.TimeStampCentre = Float.parseFloat(tokens.get(i).substring(5, 5 + 5)) /
                                        60; //(float)atof(strtok.token(i).substr(4,10).c_str())/60;  //we use minutes not ms
                                newTR.range = TIMERADIUST;
                                i++;
                            }
                            i++;
                            continue;
                        }


                        if ("<useridlist>".equals(tokens.get(i))) {

                            i++;
                            while (!"</useridlist>".equals(tokens.get(i))) {
                                EUserFrePair eufPair = new EUserFrePair();
                                eufPair.userid = Integer.parseInt(tokens.get(i));

                                int kk = 0;
                                for (kk = 0; kk < euids.size(); kk++) {
                                    if (euids.get(kk).userid == eufPair.userid)
                                        break;
                                }

                                if (kk < euids.size()) {
                                    euids.get(kk).frequency++;
                                } else {
                                    eufPair.frequency = 1;
                                    euids.add(eufPair);
                                }
                                i++;
                            }
                            i++;
                            continue;
                        }

                        i++;

                    }

                    SocialMSG newsocialmsg = new SocialMSG(vec, newTR, newSR, euids);
                    newsocialmsg.setMSGID(msgno);
                    msgno++;
                    if (hflag) {
                        newsocialmsg.hashtaged = 1;
                        HashTagedMSGlist.add(newsocialmsg);
                    } else {
                        newsocialmsg.hashtaged = 0;
                        NonHashTagedMSGlist.add(newsocialmsg);
                    }

                    //deallocate and reset
                    hflag = false;
                    for (int k = 0; k < TFIDF_DIM; k++) {
                        vec[k] = 0.0f;
                    }
                    newTR.TimeStampCentre = 0.0f;
                    newSR.lat = 0.0f;
                    newSR.longi = 0.0f;
                    euids.clear();
                    ////////////////////////////
                }//read end




            } catch (Exception e) {
                //log4j is needed
                e.printStackTrace();

            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    /////
    //compute the relevance probability of seFirst and seMigrate
    public float EventMigrationProb(SocialEvent seFirst, SocialEvent seMigrate) {
        float Probr = 0; //if seMigrate is the migration of social event seFirst, then ret=1, otherwise ret=0;

        int sfit = 0;
        int sfitend = seFirst.GetEventUserIDs().size();

        int seFirstULen = seFirst.GetEventUserIDs().size();
        int migrateULen = seMigrate.GetEventUserIDs().size();

        while (sfit != sfitend) {
            //if((*sfit).userid)  //value is not 0
            {
                int sefit = 0;
                int sefitend = seMigrate.GetEventUserIDs().size();

                while (sefit != sefitend) {
                    //if ((*sefit).userid)
                    {
                        Probr += GetUserSimi(UserProfileHashMap.get(seFirst.GetEventUserIDs().get(sfit).userid),
                                             UserProfileHashMap.get(seMigrate.GetEventUserIDs().get(sefit).userid));
                    }
                    sefit++;
                }
            }
            sfit++;
        }


        int tmp = seFirstULen * migrateULen; // sometimes while debugging ,the result is zero and divide not legal
        if (0 == tmp){
            tmp = 1;
        }

        Probr /= tmp;
        return Probr;
    }


    /*==============================================
        return the maximal probability from up1 to up2
    ================================================*/
    float GetUserSimi(UserProfile up1, UserProfile up2) {
        float ret = 0;
        //ret = up1.UserInfluenceDistri[up2.userId];


        for (UPInfluDistriEle upidit : up1.UserInfluenceDistri
                ) {
            if ((upidit).userid == up2.userId) {
                ret = (upidit).userInflu;
                break;
            }
        }

        return ret;
    }


}
