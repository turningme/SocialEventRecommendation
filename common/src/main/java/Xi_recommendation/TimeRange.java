package Xi_recommendation;

import java.io.Serializable;

/**
 * @author helen ding on 26/09/2020.
 */
public class TimeRange implements Serializable {
    public float TimeStampCentre;   //time stamp: change date-time into int, we use float to fit the composite time point
    public float range;  //the uncertainty threshold range \tau with time stamp

    public float getTimeStampCentre() {
        return TimeStampCentre;
    }

    public void setTimeStampCentre(float timeStampCentre) {
        TimeStampCentre = timeStampCentre;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }
}
