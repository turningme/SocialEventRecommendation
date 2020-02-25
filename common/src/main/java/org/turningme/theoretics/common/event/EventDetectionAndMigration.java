package org.turningme.theoretics.common.event;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.turningme.theoretics.common.RecContext;
import org.turningme.theoretics.common.beans.SocialMSG;
import org.turningme.theoretics.common.beans.UPEventPartition;
import org.turningme.theoretics.common.lsb.LSB;

/**
 * Created by jpliu on 2020/2/24.
 */
public class EventDetectionAndMigration implements Serializable{
    String userinfluenceFile = "/Users/jpliu/CLionProjects/EventRecoHelper/UserInfluDictfile.txt";



    public static void UserProfileAllocation() {

        RecContext recCxt = new RecContext();

        EventMigration eventMigration = new EventMigration(recCxt);

        eventMigration.loadUserProfileHashMap("/Users/jpliu/CLionProjects/EventRecoHelper/UserInfluDictfile.txt");
        eventMigration.uploadUserProfilesIntoHashMap("/Users/jpliu/CLionProjects/EventRecoHelper/userprofile217.txt");



        List<SocialEvent> userHisEvents = eventMigration.BulkLoadUserHistoryEventsForSpark("/Users/jpliu/CLionProjects/EventRecoHelper/UserNamelist.txt");

        for (SocialEvent seit:userHisEvents
             ) {
            (seit).SetCluster_ConceptTFIDFVec();
            (seit).SetEventUserIDs();
            (seit).SetSpaceRange();
            (seit).SetTimeRange();
        }



        int numofGroups = 10;
        LSB lsb = new LSB(recCxt);
        lsb.readParaFile( "/Users/jpliu/CLionProjects/EventRecoHelper/para.txt");

        System.out.println("para loading finished\n");
        float alpha = 0.5f;


        List<UPEventPartition> uprofileEventGroupSet = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            uprofileEventGroupSet.add(new UPEventPartition());
        }

        EventRecomOpti eropti = new EventRecomOpti(recCxt);
        eropti.UserProfileDataPartition(userHisEvents, numofGroups, uprofileEventGroupSet, lsb, alpha);




        String FullSlotFileName = "/Users/jpliu/CLionProjects/EventRecoHelper/statuses.log.2015-04-25-16.txt";
        List<SocialMSG> HashTagedMSGlist = new ArrayList<>(), NonHashTagedMSGlist = new ArrayList<>();
        List<SocialEvent> Eventclusters = new ArrayList<>();

        ////hash tag and non hash tag  records count ok


        int startmsgno = 0;
        loadMessageSlotForTraining(FullSlotFileName, HashTagedMSGlist, NonHashTagedMSGlist, startmsgno ,recCxt);

        /////

        SocialEventOperation SEO = new SocialEventOperation(recCxt);
        SEO.OnlineClustering(HashTagedMSGlist, NonHashTagedMSGlist, Eventclusters, lsb);
        HashTagedMSGlist.clear();
        NonHashTagedMSGlist.clear();



        ////
        recCxt.setRecommendation(new EventRecommendation(recCxt));
        List<SocialEvent> IncomingEventSubset = new ArrayList<>();
//        float SimiThreshold = 0.7f;
        float SimiThreshold = 0.f;
        for (int i = 0; i < numofGroups; i++) {
            /// too much code not used in the whole code stack
            eropti.IncomingEventSubsetIdentification(Eventclusters, IncomingEventSubset, uprofileEventGroupSet.get(i),
                                                     SimiThreshold, alpha);
            ///

            eropti.EventSimilarityJoin(uprofileEventGroupSet.get(i), IncomingEventSubset, SimiThreshold);
            IncomingEventSubset.clear();
            System.out.printf("join %d\n", i);

        }


    }


    /**
     *
     * @param FullSlotFileName
     * @param HashTagedMSGlist
     * @param NonHashTagedMSGlist
     * @param startMSGno
     * @param rc
     *
     * two containers HashTagedMSGlist and NonHashTagedMSGlist is something returned
     */
    public static void loadMessageSlotForTraining(String FullSlotFileName, List<SocialMSG> HashTagedMSGlist,
            List<SocialMSG> NonHashTagedMSGlist, int startMSGno ,RecContext rc) {

        rc.getEventMigration().loadMessageSlotForTraining(FullSlotFileName, HashTagedMSGlist,NonHashTagedMSGlist,startMSGno);
    }




}
