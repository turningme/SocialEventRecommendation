package Xi_recommendation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author helen ding on 26/09/2020.
 */
public class UserProfile implements Serializable {

    int userId;  //the index no of user in user dictionary
//    int userOId;  // the original user id from twitter
    public String userOId;
    List<UPInfluDistriEle> UserInfluenceDistri = new ArrayList<>();

    SpaceRange UserPhysicalLocation;
    int PostNum;//the toal number of posts for this user
    List<UPRespnseEle> UserResponseNumbers = new ArrayList<>(); //user variable length array to reduce the memory cost
    public List<SubEvent> UserInterestEvents = new ArrayList<>();

    int Inserted;//if allocated to a partition, for user partition
    List<SubEvent> GetUserInterestEvents() {
        return UserInterestEvents;
    }

    public void UserProfileAllocation(String MTimeSlotFlist,
                                      String path, String outpath,
                                      String userlist, String userProfilePath,
                                      String UserInfluFilePath, String UserInfluFileName) {

    }

}
