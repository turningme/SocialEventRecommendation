package org.turningme.theoretics.common.beans;

import java.util.ArrayList;
import java.util.List;

import org.turningme.theoretics.common.event.SocialEvent;

/**
 * Created by jpliu on 2020/2/24.
 */
public class UserProfile {
    public int userId;  //the index no of user in user dictionary
    //float UserInfluenceDistri[TUNUM];
    public List<UPInfluDistriEle> UserInfluenceDistri = new ArrayList<>();

    public SpaceRange UserPhysicalLocation = new SpaceRange();
    public int PostNum;//the toal number of posts for this user
    //int UserResponseNumbers[TUNUM];	 //ith element is the number of userID's messages followed by ith user in the training dataset.
    public List<UPRespnseEle> UserResponseNumbers = new ArrayList<>(); //user variable length array to reduce the memory cost
    public List<SocialEvent> UserInterestEvents = new ArrayList<>();

    public int Inserted;//if allocated to a partition, for user partition



    public static UserProfile createDefaultUP(){
        UserProfile up = new UserProfile();
        up.reset();
        return up;
    }

    public UserProfile reset(){
        UserProfile up = this;
        //reset up for next user profile reading.
        up.PostNum = 0;
        up.userId = 0;
        up.UserInfluenceDistri.clear();
        up.UserInterestEvents.clear();
        up.UserPhysicalLocation.lat = 0;
        up.UserPhysicalLocation.longi = 0;
        up.UserPhysicalLocation.radius = 0;
        up.UserResponseNumbers.clear();
        up.Inserted = 0;
        return up;
    }


}
