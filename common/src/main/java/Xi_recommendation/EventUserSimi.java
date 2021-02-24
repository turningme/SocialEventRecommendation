package Xi_recommendation;

import java.io.Serializable;

/**
 * @author helen ding on 26/09/2020.
 */
public class EventUserSimi implements Serializable {
    public int userid;
    public float simi;

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public float getSimi() {
        return simi;
    }

    public void setSimi(float simi) {
        this.simi = simi;
    }
}
