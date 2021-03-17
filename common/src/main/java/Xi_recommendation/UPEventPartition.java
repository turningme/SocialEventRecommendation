package Xi_recommendation;

import java.io.Serializable;
import java.util.ArrayList;


public class UPEventPartition implements Serializable {

    public int userNum; //the number of users in this partition
    public  ArrayList<UPEventBucket> UProfileEventGroup = new ArrayList<UPEventBucket>();
    public  ValueRangePair[] TopicRangeVector = new ValueRangePair[parameters.TFIDF_DIM];
    public  ValueRangePair TimeRangePair;
    public  ValueRangePair[] SpaceRangePair = new ValueRangePair[2]; //SpaceRangePair [0] stores max and min Lat, SpaceRangePair[1] stores max and min longi

    public  ArrayList<UserValueRangePair> InfluenceRangeVector;  //include TUNUM ValueRangePairs
    public  int minNumOfInfluencedUsers;
    public  int maxNumOfInfluencedUsers;

    public void UPEventPartition() {
        userNum = 1;
    }

    public void UPEventPartition(int uNum) {
        userNum = uNum;
    }

    public  void setUProfileEventGroup(UPEventBucket e) {
        UProfileEventGroup.add(e);
    }

    public  ArrayList<UPEventBucket> getUProfileEventGroup() {
        return UProfileEventGroup;
    }

    public void setTopicRangeVector(ValueRangePair[] TRV) {
        for (int i = 0; i < parameters.TFIDF_DIM; i++) {
            if (TopicRangeVector[i] == null){
                TopicRangeVector[i] = new ValueRangePair();
            }
            TopicRangeVector[i].minV = TRV[i].minV;
            TopicRangeVector[i].maxV = TRV[i].maxV;
        }
    }

    public  ValueRangePair[] getTopicRangeVector() {
        return TopicRangeVector;
    }

    public  void setSpaceRangePair(ValueRangePair[] TRV) {
        for (int i = 0; i < 2; i++) {
            if (SpaceRangePair[i] == null){
                SpaceRangePair[i] = new ValueRangePair();
            }
            SpaceRangePair[i].minV = TRV[i].minV;
            SpaceRangePair[i].maxV = TRV[i].maxV;
        }
    }

    public  ValueRangePair[] getSpaceRangePair() {
        return SpaceRangePair;
    }

    public  void setTimeRangePair(ValueRangePair TRV) {
        if (TimeRangePair == null){
            TimeRangePair = new ValueRangePair();
        }
        TimeRangePair.minV = TRV.minV;
        TimeRangePair.maxV = TRV.maxV;
    }

    public  ValueRangePair getTimeRangePair() {
        return TimeRangePair;
    }

    public  void setInfluenceRangeVector(ArrayList<UserValueRangePair> IRV) {
        for (int i = 0; i < IRV.size(); i++) {
            InfluenceRangeVector.add(IRV.get(i));
        }
    }

    public  ArrayList<UserValueRangePair> getInfluenceRangeVector() {
        return InfluenceRangeVector;
    }

    public  void setMinNumInfluencedUsers(int minNum) {
        minNumOfInfluencedUsers = minNum;
    }

    public   void setMaxNumInfluencedUsers(int maxNum) {
        maxNumOfInfluencedUsers = maxNum;
    }

    public      int getMinNumInfluencedUsers() {
        return minNumOfInfluencedUsers;
    }

    public      int getMaxNumInfluencedUsers() {
        return maxNumOfInfluencedUsers;
    }

    public void UPEventPartitionClear() {
        UProfileEventGroup.clear();
    }


    public void SetUN(ValueRangePair un) {
        minNumOfInfluencedUsers= (int) un.minV;
        maxNumOfInfluencedUsers= (int) un.maxV;
    }

    public void reset(){
        userNum = -1;
        UProfileEventGroup = new ArrayList<UPEventBucket>();
        TopicRangeVector = new ValueRangePair[parameters.TFIDF_DIM];
        TimeRangePair = null;
        SpaceRangePair = new ValueRangePair[2];

        InfluenceRangeVector = new ArrayList<>();
        minNumOfInfluencedUsers = -1;
        maxNumOfInfluencedUsers = -1;
    }
}
