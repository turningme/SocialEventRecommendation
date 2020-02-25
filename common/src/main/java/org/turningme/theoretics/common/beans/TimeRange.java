package org.turningme.theoretics.common.beans;

import java.io.Serializable;

/**
 * Created by jpliu on 2020/2/23.
 */
public class TimeRange  implements Serializable {
    public float TimeStampCentre;   //time stamp: change date-time into int
    public float range;  //the uncertainty threshold range \tau with time stamp , should be changed to int type  // TODO: 2020/2/25


    public TimeRange() {
    }

    public TimeRange(float range, int timeStampCentre) {
        this.range = range;
        TimeStampCentre = timeStampCentre;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public float getTimeStampCentre() {
        return TimeStampCentre;
    }

    public void setTimeStampCentre(int timeStampCentre) {
        TimeStampCentre = timeStampCentre;
    }
}
