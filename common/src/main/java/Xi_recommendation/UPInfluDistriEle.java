package Xi_recommendation;

import java.io.Serializable;

/**
 * @author helen ding on 26/09/2020.
 */
public class UPInfluDistriEle implements Serializable {
    int userid;
    float userInflu; //the ratio of comments, ...for a user interacting with other users--use training data to get this

    UPInfluDistriEle(){
        userid=0;
        userInflu=0;
    }
}
