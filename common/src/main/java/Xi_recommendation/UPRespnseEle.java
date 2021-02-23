package Xi_recommendation;

import java.io.Serializable;

/**
 * @author helen ding on 26/09/2020.
 */
public class UPRespnseEle implements Serializable {
    public int userid;
    public int userResponse; //the number of response, ...for a user interacting with other users--use training data to get this
    //ith element is the number of userID's messages followed by ith user in the training dataset.


    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(int userResponse) {
        this.userResponse = userResponse;
    }
}
