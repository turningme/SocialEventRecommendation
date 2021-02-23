package Xi_recommendation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author helen ding on 26/09/2020.
 */
public class UserProfile implements Serializable {

    /**
     * the index no of user in user dictionary
     */
    public int userId;
//    int userOId;  // the original user id from twitter
    public String userOId;
    public List<UPInfluDistriEle> UserInfluenceDistri = new ArrayList<>();

    public SpaceRange UserPhysicalLocation;
    public  int PostNum;//the toal number of posts for this user
    public List<UPRespnseEle> UserResponseNumbers = new ArrayList<>(); //user variable length array to reduce the memory cost
    public List<SubEvent> UserInterestEvents = new ArrayList<>();

    /**
     * if allocated to a partition, for user partition
     */
    int Inserted;
    List<SubEvent> GetUserInterestEvents() {
        return UserInterestEvents;
    }

    public void UserProfileAllocation(String MTimeSlotFlist,
                                      String path, String outpath,
                                      String userlist, String userProfilePath,
                                      String UserInfluFilePath, String UserInfluFileName) {

    }

    public void reset(){
        userId = Integer.MIN_VALUE;
        userOId = null;
        UserInfluenceDistri.clear();
        UserPhysicalLocation = null;
        PostNum = Integer.MIN_VALUE;
        UserResponseNumbers.clear();
        UserInterestEvents.clear();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserOId() {
        return userOId;
    }

    public void setUserOId(String userOId) {
        this.userOId = userOId;
    }

    public List<UPInfluDistriEle> getUserInfluenceDistri() {
        return UserInfluenceDistri;
    }

    public void setUserInfluenceDistri(List<UPInfluDistriEle> userInfluenceDistri) {
        UserInfluenceDistri = userInfluenceDistri;
    }

    public SpaceRange getUserPhysicalLocation() {
        return UserPhysicalLocation;
    }

    public void setUserPhysicalLocation(SpaceRange userPhysicalLocation) {
        UserPhysicalLocation = userPhysicalLocation;
    }

    public int getPostNum() {
        return PostNum;
    }

    public void setPostNum(int postNum) {
        PostNum = postNum;
    }

    public List<UPRespnseEle> getUserResponseNumbers() {
        return UserResponseNumbers;
    }

    public void setUserResponseNumbers(List<UPRespnseEle> userResponseNumbers) {
        UserResponseNumbers = userResponseNumbers;
    }

    public List<SubEvent> getUserInterestEvents() {
        return UserInterestEvents;
    }

    public void setUserInterestEvents(List<SubEvent> userInterestEvents) {
        UserInterestEvents = userInterestEvents;
    }

    public int getInserted() {
        return Inserted;
    }

    public void setInserted(int inserted) {
        Inserted = inserted;
    }
}
