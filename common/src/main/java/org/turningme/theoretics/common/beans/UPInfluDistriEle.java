package org.turningme.theoretics.common.beans;

import java.io.Serializable;

/**
 * Created by jpliu on 2020/2/24.
 */
public class UPInfluDistriEle  implements Serializable {
    public int userid;
    public float userInflu; //the ratio of comments, ...for a user interacting with other users--use training data to get this
}
