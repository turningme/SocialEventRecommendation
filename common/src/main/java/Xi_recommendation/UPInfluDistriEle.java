package Xi_recommendation;

import java.io.Serializable;

/**
 * @author helen ding on 26/09/2020.
 */
public class UPInfluDistriEle implements Serializable {
    public int userid;
    public float userInflu; //the ratio of comments, ...for a user interacting with other users--use training data to get this

    public UPInfluDistriEle(){
        userid=0;
        userInflu=0;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public float getUserInflu() {
        return userInflu;
    }

    public void setUserInflu(float userInflu) {
        this.userInflu = userInflu;
    }
}
