package Xi_recommendation;

import java.io.Serializable;

/**
 * @author helen ding on 26/09/2020.
 */
public class UPRespnseEle implements Serializable {
    int userid;
    int userResponse; //the number of response, ...for a user interacting with other users--use training data to get this
    //ith element is the number of userID's messages followed by ith user in the training dataset.

}
