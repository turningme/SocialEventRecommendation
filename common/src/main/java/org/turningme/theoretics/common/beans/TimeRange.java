package org.turningme.theoretics.common.beans;

/**
 * Created by jpliu on 2020/2/23.
 */
public class TimeRange {
    public float TimeStampCentre;   //time stamp: change date-time into int
    public float range;  //the uncertainty threshold range \tau with time stamp


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
