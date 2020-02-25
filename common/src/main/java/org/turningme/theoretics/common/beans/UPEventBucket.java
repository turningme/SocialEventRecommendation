package org.turningme.theoretics.common.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.turningme.theoretics.common.event.SocialEvent;

import static org.turningme.theoretics.common.Constants.TFIDF_DIM;
import static org.turningme.theoretics.common.Constants.TUNUM;

/**
 * Created by jpliu on 2020/2/24.
 */
public class UPEventBucket  implements Serializable {
    public int userNum; //the number of users in this partition
    public List<SocialEvent> UProfileEventGroup = new ArrayList<>();
    public ValueRangePair[] TopicRangeVector = new ValueRangePair[TFIDF_DIM] ;
    public ValueRangePair[] InfluenceRangeVector = new ValueRangePair[TUNUM]  ;  //include TUNUM ValueRangePairs
    public int minNumOfInfluencedUsers;
    public int maxNumOfInfluencedUsers;

    public UPEventBucket() {
        userNum = 1;
        //InfluenceRangeVector = new ValueRangePair[userNum];
    }



    public UPEventBucket(int uNum) {
        userNum = uNum;
        //	InfluenceRangeVector = new ValueRangePair[userNum];
    }

    public void setUProfileEventGroup(SocialEvent e) {
        UProfileEventGroup.add(e);
    }

    public List<SocialEvent> getUProfileEventGroup() {
        return UProfileEventGroup;
    }

    public void setTopicRangeVector(ValueRangePair[] TRV) {
        for (int i = 0; i < TFIDF_DIM; i++) {
            TopicRangeVector[i].minV = TRV[i].minV;
            TopicRangeVector[i].maxV = TRV[i].maxV;
        }

    }

    public ValueRangePair[]  getTopicRangeVector() {
        return TopicRangeVector;
    }

    public void setInfluenceRangeVector(ValueRangePair[]  IRV) {
        for (int i = 0; i < userNum; i++) {
            InfluenceRangeVector[i].minV = IRV[i].minV;
            InfluenceRangeVector[i].maxV = IRV[i].maxV;
        }
    }

    public ValueRangePair[]  getInfluenceRangeVector() {
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
