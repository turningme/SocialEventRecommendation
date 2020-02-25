package org.turningme.theoretics.common.beans;

import java.util.ArrayList;
import java.util.List;

import static org.turningme.theoretics.common.Constants.TFIDF_DIM;
import static org.turningme.theoretics.common.Constants.TUNUM;

/**
 * Created by jpliu on 2020/2/24.
 */
public class UPEventPartition {
    public int userNum; //the number of users in this partition
    public List<UPEventBucket> UProfileEventGroup = new ArrayList<>();
    public ValueRangePair[] TopicRangeVector = new ValueRangePair[TFIDF_DIM];
    public ValueRangePair[] InfluenceRangeVector = new ValueRangePair[TUNUM];  //include TUNUM ValueRangePairs
    public int minNumOfInfluencedUsers;
    public int maxNumOfInfluencedUsers;



    public UPEventPartition() {
        userNum = 1;
    }

    public UPEventPartition(int uNum) {
        userNum = uNum;
    }

    public void setUProfileEventGroup(UPEventBucket e) {
        UProfileEventGroup.add(e);
    }

    public List<UPEventBucket> getUProfileEventGroup() {
        return UProfileEventGroup;
    }

    public void setTopicRangeVector(ValueRangePair[] TRV) {
        for (int i = 0; i < TFIDF_DIM; i++) {
            if ( TopicRangeVector[i]== null){
                TopicRangeVector[i] = new ValueRangePair();
            }
            TopicRangeVector[i].minV = TRV[i].minV;
            TopicRangeVector[i].maxV = TRV[i].maxV;
        }

    }

    public ValueRangePair [] getTopicRangeVector() {
        return TopicRangeVector;
    }

    public void setInfluenceRangeVector(ValueRangePair[] IRV) {
        for (int i = 0; i < TUNUM; i++) {

            if ( InfluenceRangeVector[i]== null){
                InfluenceRangeVector[i] = new ValueRangePair();
            }
            InfluenceRangeVector[i].minV = IRV[i].minV;
            InfluenceRangeVector[i].maxV = IRV[i].maxV;
        }
    }

    public ValueRangePair [] getInfluenceRangeVector() {
        return InfluenceRangeVector;
    }

    public void setMinNumInfluencedUsers(int minNum) {
        minNumOfInfluencedUsers = minNum;
    }

    public void setMaxNumInfluencedUsers(int maxNum) {
        maxNumOfInfluencedUsers = maxNum;
    }

    public int getMinNumInfluencedUsers() {
        return minNumOfInfluencedUsers;
    }

    public int getMaxNumInfluencedUsers() {
        return maxNumOfInfluencedUsers;
    }
}
