package Xi_recommendation;

//import scala.scalanative.runtime.Array;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


import static Xi_recommendation.parameters.TFIDF_DIM;
import static Xi_recommendation.parameters.TOPK;

public class EventRecomOpti {
//    public void UserProfileDataPartition(ArrayList<SubEvent> UProfileEventSet, int NumOfGroups,
//                                         ArrayList<UPEventPartition> UProfileEventGroupSets, LSB lsb, SocialEventOperation seoper) {
//        MapEvent2Vec(UProfileEventSet, lsb);
//
//        ArrayList<UPEventBucket> UProfileEventBuckets;
//
//        ConflictEvents2Buckets(UProfileEventSet, UProfileEventBuckets, lsb, seoper);
//
//        AllocateBucketsofGroups2Processors(UProfileEventBuckets, NumOfGroups, UProfileEventGroupSets, lsb, seoper);
//
//        for (int i = 0; i < UProfileEventGroupSets.size(); i++){
//            ProduceDataPartitionSummary(UProfileEventGroupSets[i]);
//        }
//    }

    public static void IncomingEventSubsetIdentification(ArrayList<SubEvent> IncomingEventSet,
            ArrayList<SubEvent> IncomingEventSubset,
            UPEventPartition UPEPar,
            float SimiThreshold, float alpha,
            ArrayList<UserProfile> UserProfileHashMap) {
        for (SubEvent IESit : IncomingEventSet) {
            float upmax = ComputeUPmax(UPEPar, (IESit), alpha, UserProfileHashMap);
            if (upmax >= SimiThreshold) {
                IncomingEventSubset.add(IESit);
            }
        }
    }

    public static float ComputeUPmax(UPEventPartition UserProfileEventPartition, SubEvent IncomingEvent, float alpha,
            ArrayList<UserProfile> UserProfileHashMap) {
        float UPmax = 0;
        float UPmaxT = ComputeUPmaxT(UserProfileEventPartition, IncomingEvent) * parameters.omeg1 * (1 - alpha);
        float UPmaxI = ComputeProb_EvEn(UserProfileEventPartition, IncomingEvent, UserProfileHashMap) * alpha;

        SubEvent virtualEvent;
        ValueRangePair vrp = UserProfileEventPartition.getTimeRangePair();
        ValueRangePair[] vrpLocation = UserProfileEventPartition.getSpaceRangePair();

        // float UPmaxLocation = ComputeUPmaxLocation(vrpLocation,IncomingEvent)*omeg2 * (1 - alpha);

        float UPmaxTime = ComputeUPmaxTime(vrp, IncomingEvent) * (1 - parameters.omeg1 - parameters.omeg2) * (1 - alpha);
//        UPmax = UPmaxT + UPmaxI + UPmaxLocation+ UPmaxTime;
        UPmax = UPmaxT + UPmaxI + UPmaxTime;

        return UPmax;
    }


    public static float ComputeUPmaxTime(ValueRangePair vrp, SubEvent IncomingEvent) {
        float UPmaxTime = 0;

        SubEvent virtualEvent = new SubEvent();
        TimeRange tr = new TimeRange();
        tr.TimeStampCentre = vrp.maxV;
        tr.range = parameters.TIMERADIUST;
        virtualEvent.setTimeRange(tr);

        UPmaxTime = virtualEvent.GetTimeSimilarity(IncomingEvent);

        return UPmaxTime;
    }

    public static float ComputeUPmaxT(UPEventPartition UserProfileEventPartition, SubEvent IncomingEvent) {
        float UPmaxT = 0;

        SocialMSG smg = new SocialMSG();
//        ArrayList<Float> virtualConceptVec=new ArrayList<>();
        float[] virtualConceptVec = new float[TFIDF_DIM];

        ValueRangePair[] upep = UserProfileEventPartition.getTopicRangeVector();
//        ArrayList<Float> incomingConceptVec = IncomingEvent.GetCluster_ConceptTFIDFVec();
        float[] incomingConceptVec = IncomingEvent.GetCluster_ConceptTFIDFVec();

        for (int i = 0; i < TFIDF_DIM - 1; i++)
            GetTBoundValueofDim(upep, incomingConceptVec, virtualConceptVec);

        UPmaxT = smg.GetConceptVectorSimilarity(virtualConceptVec, IncomingEvent.GetCluster_ConceptTFIDFVec());

        return UPmaxT;
    }

    public static float GetTBoundValueofDim(ValueRangePair[] UserParVPRect, float[] EventVRP, float[] VirtualEventTopic) {
        float BoundValue = 0;  //0 not to be filtered

        float minRectAngleTan = UserParVPRect[1].minV / UserParVPRect[0].maxV;  //here dim i to 0, dim j to 1
        float maxRectAngleTan = UserParVPRect[1].maxV / UserParVPRect[0].minV;

        float EventVRPAngleTan = EventVRP[1] / EventVRP[0];

        if (EventVRPAngleTan > maxRectAngleTan)  //for the case like T3 in the figure 6 of the paper
        {
            VirtualEventTopic[0] = UserParVPRect[0].minV;
            VirtualEventTopic[1] = UserParVPRect[1].maxV;
            BoundValue = 1;
        } else if (EventVRPAngleTan < minRectAngleTan) //for the case like T2 in the figure 6 of the paper
        {
            VirtualEventTopic[0] = UserParVPRect[0].maxV;
            VirtualEventTopic[1] = UserParVPRect[1].minV;
            BoundValue = 1;
        } else //for the case like T1 in the figure 6 of the paper
        {
            VirtualEventTopic[0] = EventVRP[0];
            VirtualEventTopic[1] = EventVRP[1];
        }

        return BoundValue;
    }

    public static void EventSimilarityJoin(UPEventPartition UserProfileEventPartion,
            ArrayList<SubEvent> IncomingEventSubset,
            float SimiThreshold, EventRecommendation eventRec,
            ArrayList<UserProfile> UserProfileHashMap) {

        for (SubEvent IESit : IncomingEventSubset) {
            EventSimilarityJoin(UserProfileEventPartion, IESit, SimiThreshold, eventRec, UserProfileHashMap);

        }
    }


    public static void EventSimilarityJoin(UPEventPartition UserProfileEventPartion,
            SubEvent IESit,
            float SimiThreshold, EventRecommendation eventRec,
            ArrayList<UserProfile> UserProfileHashMap) {
        int bucketNum = UserProfileEventPartion.getUProfileEventGroup().size();
        for (int i = 0; i < bucketNum; i++) //for UserProfileEventPartition
        {
            for (SubEvent seit : UserProfileEventPartion.getUProfileEventGroup().get(i).getUProfileEventGroup()) {
                float simiV = eventRec.GetESim((IESit), (seit), UserProfileHashMap);
                if (simiV >= SimiThreshold) //recommend (*IESit) to the users whose profiles contain (*seit), update the RecUserSimi in (*IESit)
                {
                    UpdateRecUserSimi((IESit), (seit), simiV);
                }
            }
        }
    }


    public static float ComputeProb_EvEn(UPEventPartition UserProfileEventPartition, SubEvent IncomingEvent,
            ArrayList<UserProfile> UserProfileHashMap) {
        float Prob_EvEn = 0;
        SubEvent VirtualUPPevent = new SubEvent();

        GetDomiUserVirEvent(UserProfileEventPartition, IncomingEvent, VirtualUPPevent, UserProfileHashMap);

        EventMigration eventmig = new EventMigration();
        Prob_EvEn = EventMigration.EventMigrationProb(VirtualUPPevent, IncomingEvent, UserProfileHashMap);

        return Prob_EvEn;
    }

    public static void GetDomiUserVirEvent(UPEventPartition UserProfileEventPartition,
            SubEvent IncomingEvent, SubEvent VirtualUPPevent,
            ArrayList<UserProfile> UserProfileHashMap) {
        //estiate user influential between UserProfileEventPartition and IncomingEvent
        //Get all the users in UserProfileEventPartition
        ArrayList<Integer> userlist = new ArrayList<>();
        GetUserInEventPartition(UserProfileEventPartition, userlist);

        //Select all the users appearing in UserProfileEventPartition and influencing users in IncomingEvent , and calculate the average influence
        ArrayList<Float> influentials = new ArrayList<>();
        for (int i = 0; i < parameters.TUNUM; i++) {
            influentials.add((float) 0);
        }
        RemoveNonInfluentialUsers(userlist, IncomingEvent.GetEventUserIDs(), influentials, UserProfileHashMap);

        //rank the selected users in UserProfileEventPartition based on the average influentials they can generate to IncomingEvent.
        RankInfluentialUsers(userlist, influentials);

        //select the top un_{min} users from UserProfileEventPartition as the dominant ones
        int ulsize = userlist.size();
        if (ulsize > UserProfileEventPartition.getMinNumInfluencedUsers()) {
            userlist.remove(userlist.get(0) + UserProfileEventPartition.getMinNumInfluencedUsers() + userlist.get(0) + ulsize - 1);
        }

        //form VirtualUPPevent
        VirtualUPPevent.SetEventUserIDs_Vec(userlist);

    }

    public int SetEventUserIDs_Vec(ArrayList<Integer> userlist, SubEvent IncomingEvent) {
        int usernumber = userlist.size();
        EUserFrePair upair = new EUserFrePair();
        upair.frequency = 1;
        for (int i = 0; i < usernumber; i++) {
            upair.userid = userlist.get(i);
            IncomingEvent.eventUserIdsFre.add(upair);
        }
        return usernumber;
    }


    public static void GetUserInEventPartition(UPEventPartition UserProfileEventPartition, ArrayList<Integer> userlist) {
        for (UPEventBucket upebit : UserProfileEventPartition.getUProfileEventGroup()) {
            for (SubEvent seit : upebit.getUProfileEventGroup()) {
                for (EUserFrePair uit : seit.GetEventUserIDs()) {
                    int flag = 0;
                    for (int ulit : userlist) {
                        if (uit.userid == ulit) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        userlist.add(uit.userid);
                    }
                }
            }
        }
    }

    public static void RankInfluentialUsers(ArrayList<Integer> userlist, ArrayList<Float> influentials) {
        int userlistsize = userlist.size();
        for (int i = 0; i < userlistsize; i++) {
            for (int j = i + 1; j < userlistsize; j++) {
                if (userlist.get(j) > userlist.get(i)) {
                    //swap
                    int temp = userlist.get(i);
                    userlist.set(i, userlist.get(j));
                    userlist.set(j, temp);
                }
            }
        }
    }

    public static void RemoveNonInfluentialUsers(List<Integer> userlist,
            List<EUserFrePair> eventUserIdsFre, List<Float> influentials,
            List<UserProfile> UserProfileHashMap) {
//        std::vector<int>::iterator ulit, eraselit;

        for (int ulit = 0; ulit < userlist.size(); ulit++) {
            int userNumBeInfluenced = 0;
            for (EUserFrePair euifit : eventUserIdsFre) {
                for (UPInfluDistriEle upDit : UserProfileHashMap.get(ulit).UserInfluenceDistri) {
                    if (upDit.userid == euifit.userid) {
                        userNumBeInfluenced++;
                        float temp = influentials.get(ulit) + upDit.userInflu;
                        influentials.set(ulit, temp);
                    }
                }
            }
            if (userNumBeInfluenced != 0) {
                int eraselit = ulit;
                if (ulit == userlist.get(0)) {
                    userlist.remove(eraselit);
                    ulit = userlist.get(0);
                } else {
                    ulit--;
                    userlist.remove(eraselit);
                    ulit = userlist.get(0);
                }
            } else {
                float temp = influentials.get(ulit) / userNumBeInfluenced;
                influentials.set(ulit, temp);
                ulit++;
            }
        }
    }


    public static void UpdateRecUserSimi(SubEvent IncomingEvent, SubEvent UserProfileEvent, float simiV) {
        //int NU_seit = UserProfileEvent.userlist.size();
        int NU_seit = UserProfileEvent.GetEventUserIDs().size();
        for (int j = 0; j < NU_seit; j++) {
            //check if (*seit).userlist[j] is in (*IESit).RecUserSimi
            int NRU_IESit = IncomingEvent.RecUserSimi.size();
            int k = 0;
            for (k = 0; k < NRU_IESit; k++) {
                if (UserProfileEvent.GetEventUserIDs().get(j).userid == IncomingEvent.RecUserSimi.get(k).userid) {
                    if (simiV > IncomingEvent.RecUserSimi.get(k).simi)
                        IncomingEvent.RecUserSimi.get(k).simi = simiV;
                    break;
                }
            }
            if (k < NRU_IESit || k == 0) //not in the RecUserSimi list, insert it
            {
                EventUserSimi eusTuple = new EventUserSimi();
                eusTuple.userid = UserProfileEvent.GetEventUserIDs().get(j).userid;
                eusTuple.simi = simiV;

                int RUSsize = IncomingEvent.RecUserSimi.size();
                for (int i = 0; i < IncomingEvent.RecUserSimi.size(); i++) {
                    EventUserSimi rusit = IncomingEvent.RecUserSimi.get(i);
                    if (rusit.simi > simiV) {
                    } else {
                        IncomingEvent.RecUserSimi.set(i, eusTuple);
                    }

                    if (RUSsize == IncomingEvent.RecUserSimi.size()) {  //have not been inserted yet, then push back
                        if (IncomingEvent.RecUserSimi.size() < TOPK)
                            IncomingEvent.RecUserSimi.add(eusTuple);
                    }
                    if (IncomingEvent.RecUserSimi.size() > TOPK) {
                        i = IncomingEvent.RecUserSimi.size() - 1;
                        i--;
                        IncomingEvent.RecUserSimi.remove(i);
                    }
                }
            }
        }
    }

}
