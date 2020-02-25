package org.turningme.theoretics.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.turningme.theoretics.common.RecContext;
import org.turningme.theoretics.common.beans.SocialMSG;
import org.turningme.theoretics.common.beans.UPEventBucket;
import org.turningme.theoretics.common.beans.UPEventPartition;
import org.turningme.theoretics.common.event.EventDetectionAndMigration;
import org.turningme.theoretics.common.event.EventMigration;
import org.turningme.theoretics.common.event.EventRecomOpti;
import org.turningme.theoretics.common.event.EventRecommendation;
import org.turningme.theoretics.common.event.SocialEvent;
import org.turningme.theoretics.common.event.SocialEventOperation;
import org.turningme.theoretics.common.lsb.LSB;

/**
 * Created by jpliu on 2020/2/25.
 */
public class PreprocessingHelper implements Serializable {
    static final Logger LOG = LoggerFactory.getLogger(PreprocessingHelper.class);
    RecContext recContext;
    String userProfilePath = "/Users/jpliu/CLionProjects/EventRecoHelper/UserInfluDictfile.txt";
    String userProfileInfoPath = "/Users/jpliu/CLionProjects/EventRecoHelper/userprofile217.txt";
    String userNameList = "/Users/jpliu/CLionProjects/EventRecoHelper/UserNamelist.txt";
    String lsbFilepath = "/Users/jpliu/CLionProjects/EventRecoHelper/para.txt";

    int numofGroups = 10;


    public PreprocessingHelper() {
    }


    EventMigration eventMigration;
    EventRecomOpti eropti;
    SocialEventOperation SEO;
    EventRecommendation eventRecommendation;

    public PreprocessingHelper setup() {
        RecContext recCxt = new RecContext();

        eventMigration = new EventMigration(recCxt);
        eropti = new EventRecomOpti(recCxt);
        SEO = new SocialEventOperation(recCxt);
        eventRecommendation = new EventRecommendation(recCxt);
        recContext = recCxt;
        return this;
    }


    /**
     * with  user profile
     * and  straming messages mini batch which were reduced into  subset
     */
    public void similarityJoinManually() {

/*        List<SocialEvent> IncomingEventSubset = new ArrayList<>();
//        float SimiThreshold = 0.7f;
          float alpha = 0.5f;
        float SimiThreshold = 0.f;
        for (int i = 0; i < numofGroups; i++) {
            /// too much code not used in the whole code stack
            eropti.IncomingEventSubsetIdentification(Eventclusters, IncomingEventSubset, uprofileEventGroupSet.get(i),
                                                     SimiThreshold, alpha);
            ///

            eropti.EventSimilarityJoin(uprofileEventGroupSet.get(i), IncomingEventSubset, SimiThreshold);
            IncomingEventSubset.clear();
            System.out.printf("join %d\n", i);

        }*/
    }

    public PreprocessingHelper preLoadMessageData() {

        String FullSlotFileName = "/Users/jpliu/CLionProjects/EventRecoHelper/statuses.log.2015-04-25-16.txt";
        List<SocialMSG> HashTagedMSGlist = new ArrayList<>(), NonHashTagedMSGlist = new ArrayList<>();
        List<SocialEvent> Eventclusters = new ArrayList<>();

        ////hash tag and non hash tag  records count ok


        int startmsgno = 0;
        EventDetectionAndMigration.loadMessageSlotForTraining(FullSlotFileName, HashTagedMSGlist, NonHashTagedMSGlist, startmsgno, recContext);

        /////


        SEO.OnlineClustering(HashTagedMSGlist, NonHashTagedMSGlist, Eventclusters, recContext.lsb);
        HashTagedMSGlist.clear();
        NonHashTagedMSGlist.clear();

        //context set event cluster
        recContext.setEventclusters(Eventclusters);
        return this;
    }

    public PreprocessingHelper preLoadUsrProfile() {
        LOG.info("start preProcessing on local machine");


        LOG.info("start loading loadUserProfileHashMap ... ");
        eventMigration.loadUserProfileHashMap(userProfilePath);

        LOG.info("start loading uploadUserProfilesIntoHashMap ... ");
        eventMigration.uploadUserProfilesIntoHashMap(userProfileInfoPath);


        LOG.info("start loading BulkLoadUserHistoryEventsForSpark ... ");
        List<SocialEvent> userHisEvents = eventMigration.BulkLoadUserHistoryEventsForSpark(userNameList);


        for (SocialEvent seit : userHisEvents
                ) {
            seit.SetCluster_ConceptTFIDFVec();
            seit.SetEventUserIDs();
            seit.SetSpaceRange();
            seit.SetTimeRange();
        }


        LOG.info("start loading LSB ... ");
        LSB lsb = new LSB(recContext);
        lsb.readParaFile(lsbFilepath);


        LOG.info("para loading finished\n");
        float alpha = 0.5f;


        LOG.info("start constructing  uprofileEventGroupSet container ... ");
        List<UPEventPartition> uprofileEventGroupSet = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            uprofileEventGroupSet.add(new UPEventPartition());
        }

        LOG.info("start  UserProfileDataPartition ...  numofGroups=%s", numofGroups);
        eropti.UserProfileDataPartition(userHisEvents, numofGroups, uprofileEventGroupSet, lsb, alpha);
        recContext.setUpEventPartitionList(uprofileEventGroupSet);
        return this;
    }


    public void getUserprofileMap() {
        recContext.eventMigration.getUserProfileHashMap();
    }


    public List<UPEventPartition> getUserProfilePartition() {
        return recContext.getUpEventPartitionList();
    }

    public List<SocialEvent> getStaticClusterEvent() {
        return recContext.getEventclusters();
    }

    public static PreprocessingHelper build() {
        PreprocessingHelper instance = new PreprocessingHelper();
        instance.recContext = new RecContext();

        return instance;
    }


    public List<SocialEvent> IncomingEventSubsetIdentification(SocialEvent IESit, UPEventPartition UPEPar, float SimiThreshold, float alpha) {
        List<SocialEvent> ncomingEventSubset = new ArrayList<>();
        float upmax = eropti.ComputeUPmax(UPEPar, IESit, alpha);
        if (upmax >= SimiThreshold) {
            ncomingEventSubset.add(IESit);
        }
        return ncomingEventSubset;
    }


    public SocialEvent EventSimilarityJoin(UPEventPartition UserProfileEventPartion, SocialEvent IncomingEventSubsetEle, float SimiThreshold) {
        EventRecommendation eventRec = recContext.getRecommendation();

        int bucketNum = UserProfileEventPartion.getUProfileEventGroup().size();

        for (int i = 0; i < bucketNum; i++) {
            UPEventBucket upBucket = UserProfileEventPartion.getUProfileEventGroup().get(i);
            int seit = 0;
            while (seit < upBucket.getUProfileEventGroup().size()) {
                float simiV = eventRec.GetESim(IncomingEventSubsetEle, upBucket.getUProfileEventGroup().get(seit));

                if (simiV >= SimiThreshold) //recommend (*IESit) to the users whose profiles contain (*seit), update the RecUserSimi in (*IESit)
                {
                    eropti.UpdateRecUserSimi(IncomingEventSubsetEle, (upBucket.getUProfileEventGroup().get(seit)), simiV);
                }
                seit++;
            }

        }

        return IncomingEventSubsetEle;
    }
}
