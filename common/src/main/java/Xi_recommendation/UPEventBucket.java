package Xi_recommendation;

import java.io.Serializable;
import java.util.ArrayList;

import org.turningme.theoretics.common.beans.ValueRangePair;

public class UPEventBucket implements Serializable {
    public static int userNum; //the number of users in this partition
    public static ArrayList<SubEvent> UProfileEventGroup = new ArrayList<SubEvent>();
    public static ArrayList<ValueRangePair> TopicRangeVector = new ArrayList<ValueRangePair>();
    public static ArrayList<UserValueRangePair> InfluenceRangeVector = new ArrayList<UserValueRangePair>();  //include TUNUM ValueRangePairs
    int minNumOfInfluencedUsers;
    int maxNumOfInfluencedUsers;

    public static void UPEventBucket() {
        userNum = 1;
        //InfluenceRangeVector = new ValueRangePair[userNum];
    }

    public static void UPEventBucket(int uNum) {
        userNum = uNum;
        //	InfluenceRangeVector = new ValueRangePair[userNum];
    }

    public void setUProfileEventGroup(SubEvent e) {
        this.UProfileEventGroup.add(e);
    }

    public ArrayList<SubEvent> getUProfileEventGroup() {
        return UProfileEventGroup;
    }

    public void setTopicRangeVector(ArrayList<ValueRangePair> TRV) {
        for (int i = 0; i < TRV.size(); i++) {
            TopicRangeVector.add(TRV.get(i));
        }
    }

    public ArrayList<ValueRangePair> getTopicRangeVector() {
        return TopicRangeVector;
    }

    public void setInfluenceRangeVector(ArrayList<UserValueRangePair> IRV) {
        for (int i = 0; i < userNum; i++) {
            InfluenceRangeVector.add(IRV.get(i));
        }
    }

    public ArrayList<UserValueRangePair> getInfluenceRangeVector() {
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

