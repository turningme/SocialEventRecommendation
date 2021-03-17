package Xi_recommendation;

import java.io.*;
import java.util.ArrayList;


public class EventMigrationPartation {
    public EventMigrationPartation() {
    }

    public void EventMigrationPartation(String MTimeSlotFilist, String path, String outpath,
                                        String userlist, String userProfilePath,
                                        String UserInfluFilePath) throws IOException {

        EventMigration migrateEvent = new EventMigration();
        ArrayList<SubEvent> UPEventList = new ArrayList<>();
        ArrayList<UserProfile> UserProfileHashMap = new ArrayList<>();

        String UserInfluFileName = "UserInfluDictfile_Nepal07(Training15April-24April).txt";
        String userinfluenceFile = UserInfluFilePath + UserInfluFileName;
        migrateEvent.loadUserProfileHashMap(userinfluenceFile, UserProfileHashMap);

        migrateEvent.uploadUserProfilesIntoHashMap(userlist,UserProfileHashMap);
        migrateEvent.BulkLoadUserHistoryEvents(path,UPEventList);

        EventRecomOpti eropti=new EventRecomOpti();
        int numofGroups = 10;
//        ArrayList<UPEventPartition> uprofileEventGroupSet = new ArrayList<UPEventPartition>();

        String FullSlotPartitionFileName = outpath + MTimeSlotFilist + ".UPP";
        ArrayList<UPEventPartition> uprofileEventGroupSet=new ArrayList<>();

        LoadingUserPartitionFile(FullSlotPartitionFileName, uprofileEventGroupSet);

        String[] filelist= FileUtil.listFiles(path);
        for(String file:filelist) {
            String filename=path+file;
            ArrayList<SubEvent> Eventclusters=new ArrayList<SubEvent>();
            loadMigrationEventDetectResultSummary.loadMigrationEventDetectResultSummary(filename, Eventclusters);

            ArrayList<SubEvent> IncomingEventSubset=new ArrayList<SubEvent>();
            float SimiThreshold = (float) 0.1;
            float alpha = parameters.ALPHA;
            EventRecommendation eventRec=new EventRecommendation(alpha, UPEventList.size());
            for (int i = 0; i < numofGroups; i++)
            {

                EventRecomOpti.IncomingEventSubsetIdentification(Eventclusters, IncomingEventSubset,
                        uprofileEventGroupSet.get(i), SimiThreshold, alpha, UserProfileHashMap);
                EventRecomOpti.EventSimilarityJoin(uprofileEventGroupSet.get(i), IncomingEventSubset,
                        SimiThreshold, eventRec,UserProfileHashMap);
                //output the recommendation list of IncomingEventSubset
//                char RecommendationResult[200];
                String RecommendationResult="EventRecResult.txt";

                BufferedWriter resultFile = new BufferedWriter(new FileWriter(new File(RecommendationResult)));
                for(SubEvent iesit:IncomingEventSubset){
                    String s="<clusterid> "+iesit.GetEventNo()+" </clusterid>\n";
                    resultFile.write(s);
                    for(EventUserSimi eusit:iesit.RecUserSimi){
                        s="<recitem>\t"+eusit.userid+"\t"+eusit.simi+"</recitem>\n";
                    }
                    iesit.RecUserSimi.clear();
                }
                resultFile.close();
                //=======================================================
                IncomingEventSubset.clear();
                System.out.println("join %d\n"+ i);
            }
        }
    }

//    <groupid> 0 </groupid>
//<TopicRange>	0.8768 0.0029   0.2691 -0.5696	0.6845 -0.1577	0.3184 -0.1664	0.1671 -0.4065	0.1344 -0.1863	0.0628 -0.5864	0.1770 -0.3272	0.0900 -0.0866	0.1022 -0.0559	0.0320 -0.0470	0.2763 -0.0713	0.0395 -0.0586	0.1362 -0.0170	0.0254 -0.5921	0.3577 -0.2410	0.0862 -0.4619	0.2835 -0.1387	0.1628 -0.1749	0.0005 -0.0010	0.1077 -0.7273	0.2198 -0.1582	0.0331 -0.8792	0.1447 -0.3917	0.4062 -0.1655	0.1889 -0.2470	0.0130 -0.0098	0.7000 -0.1363	0.1577 -0.2719	0.1984 -0.2036	0.2867 -0.3891	0.3151 -0.3317	0.4334 -0.1986	0.3544 -0.1331	0.1133 -0.2811	0.3813 -0.2446	0.1920 -0.3964	0.2610 -0.1850	0.1285 -0.1993	0.1892 -0.1868	0.1976 -0.2895	0.1393 -0.3415	0.4615 -0.0818	0.1189 -0.1465	0.1433 -0.0782	0.0785 -0.1036	0.1055 -0.1629	0.0969 -0.0894	0.3456 -0.2232	0.1324 -0.1120	</TopicRange>
//<TimeRangePair> 1661.7000	17.6000	 </TimeRangePair>
//<SpaceRangePair> 56.2500 -15.3429	121.5598 -61.8124	</SpaceRangePair>
//<influenceRangeVec>	244 1.0000 1.0000	277 1.0000 1.0000	327 1.0000 1.0000	445 1.0000 1.0000	465 1.0000 1.0000	478 1.0000 1.0000	487 1.0000 1.0000	604 1.0000 1.0000	619 0.0417 0.0417	622 1.0000 1.0000	718 1.0000 1.0000	721 1.0000 1.0000	865 1.0000 1.0000	866 1.0000 1.0000	965 1.0000 1.0000	1030 1.0000 1.0000	1097 1.0000 1.0000	1300 1.0000 1.0000	1325 1.0000 1.0000	1360 1.0000 1.0000	1408 1.0000 1.0000	1508 1.0000 1.0000	1523 1.0000 1.0000	1573 1.0000 1.0000	1576 1.0000 1.0000	1584 1.0000 1.0000	1734 1.0000 1.0000	1792 1.0000 1.0000	1880 1.0000 1.0000	1937 1.0000 1.0000	2044 1.0000 1.0000	2161 1.0000 1.0000	2187 1.0000 1.0000	2229 1.0000 1.0000	2290 1.0000 1.0000	2294 1.0000 1.0000	2347 1.0000 1.0000	2352 1.0000 1.0000	2357 1.0000 1.0000	2360 1.0000 1.0000	2363 1.0000 1.0000	2381 1.0000 1.0000	2533 1.0000 1.0000	2548 1.0000 1.0000	2667 1.0000 1.0000	2677 1.0000 1.0000	2691 1.0000 1.0000	2714 1.0000 1.0000	2776 1.0000 1.0000	2794 1.0000 1.0000	2808 1.0000 1.0000	2886 1.0000 1.0000	2961 1.0000 1.0000	3125 1.0000 1.0000	3215 1.0000 1.0000	3266 1.0000 1.0000	3437 1.0000 1.0000	3506 1.0000 1.0000	3536 1.0000 1.0000	3590 1.0000 1.0000	3608 1.0000 1.0000	3667 1.0000 1.0000	3906 1.0000 1.0000	3957 1.0000 1.0000	3986 1.0000 1.0000	4199 1.0000 1.0000	4230 1.0000 1.0000	4343 1.0000 1.0000	4357 1.0000 1.0000	4416 1.0000 1.0000	4461 1.0000 1.0000	4587 1.0000 1.0000	4617 1.0000 1.0000	4912 1.0000 1.0000	4920 1.0000 1.0000	4934 1.0000 1.0000	4984 1.0000 1.0000	5114 1.0000 1.0000	5123 1.0000 1.0000	5205 1.0000 1.0000	5224 1.0000 1.0000	5228 1.0000 1.0000	5246 1.0000 1.0000	17432 0.1000 0.1000	29307 0.0208 0.0208	33297 0.0068 0.0068	34250 0.0667 0.0667	43789 0.0333 0.0333	60314 0.0333 0.0333	61529 1.0000 1.0000	61957 0.0208 0.0208	64221 0.0208 0.0208	244 1.0000 1.0000	277 1.0000 1.0000	327 1.0000 1.0000	445 1.0000 1.0000	465 1.0000 1.0000	478 1.0000 1.0000	487 1.0000 1.0000	604 1.0000 1.0000	619 0.0417 0.0417	622 1.0000 1.0000	718 1.0000 1.0000	721 1.0000 1.0000	865 1.0000 1.0000	866 1.0000 1.0000	965 1.0000 1.0000	1030 1.0000 1.0000	1097 1.0000 1.0000	1300 1.0000 1.0000	1325 1.0000 1.0000	1360 1.0000 1.0000	1408 1.0000 1.0000	1508 1.0000 1.0000	1523 1.0000 1.0000	1573 1.0000 1.0000	1576 1.0000 1.0000	1584 1.0000 1.0000	1734 1.0000 1.0000	1792 1.0000 1.0000	1880 1.0000 1.0000	1937 1.0000 1.0000	2044 1.0000 1.0000	2095 1.0000 1.0000	2161 1.0000 1.0000	2187 1.0000 1.0000	2229 1.0000 1.0000	2290 1.0000 1.0000	2294 1.0000 1.0000	2347 1.0000 1.0000	2352 1.0000 1.0000	2357 1.0000 1.0000	2360 1.0000 1.0000	2363 1.0000 1.0000	2381 1.0000 1.0000	2533 1.0000 1.0000	2548 1.0000 1.0000	2667 1.0000 1.0000	2677 1.0000 1.0000	2691 1.0000 1.0000	2714 1.0000 1.0000	2776 1.0000 1.0000	2794 1.0000 1.0000	2808 1.0000 1.0000	2886 1.0000 1.0000	2961 1.0000 1.0000	3125 1.0000 1.0000	3215 1.0000 1.0000	3266 1.0000 1.0000	3437 1.0000 1.0000	3506 1.0000 1.0000	3536 1.0000 1.0000	3590 1.0000 1.0000	3608 1.0000 1.0000	3667 1.0000 1.0000	3906 1.0000 1.0000	3957 1.0000 1.0000	3986 1.0000 1.0000	4199 1.0000 1.0000	4230 1.0000 1.0000	4343 1.0000 1.0000	4357 1.0000 1.0000	4416 1.0000 1.0000	4461 1.0000 1.0000	4587 1.0000 1.0000	4617 1.0000 1.0000	4912 1.0000 1.0000	4920 1.0000 1.0000	4934 1.0000 1.0000	4984 1.0000 1.0000	5114 1.0000 1.0000	5123 1.0000 1.0000	5205 1.0000 1.0000	5224 1.0000 1.0000	5228 1.0000 1.0000	5246 1.0000 1.0000	5335 1.0000 1.0000	5483 1.0000 1.0000	5513 1.0000 1.0000	5523 1.0000 1.0000	5648 1.0000 1.0000	5684 1.0000 1.0000	5746 1.0000 1.0000	5783 1.0000 1.0000	5805 1.0000 1.0000	5818 1.0000 1.0000	17432 0.1000 0.1000	29307 0.0208 0.0208	33297 0.0068 0.0068	34250 0.0667 0.0667	43789 0.0333 0.0333	60314 0.0333 0.0333	61529 1.0000 1.0000	61957 0.0208 0.0208	64221 0.0208 0.0208	</influenceRangeVec>
//<MaxMinUN>	 3 1	 </MaxMinUN>
//<clusterid> 5158 </clusterid>
//<msgcontfidf>	0.0245	-0.0507	-0.0282	0.0222	-0.0188	0.0380	-0.2533	-0.1236	0.0169	-0.0031	-0.0038	-0.0045	0.0031	-0.0049	-0.0020	-0.0015	0.0019	-0.0044	0.0133	0.0001	-0.0050	0.0091	0.0024	0.0206	-0.0021	-0.0044	0.0026	0.0220	-0.0570	0.0394	0.0063	-0.0348	0.0021	0.0325	-0.0056	0.0702	0.0784	0.0416	-0.0769	-0.0415	0.0750	-0.0896	0.0137	-0.0968	0.0091	0.0113	0.0294	-0.0075	-0.0057	0.0107	</msgcontfidf>
//<SpaceRange>	10.7156	-34.5989	3775.1230	</SpaceRange>
//<TimeRange>	54.5333	3.0000	</TimeRange>
//<SpaceRangeSet>	10.7156 -69.1978 0.0000	</SpaceRangeSet>
//<TimeRangeSet>	54.5333 3.0000	</TimeRangeSet>
//<HashkeyValues>	2147450880	321575583	695220760	543960233	194892443	</HashkeyValues>
//<useridlist>	5335 2	</useridlist>

    public void LoadingUserPartitionFile(String FullSlotPartitionFileName,
                                         ArrayList<UPEventPartition> uprofileEventGroupSet) throws IOException {

        BufferedReader f_open = new BufferedReader(new FileReader(new File(FullSlotPartitionFileName)));
        String line = f_open.readLine();
        while (line != null) {
            if(line.contains("<groupid>")){

                UPEventPartition group=new UPEventPartition();
                ArrayList<SubEvent> subevents=new ArrayList<SubEvent>();

                int groupid=Integer.parseInt(line.substring(line.indexOf("<groupid>")+9,line.indexOf("</groupid>")).trim());
//                group.SetGroupId(groupid);

                line=f_open.readLine();

                String[] tr=line.substring(line.indexOf("<TopicRange>")+12,line.indexOf("</TopicRange>")).trim().split("\t");
                ValueRangePair[] topicrangevec=new ValueRangePair[parameters.TFIDF_DIM];
//                ArrayList<float[]> topicrange=new ArrayList<>();
                for(int i=0;i<tr.length;i++){
                    float[] temp=new float[2];
                    ValueRangePair vrp=new ValueRangePair();
                    vrp.minV=Float.parseFloat(tr[i].split(" ")[0]);
                    vrp.maxV=Float.parseFloat(tr[i].split(" ")[1]);
                    topicrangevec[i]=vrp;
                }
                group.setTopicRangeVector(topicrangevec);

                line=f_open.readLine();

                ValueRangePair timerange=new ValueRangePair();
                String[] timereangepair=line.substring(line.indexOf("<TimeRangePair>")+15,line.indexOf("</TimeRangePair>")).trim().split("\t");
                timerange.minV=Float.parseFloat(timereangepair[0]);
                timerange.maxV=Float.parseFloat(timereangepair[1]);
                group.setTimeRangePair(timerange);

                line=f_open.readLine();

                ValueRangePair[] spacerangevec=new ValueRangePair[2];
                String[] spacereangepair=line.substring(line.indexOf("<SpaceRangePair>")+16,line.indexOf("</SpaceRangePair>")).trim().split("\t");
                for(int i=0;i<2;i++) {
                    if (spacerangevec[i] == null){
                        spacerangevec[i] = new ValueRangePair();
                    }
                    spacerangevec[i].minV = Float.parseFloat(spacereangepair[i].split(" ")[0]);
                    spacerangevec[i].maxV = Float.parseFloat(spacereangepair[i].split(" ")[1]);
                }

                group.setSpaceRangePair(spacerangevec);

                line=f_open.readLine();

                String[] infvecs=line.substring(line.indexOf("<influenceRangeVec>")+19,line.indexOf("</influenceRangeVec>")).trim().split("\t");
                ArrayList<UserValueRangePair> infvec=new ArrayList<>();
                for(String s:tr){
//                    float[] temp=new float[3];
                    UserValueRangePair temp=new UserValueRangePair();
                    temp.userid=Integer.parseInt(s.split(" ")[0]);
                    temp.minV=Float.parseFloat(s.split(" ")[1]);
                    temp.maxV=Float.parseFloat(s.split(" ")[2]);
                    infvec.add(temp);
                }
                group.setInfluenceRangeVector(infvec);

                line=f_open.readLine();
                String[] un=line.substring(line.indexOf("<MaxMinUN>")+10,line.indexOf("</MaxMinUN>")).trim().split("\t");
                ValueRangePair UN=new ValueRangePair();
                UN.minV=Integer.parseInt(un[0]);
                UN.maxV=Integer.parseInt(un[1]);
                group.SetUN(UN);

                line=f_open.readLine();
                while(line.contains("<clusterid")){
                    SubEvent curevent=new SubEvent();
                    curevent=parseSubEvent(f_open,line);
                    subevents.add(curevent);
                    line=f_open.readLine();
                }
                uprofileEventGroupSet.add(group);
            }
        }
        f_open.close();
    }

    public static void main(String[] args){
        try {
            new EventMigrationPartation().LoadingUserPartitionFile("/Users/jpliu/Downloads/Nepal_UserHistUPPartion(Feb28)/statuses.log.2015-04-26-01.UPP",new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SubEvent parseSubEvent(BufferedReader f_open, String line) throws IOException {

        SubEvent curEvent = new SubEvent();
        float[] vec = new float[50];
        TimeRange newTR = new TimeRange();
        SpaceRange newSR = new SpaceRange();
        ArrayList<EUserFrePair> euids=new ArrayList<>();

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

        if (line.contains("<HashkeyValues>")) {
            String[] Hashvalues = line.substring(line.indexOf("<HashkeyValues>") + 15, line.indexOf("</HashkeyValues>")).trim().split(" ");
            for (int i = 0; i < Hashvalues.length; i++) {
                for (int j = 0; j < parameters.MVALUE; j++) {
                    curEvent.EHashV.set(j, Integer.parseInt(Hashvalues[i]));
                }
            }
        }

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
            String[] TimeRangeSet = line.substring(line.indexOf("<TimeRangeSet>") + 14, line.indexOf("</TimeRangeSet>")).trim().split(" ");
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
                while (euit <= euids.size()) {
                    if (euids.get(euit).userid > eufPair.userid) {
                        euids.set(euit, eufPair);
                        euit = 0;
                        break;
                    }
                    euit++;
                }
                if (euit == euids.size() - 1){
                    euids.add(eufPair);
                }

            }
        }
        curEvent.setEventUserIDs(euids);
        return curEvent;
    }
}
