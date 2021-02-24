package Xi_recommendation;

import java.io.Serializable;

/**
 * @author helen ding on 26/09/2020.
 */
public class EUserFrePair implements Serializable {
    public int userid;

    /*the number of comments, ...for a user interacting with a sub-event or a message
    in his history user profile--use training data to get this*/
    public int frequency;

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
