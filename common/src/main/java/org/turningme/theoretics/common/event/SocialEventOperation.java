package org.turningme.theoretics.common.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.turningme.theoretics.common.RecContext;
import org.turningme.theoretics.common.beans.SocialMSG;

import org.turningme.theoretics.common.lsb.LSB;


import static org.turningme.theoretics.common.Constants.ELIPSE;
import static org.turningme.theoretics.common.Constants.MAXFLOAT;


/**
 * Created by jpliu on 2020/2/23.
 */
public class SocialEventOperation implements Event {

    RecContext recContext;

    public SocialEventOperation() {
    }

    public SocialEventOperation(RecContext recContext) {
        this.recContext = recContext;
    }

    public void PartitionGroupsByTimeSpace(List<SocialMSG> MSGSet, List<MSGSET> outputGroups) {
        Map<Integer,MSGSET> outputGroupsMap = new LinkedHashMap<>();

        float[] minmaxTvalue = new float[2];
        float[] minmaxLatValue = new float[2];
        float[] minmaxLongiValue = new float[2];

        System.out.println("----- PartitionGroupsByTimeSpace\n");

        char[] arrayLat = new char[5], arrayLongi = new char[5];

        //get min-max time values
        minMaxTime(MSGSet, minmaxTvalue);
        //get min-max space values
        minMaxSpace(MSGSet, minmaxLatValue, minmaxLongiValue);

        //divide latitute value into 4 segments, divide longitute into 4 segments
        int segL = (int)Math.ceil((minmaxLatValue[1] - minmaxLatValue[0]) / 4);
        int segLon = (int)Math.ceil((minmaxLongiValue[1] - minmaxLongiValue[0]) / 4);


        for (SocialMSG smit :MSGSet){
            //commpute the index of a message
            int Lindex = 0;
            int LongIndex = 0;
            int Tindex=0;

            if(segL>0)
                //Lindex = ((int)(*smit).getSpaceRange().lat - (int)minmaxLatValue[0]) % segL;
                Lindex = ((int)(smit).getSpaceRange().lat - (int)minmaxLatValue[0])/segL;
            if(segLon != 0)
                //LongIndex = ((int)(*smit).getSpaceRange().longi - (int)minmaxLongiValue[0]) % segLon;
                LongIndex = ((int)(smit).getSpaceRange().longi - (int)minmaxLongiValue[0]) / segLon;

            Tindex = (int)Math.floor(((smit).getTimeRange().TimeStampCentre - minmaxTvalue[0]) / 60);



            __bit(Lindex, arrayLat, 2);
            __bit(LongIndex, arrayLongi, 2);


            int index = Tindex * 16;
            for (int i = 0; i < 2; i++) {
                index += arrayLat[i] * Math.pow(2, 4 - i);
            }
            for (int i = 0; i < 2; i++) {
                index += arrayLongi[i] * Math.pow(2, 2 - i);
            }

            //added by emily on feb. 8
//           if (outputGroups.size() < index + 1)
//               outputGroups.resize(index + 1);
            //////////////////////

            outputGroupsMap.putIfAbsent(index,new MSGSET(new ArrayList<>()));
            outputGroupsMap.get(index).getSocialMSGs().add(smit);
        }

        Set<Map.Entry<Integer,MSGSET>> entrys = outputGroupsMap.entrySet();
        for (Map.Entry<Integer,MSGSET> en:entrys){
            outputGroups.add(en.getValue());
        }
//        outputGroups.addAll(outputGroupsMap.values());

        //set time value segment length to 60 minutes
    }

    void minMaxTime(List<SocialMSG> MSGSet, float minmax[]) {
        minmax[0] = MAXFLOAT;
        minmax[1] = 1.0f;

        for (SocialMSG smit:MSGSet){
            if (smit.getTimeRange().TimeStampCentre < minmax[0])
                minmax[0] = smit.getTimeRange().TimeStampCentre;
            if ((smit.getTimeRange().TimeStampCentre > minmax[1]))
                minmax[1] = smit.getTimeRange().TimeStampCentre;

        }
    }

    void minMaxSpace(List<SocialMSG> MSGSet, float minmaxLat[], float minmaxLongi[]) {
        minmaxLat[0] = MAXFLOAT;
        minmaxLat[1] = 1.0f;

        minmaxLongi[0] = MAXFLOAT;
        minmaxLongi[1] = 1.0f;

        for (SocialMSG smit : MSGSet){
            if (smit.getSpaceRange().lat< minmaxLat[0])
                minmaxLat[0] = (smit).getSpaceRange().lat;
            if (smit.getSpaceRange().lat > minmaxLat[1])
                minmaxLat[1] = (smit).getSpaceRange().lat;

            if (smit.getSpaceRange().longi < minmaxLongi[0])
                minmaxLongi[0] = (smit).getSpaceRange().longi;
            if (smit.getSpaceRange().longi > minmaxLongi[1])
                minmaxLongi[1] = (smit).getSpaceRange().longi;

        }


    }


    //decimal to binary number
    public void __bit(int n, char[] array, int size)
    {
        for (int i = size - 1; i >= 0; i--, n >>= 1){
            array[i] = (char)( '0' + (1 & n));
        }
        array[size] = 'z';
    }


    ////


    //produce hash vectors for messages in Group
    public void HashMappingMSGs( List<SocialMSG>  Group, LSB lsb) {
        float[] _g = new float[lsb.m];

        for (SocialMSG msit: Group
                ) {
            lsb.getHashVector(0, (msit).getConceptTFIDFVec(), _g);		//tableID is also 0
            lsb.getZ(_g, (msit).HashV);
            }

    }



    public void HashMappingEvent(SocialEvent se, LSB lsb) {
        float[] _g = null;
        _g = new float[lsb.m];

        lsb.getHashVector(0, se.GetCluster_ConceptTFIDFVec(), _g);		//tableID is also 0
        lsb.getZ(_g, se.EHashV);
    }


    public static float l2_dist_int(int[]_p1, int[]_p2, int _dim)
    {
        float ret = 0;
        for (int i = 0; i < _dim; i++)
        {
            float dif = (float)(_p1[i] - _p2[i]);
            ret += dif * dif;
        }

        return ret;
    }




    void ProduceClusterSeeds(List<SocialMSG>  HashTagedMSGset, List<SocialEvent> seedclusters, LSB lsb) {
        //PartitionGroupsByTimeSpace


        //new MSGSET(new ArrayList<>())
        List<MSGSET> outputGroups = new ArrayList<>();

        //std::list<SocialEvent> outputEventlist;
        System.out.printf("---- ProduceClusterSeeds\n");
        PartitionGroupsByTimeSpace(HashTagedMSGset, outputGroups);//the cpp code is waste too many memory  as most of the items are never used

        HashTagedMSGset.clear();

        int cluIndex=0;
        int groupNum = outputGroups.size();
        for (int i = 0; i < groupNum; i++)
        {
            if (outputGroups.get(i).getSocialMSGs().size() == 0)
                continue;

            HashMappingMSGs(outputGroups.get(i).getSocialMSGs(),lsb);

            System.out.printf("Group i=%d\n", i);
            SocialMSG mp1, mp2;

            Integer[] mps = new Integer[]{0,0};
            while (FindMaxHashConflictMSG(outputGroups.get(i),mps, lsb.m) != 0)
            {
                mp1 = outputGroups.get(i).getSocialMSGs().get(mps[0]);
                mp2 = outputGroups.get(i).getSocialMSGs().get(mps[1]);
                float MaxSim = mp1.GetGlobalSimilarity(mp2);


                SocialEvent eseed = new SocialEvent();
                eseed.EventReset();
                if (MaxSim >= ELIPSE) {  //SIMILAR, then put as seed

                    eseed.uploadEventMsg(mp1);
                    eseed.uploadEventMsg(mp2);

                    //remove the at the specified index
                    Object t1 = outputGroups.get(i).getSocialMSGs().get(mps[0]);
                    Object t2 = outputGroups.get(i).getSocialMSGs().get(mps[1]);
                    outputGroups.get(i).getSocialMSGs().remove(t1);
                    outputGroups.get(i).getSocialMSGs().remove(t2);
                    eseed.SetTimeRange();
                    eseed.SetSpaceRange();
                    eseed.SetCluster_ConceptTFIDFVec();


                    //HashMappingEvent(eseed, lsb);
                    eseed.SetEventNo(cluIndex);
                    cluIndex++;
                    seedclusters.add(eseed);
                }
                else //not similar, put one of them as seed
                {


                    eseed.uploadEventMsg(mp1);
                    outputGroups.get(i).getSocialMSGs().remove(outputGroups.get(i).getSocialMSGs().get(mps[0]));
                    eseed.SetTimeRange();
                    eseed.SetSpaceRange();
                    eseed.SetCluster_ConceptTFIDFVec();


                    //HashMappingEvent(eseed, lsb);
                    eseed.SetEventNo(cluIndex);
                    cluIndex++;
                    seedclusters.add(eseed);
                }
            }

            System.out.printf("seednum: %d\n", seedclusters.size());// less than c++ code //// TODO: 2020/2/25  checked   MaxSim is largger


            for (SocialEvent eit:seedclusters
                 ) {
                ////set virtual features of clusters
   /*             eit.SetCluster_ConceptTFIDFVec();
                HashMappingEvent(eit, lsb);
                eit.SetSpaceRange();
                eit.SetTimeRange();*/
                /////////////
                int found = FindHashConflictMSG(eit, outputGroups.get(i), lsb.m);
                int outputGsize = outputGroups.get(i).getSocialMSGs().size();


                //if (outputGsize > 0) which I think if size =1  , maps-- is no sense
//                if (outputGsize > 1)
//                    mps[0]--;
//                else {
//                    continue;
//                }

                if (outputGsize <= 0){
                    continue;
                }

                mps[0] = outputGroups.get(i).getSocialMSGs().size()-1;

                for (int j = outputGsize; j >0; j--)
                {
                    mp1 = outputGroups.get(i).getSocialMSGs().get(mps[0]);
                    if (!(mp1.conflict !=0)) {
                    if (mps[0] != 0)
                        mps[0]--;
                    continue;
                    }

                    float MaxSim = (eit).GetGlobalSimilarity((mp1));
                    if (MaxSim >= ELIPSE) {  //similar, add it in
                        (eit).uploadEventMsg(mp1);
                        mps[1] = mps[0];
                        if (mps[0] != 0)
                            mps[0]--;

                        outputGroups.get(i).getSocialMSGs().remove(outputGroups.get(i).getSocialMSGs().get(mps[1]));

                        (eit).SetTimeRange();
                        (eit).SetSpaceRange();
                        (eit).SetCluster_ConceptTFIDFVec();
                        //HashMappingEvent((*eit), lsb);

                    } else if ( (mps[0] != 0))
                    {
                        mps[0]--;
                    }

                }



                //the group has no more items
                if (outputGroups.get(i).getSocialMSGs().isEmpty())
                    break;
            }// cluster loop ends


            mps[0] = 0;
            while ( mps[0] < outputGroups.get(i).getSocialMSGs().size())
            {
                mp1 = outputGroups.get(i).getSocialMSGs().get(mps[0]);
                SocialEvent eseed = new SocialEvent();
                eseed.EventReset();
                eseed.uploadEventMsg(mp1);

                eseed.SetTimeRange();
                eseed.SetSpaceRange();
                eseed.SetCluster_ConceptTFIDFVec();


                //HashMappingEvent(eseed, lsb);
                eseed.SetEventNo(cluIndex);
                cluIndex++;
                seedclusters.add(eseed);
                mps[0]++;

            }


            outputGroups.get(i).getSocialMSGs().clear();
        }
        outputGroups.clear();

    }



    void ClusteringNonHashtaggedMSGs(List<SocialEvent> seedclusters, List<SocialMSG>nonHashTagedMSGset, LSB lsb)
    {
        //PartitionGroupsByTimeSpace
        //FindHashConflictMSG
        //Group
        //Remove
        //PartitionGroupsByTimeSpace
        System.out.printf("PartitionGroupsByTimeSpace---ClusteringNonHashtaggedMSGs\n");
        List<MSGSET> outputGroups = new ArrayList<>();


        PartitionGroupsByTimeSpace(nonHashTagedMSGset, outputGroups);
        nonHashTagedMSGset.clear();


        int groupNum = outputGroups.size();
        for (int i = 0; i < groupNum; i++)
        {
            System.out.printf("nonHashtagclustering i=%d\n", i);
            if (outputGroups.get(i).getSocialMSGs().size() == 0)
                continue;
            HashMappingMSGs(outputGroups.get(i).getSocialMSGs(), lsb);

            /////////


            ////////

            SocialEvent eit = null;
            Iterator<SocialEvent> iter = seedclusters.iterator();

            SocialMSG mp1, mp2;

            Integer[] mps = new Integer[]{0,0};

            while (iter.hasNext()){
                eit = iter.next();
                int found = FindHashConflictMSG((eit), outputGroups.get(i), lsb.m);
                int outputGsize = outputGroups.get(i).getSocialMSGs().size();


                if (outputGsize <= 0){
                    continue;
                }
                mps[0] = outputGroups.get(i).getSocialMSGs().size()-1;

                for (int j = outputGsize; j > 0; j--)
                {
                    mp1 = outputGroups.get(i).getSocialMSGs().get(mps[0]);
                    if (!(mp1.conflict !=0)) {
                        if (mps[0] != 0)
                            mps[0]--;
                        continue;
                    }

                    float MaxSim = (eit).GetGlobalSimilarity((mp1));
                    if (MaxSim >= ELIPSE) {  //similar, add it in
                        (eit).uploadEventMsg(mp1);
                        mps[1] = mps[0];
                        if (mps[0] != 0)
                            mps[0]--;

                        outputGroups.get(i).getSocialMSGs().remove(mps[1]);

                        (eit).SetTimeRange();
                        (eit).SetSpaceRange();
                        (eit).SetCluster_ConceptTFIDFVec();
                        //HashMappingEvent((*eit), lsb);

                    } else if ( (mps[0] != 0))
                    {
                        mps[0]--;
                    }

                }


                //jump out
                if (outputGroups.get(i).getSocialMSGs().isEmpty())
                    break;
            }


            //
            outputGroups.get(i).getSocialMSGs().clear();
        }
        outputGroups.clear();
    }



    
    //// TODO: 2020/2/24  invalide reference update
    int FindMaxHashConflictMSG(MSGSET Group, SocialMSG[] maxConflictMsgPs, int dim) {
        int ret = 0;
        float maxConflict = dim;

        SocialMSG p1, p2;
        int len = Group.getSocialMSGs().size();
        for (int i=0; i<len; i++){
            p1 = Group.getSocialMSGs().get(i);
            for (int j = 0; j < len; j++) {
                p2 = Group.getSocialMSGs().get(j);
                float curConflict = l2_dist_int((p1).HashV, (p2).HashV, dim);
                if (curConflict < maxConflict) {
                    maxConflict = curConflict;
                    maxConflictMsgPs[0] = p1;
                    maxConflictMsgPs[1] = p2;
                    ret = 1;
                }

            }
        }

        return ret;
    }


    //// TODO: 2020/2/24  invalide reference update
    int FindMaxHashConflictMSG(MSGSET Group, Integer[] maxConflictMsgPsPos, int dim) {
        int ret = 0;
        float maxConflict = dim;

        SocialMSG p1, p2;
        int len = Group.getSocialMSGs().size();
        for (int i=0; i<len; i++){
            p1 = Group.getSocialMSGs().get(i);
            for (int j = i +1; j < len; j++) {
                p2 = Group.getSocialMSGs().get(j);
                float curConflict = l2_dist_int((p1).HashV, (p2).HashV, dim);
                if (curConflict < maxConflict) {
                    maxConflict = curConflict;
                    maxConflictMsgPsPos[0] = i;
                    maxConflictMsgPsPos[1] = j;
                    ret = 1;
                }

            }
        }

        return ret;
    }

    int FindHashConflictMSG(SocialEvent  seedclusters, MSGSET msgset, int dim)
    {
        int ret = 0;
        float maxConflict = dim;

        SocialMSG p1, p2;
        //p1 = msgset.begin();
        //p1 = StartIter;
        //if (msgset.size() == 1)


        for (int i = 0; i < msgset.getSocialMSGs().size(); i++) {
            p1 = msgset.getSocialMSGs().get(i);

            for (int j = 0; j < seedclusters.getEventMSG().size(); j++) {
                p2 = seedclusters.getEventMSG().get(j);
                
                //// TODO: 2020/2/24  why the type in cpp is int ???
                float curConflict = l2_dist_int((p1).HashV, (p2).HashV, dim);
                if (curConflict < maxConflict)
                {
                    //HashConflictMSGs.push_back(*p1);

                    //HashConflictMSGs = p1;

                    //if(StartIter!=msgset.end())
                    //	StartIter++;

                    p1.conflict = 1;
                    ret = 1;
                }
                else
                    (p1).conflict = 0;

            }
        }
        
        return ret;
    }





     /*
     input messages, output Eventclusters
    */
    void OnlineClustering(List<SocialMSG> HashTagedMSGlist,
            List<SocialMSG> NonHashTagedMSGlist,  List<SocialEvent> Eventclusters, LSB lsb) {

        ProduceClusterSeeds(HashTagedMSGlist, Eventclusters,lsb);
        System.out.printf("seed num: %d\n", Eventclusters.size());

        int sit = 0;
        for (; sit < Eventclusters.size(); sit++){
            System.out.printf("cluster %d \t: msgno:%d\n", Eventclusters.get(sit).GetEventNo(), Eventclusters.get(sit).GetEventMSGs().size());
        }

        /////////////////////////////////////////////
        for (SocialEvent eit:Eventclusters
             ) {
            ////set virtual features of clusters
            (eit).SetCluster_ConceptTFIDFVec();
            (eit).SetSpaceRange();
            (eit).SetTimeRange();
            /////////////
        }

        ClusteringNonHashtaggedMSGs(Eventclusters, NonHashTagedMSGlist,lsb);
        System.out.printf("cluster num: %d\n", Eventclusters.size());




        for (; sit < Eventclusters.size(); sit++){
            System.out.printf("cluster %d \t: msgno:%d\n", Eventclusters.get(sit).GetEventNo(), Eventclusters.get(sit).GetEventMSGs().size());
        }

        //seting eventUserIdsFre for future event migration identification
        SetEventUserIdsFre(Eventclusters);
    }


    void SetEventUserIdsFre(List<SocialEvent> Eventclusters)
    {


        for (SocialEvent ecit:Eventclusters
             ) {
            (ecit).SetEventUserIDs();
        }
    }




}





