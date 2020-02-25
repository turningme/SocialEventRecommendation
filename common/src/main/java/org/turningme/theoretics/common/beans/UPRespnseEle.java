package org.turningme.theoretics.common.beans;

/**
 * Created by jpliu on 2020/2/24.
 */
public class UPRespnseEle {
    public int userid;
    public int userResponse; //the number of response, ...for a user interacting with other users--use training data to get this
    //ith element is the number of userID's messages followed by ith user in the training dataset.
}
