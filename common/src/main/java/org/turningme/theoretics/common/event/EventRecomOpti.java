package org.turningme.theoretics.common.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.turningme.theoretics.common.RecContext;
import org.turningme.theoretics.common.beans.EUserFrePair;
import org.turningme.theoretics.common.beans.SocialMSG;
import org.turningme.theoretics.common.beans.UPEventBucket;
import org.turningme.theoretics.common.beans.UPEventPartition;
import org.turningme.theoretics.common.beans.UPInfluDistriEle;
import org.turningme.theoretics.common.beans.UserProfile;
import org.turningme.theoretics.common.beans.ValueRangePair;
import org.turningme.theoretics.common.lsb.LSB;

import static org.turningme.theoretics.common.Constants.MAXFLOAT;
import static org.turningme.theoretics.common.Constants.MAXINT;
import static org.turningme.theoretics.common.Constants.MINFLOAT;
import static org.turningme.theoretics.common.Constants.MININT;
import static org.turningme.theoretics.common.Constants.TFIDF_DIM;
import static org.turningme.theoretics.common.Constants.TUNUM;

/**
 * Created by jpliu on 2020/2/24.
 */
public class EventRecomOpti implements Serializable {
    RecContext recCxt;


    public EventRecomOpti() {
    }

    public EventRecomOpti(RecContext recCxt) {
        this.recCxt = recCxt;
        recCxt.setEventRecomOpti(this);
    }


    /**
     * event list to bucket then to processor-sticky group partition
     * @param UProfileEventSet
     * @param NumOfGroups
     * @param UProfileEventGroupSets
     * @param lsb
     * @param alpha
     */
    public void UserProfileDataPartition(List<SocialEvent> UProfileEventSet, int NumOfGroups,
            List<UPEventPartition> UProfileEventGroupSets, LSB lsb, float alpha) {
        MapEvent2Vec(UProfileEventSet,lsb,alpha);

        List<UPEventBucket> UProfileEventBuckets = new ArrayList<>();

        //historical event to bucket list
        ConflictEvents2Buckets(UProfileEventSet, UProfileEventBuckets,lsb);


        AllocateBucketsofGroups2Processors(UProfileEventBuckets, NumOfGroups, UProfileEventGroupSets, lsb);


        for(int i=0;i<UProfileEventGroupSets.size();i++)
            ProduceDataPartitionSummary(UProfileEventGroupSets.get(i));

    }


    void ProduceDataPartitionSummary(UPEventPartition  UProfileEventGroup) {
        Map<Integer,UserProfile> uProfileMap = recCxt.getEventMigration().UserProfileHashMap;


        float[] minCluster_ConceptTFIDFVec = new float[TFIDF_DIM];// = {MAXFLOAT};
        float[] maxCluster_ConceptTFIDFVec = new float[TFIDF_DIM] ; //= {MINFLOAT};

        float[] minUserInfluenceDistri = new float[TUNUM];  //= { MAXFLOAT };
        float[] maxUserInfluenceDistri = new float[TUNUM] ;//= { MINFLOAT };

        for (int i =0 ; i< 1 ; i++){
            minCluster_ConceptTFIDFVec[i] = MAXFLOAT;
            maxCluster_ConceptTFIDFVec[i] = MINFLOAT;
        }

        for (int i =0 ; i< 1 ; i++){
            minUserInfluenceDistri[i] = MAXFLOAT;
            minUserInfluenceDistri[i] = MINFLOAT;
        }

        int minUN = MAXINT;
        int maxUN = MININT;


        int  upegit =0 ;
        for (; upegit < UProfileEventGroup.getUProfileEventGroup().size() ; upegit++) {
            int seit = 0;

            UPEventBucket upBucket = UProfileEventGroup.getUProfileEventGroup().get(upegit);
            for (; seit < upBucket.getUProfileEventGroup().size() ; seit++) {
               SocialEvent socialEvent = upBucket.getUProfileEventGroup().get(seit);

                int[]  UserInfluencedFlag = new int[TUNUM] ;// default 0;
                for (int i = 0; i < TFIDF_DIM; i++)
                {
                    if (socialEvent.GetCluster_ConceptTFIDFVec()[i] > maxCluster_ConceptTFIDFVec[i])
                    maxCluster_ConceptTFIDFVec[i] = socialEvent.GetCluster_ConceptTFIDFVec()[i];
                    if (socialEvent.GetCluster_ConceptTFIDFVec()[i] < minCluster_ConceptTFIDFVec[i])
                    minCluster_ConceptTFIDFVec[i] = socialEvent.GetCluster_ConceptTFIDFVec()[i];
                }


                int EventUserNum = socialEvent.GetEventUserIDs().size();
                for (int i = 0; i < EventUserNum; i++) {
                    //////////////////////////////////


                     // all the code lack some defense
                     List<UPInfluDistriEle> upInfluDistriEle = uProfileMap.get(socialEvent.GetEventUserIDs().get(i).userid).UserInfluenceDistri;

                    for (UPInfluDistriEle uidp:upInfluDistriEle
                         ) {
                        if ((uidp).userInflu > maxUserInfluenceDistri[(uidp).userid])////for UserInfluenceDistri
                        maxUserInfluenceDistri[(uidp).userid] = (uidp).userInflu;
                        if ((uidp).userInflu< minUserInfluenceDistri[(uidp).userid])////for UserInfluenceDistri
                        minUserInfluenceDistri[(uidp).userid] = (uidp).userInflu;

                        if ((uidp).userInflu>0) //for UN
                        UserInfluencedFlag[(uidp).userid] = 1;
                    }
                }




                //for UN
                int influencedUserNum = 0;
                for (int i = 0; i < TUNUM; i++)	{
                    if (UserInfluencedFlag[i] !=0)
                        influencedUserNum++;
                }
                if (influencedUserNum > maxUN)
                    maxUN = influencedUserNum;
                if (influencedUserNum < minUN)
                    minUN = influencedUserNum;
                //////////////////////
            }

        }


        ValueRangePair[] TopicRangeVec = new ValueRangePair[TFIDF_DIM];
        for (int i = 0; i < TFIDF_DIM; i++) {
            TopicRangeVec[i] = new ValueRangePair();
            TopicRangeVec[i].minV = minCluster_ConceptTFIDFVec[i];
            TopicRangeVec[i].maxV = maxCluster_ConceptTFIDFVec[i];
        }

        UProfileEventGroup.setTopicRangeVector(TopicRangeVec);


        ValueRangePair[] influenceRangeVec = new ValueRangePair[TUNUM];
        for (int i = 0; i < TUNUM; i++) {
            influenceRangeVec[i] = new ValueRangePair();
            influenceRangeVec[i].minV = minUserInfluenceDistri[i];
            influenceRangeVec[i].maxV = maxUserInfluenceDistri[i];
        }
        UProfileEventGroup.setInfluenceRangeVector(influenceRangeVec);
        UProfileEventGroup.setMaxNumInfluencedUsers(maxUN);
        UProfileEventGroup.setMinNumInfluencedUsers(minUN);

    }
    /**
     *
     * @param UPEBuckets
     * @param NumOfGroups
     * @param UProfileEventGroupProcessors
     * @param lsb
     */
    void AllocateBucketsofGroups2Processors(List<UPEventBucket> UPEBuckets,	int NumOfGroups,
            List<UPEventPartition> UProfileEventGroupProcessors, LSB lsb) {

        //select the first bucket for the first processor
        int allocatedProcessorNum = 0;
        int upebit, upebitInner, FirstProBit;
        if (!UPEBuckets.isEmpty()) {
            upebit = 0;
            float maxdist = 0;
            FirstProBit = upebit;

            while (upebit < UPEBuckets.size()) {
                upebitInner = 0;
                float curdist = 0;
                while (upebitInner < UPEBuckets.size()) {
                    curdist += SocialEventOperation.l2_dist_int((UPEBuckets.get(upebit)).getUProfileEventGroup().get(0).EventHashV, (UPEBuckets.get(upebitInner)).getUProfileEventGroup().get(0).EventHashV, lsb.m);
                    upebitInner++;
                }
                if (curdist > maxdist)
                {
                    FirstProBit = upebit;
                    maxdist = curdist;
                }

                upebit++;
            }


            UProfileEventGroupProcessors.get(0).setUProfileEventGroup(UPEBuckets.get(FirstProBit));  //allocate the first bucket for the first processor
            UPEBuckets.remove(FirstProBit);  //remove the bucket // TODO: 2020/2/25  test this
            allocatedProcessorNum++;
        }


        //select the first bucket for each of the rest of processors
        for (int i = allocatedProcessorNum; i < NumOfGroups; i++) {
            //find the first bucket for processor i
            upebit = 0;
            float maxdist = 0;
            FirstProBit = upebit;


            while (upebit < UPEBuckets.size()) {
                float curdist = 0;
                for (int j = 0; j < allocatedProcessorNum; j++) {
                    curdist += SocialEventOperation.l2_dist_int(UPEBuckets.get(upebit).getUProfileEventGroup().get(0).EventHashV,
                            UProfileEventGroupProcessors.get(j).getUProfileEventGroup().get(0).getUProfileEventGroup().get(0).EventHashV, lsb.m);
                }
                if (curdist > maxdist) {
                    maxdist = curdist;
                    FirstProBit = upebit;
                }

                upebit++;
            }

            UProfileEventGroupProcessors.get(i).setUProfileEventGroup(UPEBuckets.get(FirstProBit));  //allocate the first bucket for the ith processor
            UPEBuckets.remove(FirstProBit);  //remove the bucket
            allocatedProcessorNum++;
        }


        //allocate the rest of buckets to NumOfGroups processors
        while (!UPEBuckets.isEmpty()) {
            int proToAllocate = findSmallestGroup(UProfileEventGroupProcessors, NumOfGroups);
            upebit = 0;
            float mindist=MAXFLOAT;
            FirstProBit = upebit;


            while (upebit < UPEBuckets.size())
            {

                float curdist= SocialEventOperation.l2_dist_int(UPEBuckets.get(upebit).getUProfileEventGroup().get(0).EventHashV,
                UProfileEventGroupProcessors.get(proToAllocate).getUProfileEventGroup().get(0).getUProfileEventGroup().get(0).EventHashV, lsb.m);
                if (curdist < mindist) {
                    mindist = curdist;
                    FirstProBit = upebit;
                }


                upebit++;
            }


            //the corresponding cpp condition here would never be invoked ???
            //if FirstProBit is replaced by upebit, this would be happend
            if (FirstProBit != UPEBuckets.size() ) {
                UProfileEventGroupProcessors.get(proToAllocate).setUProfileEventGroup(UPEBuckets.get(FirstProBit));  //allocate the rest bucket for the processor proToAllocate
                UPEBuckets.remove(FirstProBit);  //remove the bucket
            }


        }


    }


    /**
     *
     * @param UProfileEventGroupProcessors
     * @param NumOfGroups
     * @return
     */
    public int findSmallestGroup(List<UPEventPartition> UProfileEventGroupProcessors, int NumOfGroups) {
        int processorNumber = 0;
        int SmallestclusterNum = MAXINT;
        for (int i = 0; i < NumOfGroups; i++) {
            int CurClusterNum = 0;
            for (int j = 0; j < UProfileEventGroupProcessors.get(i).getUProfileEventGroup().size(); j++) {
                CurClusterNum += UProfileEventGroupProcessors.get(i).getUProfileEventGroup().get(j).getUProfileEventGroup().size();
            }
            if (CurClusterNum < SmallestclusterNum) {
                processorNumber = i;

                SmallestclusterNum = CurClusterNum;
            }
        }

        //why not  return the smallest number as the function definition tells .
        return processorNumber;
    }



        /**
         *
         * @param UProfileEventSet
         * @param UPEventBuckets
         * @param lsb
         *
         * return UPEventBuckets as result
         */
   public void ConflictEvents2Buckets(List<SocialEvent> UProfileEventSet, List<UPEventBucket>UPEventBuckets, LSB lsb)
    {


        for (SocialEvent upesit:UProfileEventSet
             ) {

            int i = 0;
            for (i =0 ; i<UPEventBuckets.size() ; i++){
                UPEventBucket upebit = UPEventBuckets.get(i);
                float curdist =  SocialEventOperation.l2_dist_int( upebit.getUProfileEventGroup().get(0).EventHashV, upesit.EventHashV, lsb.m);
                if (curdist <= 0.00001) //close to 0, find the conflict bucket, put the event into the bucket
                {
                    upebit.setUProfileEventGroup(upesit);
                    break;
                }
            }


            /////////
            //overflow the array
            // that is the above code block never been executed
            if (UPEventBuckets.size() == i){
                UPEventBucket curEPar = new UPEventBucket();
                curEPar.setUProfileEventGroup(upesit);
                UPEventBuckets.add(curEPar);
            }


        }

    }






    // simple method
    //test on date 20200224 almost the same _g var . precise
    //
    public void MapEvent2Vec(List<SocialEvent> UProfileEventSet, LSB lsb,float alpha){
         float[]  _g = new float[lsb.m];


         Map<Integer,UserProfile> userProfileMap = recCxt.getEventMigration().UserProfileHashMap;

         for (SocialEvent eit:UProfileEventSet
              ) {
             eit.EventHashV =  new int[lsb.m];
             //get user influence vector variant
             float[] EventUserInfluenceDistri = new float[TUNUM];//and init it to 0.0f
             List<EUserFrePair> eUserFre = eit.GetEventUserIDs();
             int EventUserNum = eUserFre.size();

             for (EUserFrePair oneUser:eUserFre
                  ) {
                 int curUid = oneUser.userid;

                 for (UPInfluDistriEle upidit:userProfileMap.get(curUid).UserInfluenceDistri
                      ) {
                     EventUserInfluenceDistri[upidit.userid] += upidit.userInflu;

                 }
             }


             for (int j = 0; j < TUNUM; j++) {
                 EventUserInfluenceDistri[j] /= EventUserNum;
             }

             getEventHashVector(0, eit.GetCluster_ConceptTFIDFVec(), EventUserInfluenceDistri,alpha,_g,lsb);

             //_g input ,eit.EventHashV output
             lsb.getZ(_g, eit.EventHashV);

         }

     }



   public   void getEventHashVector(int _tableID, float[]_key, float[]  influencekey, float alpha, float[] _g, LSB lsb)
    {
        int i;

        int m1 = (int)(lsb.m * alpha);
        int m2 = lsb.m - m1;

        for (i = 0; i < m1; i++)
        {
            _g[i] = lsb.get1HashV(_tableID, i, _key);
        }

        for(i=m1;i<lsb.m;i++)
        {
            _g[i] = lsb.get1HashV(_tableID, i, influencekey);
        }
    }


    /**
     *
     * @param IncomingEventSet
     * @param IncomingEventSubset
     * @param UPEPar
     * @param SimiThreshold
     * @param alpha
     *
     * return IncomingEventSubset as an middle result
     */
    public void IncomingEventSubsetIdentification(List<SocialEvent> IncomingEventSet, List<SocialEvent> IncomingEventSubset,
            UPEventPartition UPEPar, float SimiThreshold, float alpha)
    {


        for (SocialEvent IESit:IncomingEventSet
             ) {
            float upmax = ComputeUPmax(UPEPar, IESit,alpha);
            if (upmax >= SimiThreshold)
            {
                IncomingEventSubset.add(IESit);
            }
        }

    }


    public float ComputeUPmax(UPEventPartition UserProfileEventPartition, SocialEvent  IncomingEvent, float alpha)
    {
        float UPmax = 0;

        float UPmaxT = ComputeUPmaxT(UserProfileEventPartition, IncomingEvent)*alpha;
        float UPmaxI = ComputeProb_EvEn(UserProfileEventPartition, IncomingEvent) * (1 - alpha);

        UPmax = UPmaxT + UPmaxI;

        return UPmax;
    }


    float ComputeUPmaxT(UPEventPartition UserProfileEventPartition, SocialEvent  IncomingEvent)
    {
        float UPmaxT = 0;

        SocialMSG smg = new SocialMSG();
        float[] virtualConceptVec = new float[TFIDF_DIM]; // default 0 = { 0 };
        ValueRangePair[] upep = UserProfileEventPartition.getTopicRangeVector();
        float[] incomingConceptVec = IncomingEvent.GetCluster_ConceptTFIDFVec();

        for (int i = 0; i < TFIDF_DIM-1; i++)
            GetTBoundValueofDim(upep,i, incomingConceptVec,i, virtualConceptVec,i);

        UPmaxT=smg.GetConceptVectorSimilarity(virtualConceptVec,IncomingEvent.GetCluster_ConceptTFIDFVec());

        return UPmaxT;
    }



    float GetTBoundValueofDim(ValueRangePair UserParVPRect[], float EventVRP[], float VirtualEventTopic[])
    {
        float BoundValue = 0;  //0 not to be filtered

        float minRectAngleTan = UserParVPRect[1].minV / UserParVPRect[0].maxV;  //here dim i to 0, dim j to 1
        float maxRectAngleTan = UserParVPRect[1].maxV / UserParVPRect[0].minV;

        float EventVRPAngleTan = EventVRP[1] / EventVRP[0];

        if (EventVRPAngleTan > maxRectAngleTan)  //for the case like T3 in the figure 6 of the paper
        {
            VirtualEventTopic[0] = UserParVPRect[0].minV;
            VirtualEventTopic[1] = UserParVPRect[1].maxV;
            BoundValue = 1;
        }
        else if (EventVRPAngleTan < minRectAngleTan) //for the case like T2 in the figure 6 of the paper
        {
            VirtualEventTopic[0] = UserParVPRect[0].maxV;
            VirtualEventTopic[1] = UserParVPRect[1].minV;
            BoundValue = 1;
        }
        else //for the case like T1 in the figure 6 of the paper
        {
            VirtualEventTopic[0] = EventVRP[0];
            VirtualEventTopic[1] = EventVRP[1];
        }

        return BoundValue;
    }


    float GetTBoundValueofDim(ValueRangePair UserParVPRect[] , int UserParVPRectPos, float EventVRP[] , int EventVRPPos, float VirtualEventTopic[], int VirtualEventTopicPos)
    {
        float BoundValue = 0;  //0 not to be filtered

        float minRectAngleTan = UserParVPRect[1+UserParVPRectPos].minV / UserParVPRect[0+UserParVPRectPos].maxV;  //here dim i to 0, dim j to 1
        float maxRectAngleTan = UserParVPRect[1 + UserParVPRectPos].maxV / UserParVPRect[0 + UserParVPRectPos].minV;

        float EventVRPAngleTan = EventVRP[1 + EventVRPPos] / EventVRP[0 + EventVRPPos];

        if (EventVRPAngleTan > maxRectAngleTan)  //for the case like T3 in the figure 6 of the paper
        {
            VirtualEventTopic[0 + VirtualEventTopicPos] = UserParVPRect[0 + UserParVPRectPos].minV;
            VirtualEventTopic[1 + VirtualEventTopicPos] = UserParVPRect[1 + UserParVPRectPos].maxV;
            BoundValue = 1;
        }
        else if (EventVRPAngleTan < minRectAngleTan) //for the case like T2 in the figure 6 of the paper
        {
            VirtualEventTopic[0 + VirtualEventTopicPos] = UserParVPRect[0 + UserParVPRectPos].maxV;
            VirtualEventTopic[1 + VirtualEventTopicPos] = UserParVPRect[1 + UserParVPRectPos].minV;
            BoundValue = 1;
        }
        else //for the case like T1 in the figure 6 of the paper
        {
            VirtualEventTopic[0 + VirtualEventTopicPos] = EventVRP[0 + EventVRPPos];
            VirtualEventTopic[1 + VirtualEventTopicPos] = EventVRP[1 + EventVRPPos];
        }

        return BoundValue;
    }


    float ComputeProb_EvEn(UPEventPartition UserProfileEventPartition, SocialEvent  IncomingEvent)
    {
        float Prob_EvEn = 0;
        SocialEvent VirtualUPPevent = new SocialEvent();
        VirtualUPPevent.EventReset();

        GetDomiUserVirEvent(UserProfileEventPartition, IncomingEvent, VirtualUPPevent);

        EventMigration eventmig = recCxt.getEventMigration();
        Prob_EvEn = eventmig.EventMigrationProb(VirtualUPPevent, IncomingEvent);

        return Prob_EvEn;
    }



    void GetDomiUserVirEvent(UPEventPartition UserProfileEventPartition, SocialEvent IncomingEvent, SocialEvent  VirtualUPPevent)
    {
        //estiate user influential between UserProfileEventPartition and IncomingEvent
        //Get all the users in UserProfileEventPartition
        List<Integer> userlist = new ArrayList<>();
        GetUserInEventPartition(UserProfileEventPartition, userlist);

        //Select all the users appearing in UserProfileEventPartition and influencing users in IncomingEvent , and calculate the average influence
        float[] influentials = new float[TUNUM];
        for (int i = 0; i < TUNUM; i++)
            influentials[i] = (0f);
        RemoveNonInfluentialUsers(userlist, IncomingEvent.GetEventUserIDs(),influentials);

        //rank the selected users in UserProfileEventPartition based on the average influentials they can generate to IncomingEvent.
        RankInfluentialUsers(userlist, influentials);

        //select the top un_{min} users from UserProfileEventPartition as the dominant ones
        int ulsize = userlist.size();
        if(ulsize> UserProfileEventPartition.getMinNumInfluencedUsers())

            //remove operation include first one    , not the last one
            for (int i=0 + UserProfileEventPartition.getMinNumInfluencedUsers() ; i < 0 + ulsize-1; i++){
                userlist.remove(i);
            }


        //form VirtualUPPevent
        VirtualUPPevent.SetEventUserIDs_Vec(userlist);
    }


    void GetUserInEventPartition(UPEventPartition UserProfileEventPartition, List<Integer> userlist)
    {


        for (UPEventBucket upebit:UserProfileEventPartition.getUProfileEventGroup()
             ) {
            for (SocialEvent seit: (upebit).getUProfileEventGroup()
                 ) {
                for (EUserFrePair uit: (seit).GetEventUserIDs()
                     ) {


                    boolean flag = false;
                    for (Integer ulit:userlist
                         ) {
                        if ((uit).userid == (ulit)) {
                            flag = true;
                            break;

                        }

                    }


                    if (!flag) {
                        userlist.add((uit).userid);
                    }

                    
                }
                
                
                
            }
            
        }

    }



    void RemoveNonInfluentialUsers(List<Integer> userlist, List<EUserFrePair> eventUserIdsFre,  float[] influentials)
    {

        Map<Integer,UserProfile> UserProfileHashMap = recCxt.getEventMigration().UserProfileHashMap;
        int ulit=0, eraselit = 0;
        while(ulit < userlist.size()){
            //check if (*ulit) influence IncomingEvent
            int userNumBeInfluenced = 0; //count the number of user being influenced by (*ulit)
            int euifit = 0;

            while (euifit < eventUserIdsFre.size()){
                EUserFrePair euifitEle = eventUserIdsFre.get(euifit);
                int upDit = 0;
                while (upDit < UserProfileHashMap.get(ulit).UserInfluenceDistri.size()) {
                    UPInfluDistriEle upDitEle =  UserProfileHashMap.get(ulit).UserInfluenceDistri.get(upDit);
                    if (upDitEle.userid == ((euifitEle).userid)) {
                        if ((upDitEle).userInflu > 0) {//has influence
                            userNumBeInfluenced++;
                            influentials[ulit] += (upDitEle).userInflu;
                        }
                    }
                    upDit++;
                }
                euifit++;

            }


            ////
            if (! (userNumBeInfluenced !=0 ))//no influence
            {
                eraselit = ulit;
                if (ulit ==0)
                {
                    userlist.remove(eraselit);
                    ulit=0;
                }
                else {
                    ulit--;
                    userlist.remove(eraselit);
                    ulit++;
                }
            } else  //has influence
            {
                influentials[ulit] /= userNumBeInfluenced;
                ulit++;
            }

        }
    }



    // list could be array
    void RankInfluentialUsers(List<Integer> userlist, float[] influentials)
    {
        int userlistsize = userlist.size();
        for (int i = 0; i < userlistsize; i++)
        {
            for (int j = i+1; j < userlistsize; j++)
            {
                if (userlist.get(j) > userlist.get(i))
                {
                    //swap
                    int temp= userlist.get(i);
                    userlist.set(i,userlist.get(j)) ;
                    userlist.set(j,temp);
                }
            }
        }
    }


    public void EventSimilarityJoin(UPEventPartition UserProfileEventPartion, List<SocialEvent> IncomingEventSubset, float SimiThreshold)
    {
        EventRecommendation eventRec = recCxt.getRecommendation();

        int IESit = 0;
        while (IESit < IncomingEventSubset.size()){
            int bucketNum = UserProfileEventPartion.getUProfileEventGroup().size();

            for (int i = 0; i < bucketNum; i++){
                UPEventBucket upBucket = UserProfileEventPartion.getUProfileEventGroup().get(i);
                int  seit  = 0;
                while(seit < upBucket.getUProfileEventGroup().size()){
                    float simiV = eventRec.GetESim(IncomingEventSubset.get(IESit), upBucket.getUProfileEventGroup().get(seit));

                    if (simiV >= SimiThreshold) //recommend (*IESit) to the users whose profiles contain (*seit), update the RecUserSimi in (*IESit)
                    {
                        UpdateRecUserSimi(IncomingEventSubset.get(IESit),( upBucket.getUProfileEventGroup().get(seit)),simiV);
                    }
                    seit++;
                }

            }
                /////////
            IESit ++;
        }
    }


    public void UpdateRecUserSimi(SocialEvent  IncomingEvent, SocialEvent  UserProfileEvent, float simiV)
    {
        int NU_seit = UserProfileEvent.userlist.size();
        for (int j = 0; j < NU_seit; j++)
        {
            //check if (*seit).userlist[j] is in (*IESit).RecUserSimi
            int NRU_IESit = IncomingEvent.RecUserSimi.size();
            for (int k = 0; k < NRU_IESit; k++)
            {
                if (UserProfileEvent.userlist.get(j) == IncomingEvent.RecUserSimi.get(k).userid)
                {
                    if (simiV > IncomingEvent.RecUserSimi.get(k).simi)
                        IncomingEvent.RecUserSimi.get(k).simi = simiV;
                    break;
                }
            }
        }
    }
}
