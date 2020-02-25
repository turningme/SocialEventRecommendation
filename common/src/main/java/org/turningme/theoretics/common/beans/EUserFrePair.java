package org.turningme.theoretics.common.beans;

import java.io.Serializable;

/**
 * Created by jpliu on 2020/2/23.
 */
public class EUserFrePair  implements Serializable {
    public int userid;
    public int frequency; //the number of comments, ...for a user interacting with an event in his history user profile--use training data to get this

    public EUserFrePair() {
    }

    public EUserFrePair(int frequency, int userid) {
        this.frequency = frequency;
        this.userid = userid;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }
}
