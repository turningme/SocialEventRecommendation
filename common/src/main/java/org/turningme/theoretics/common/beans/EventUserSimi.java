package org.turningme.theoretics.common.beans;

import java.io.Serializable;

/**
 * Created by jpliu on 2020/2/23.
 */
public class EventUserSimi  implements Serializable {
    public int userid;
    public float simi;

    public EventUserSimi(float simi, int userid) {
        this.simi = simi;
        this.userid = userid;
    }

    public float getSimi() {
        return simi;
    }

    public void setSimi(float simi) {
        this.simi = simi;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }
}
