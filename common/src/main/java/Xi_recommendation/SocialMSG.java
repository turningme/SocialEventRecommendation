package Xi_recommendation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author helen ding on 26/09/2020.
 */
public class SocialMSG {

    /**
     * @author Xi chen on 27/09/2020
     */
    int hashtaged;
    long retweetedStatus; // if this message is retweeted one, retweetedStatus is its source msgID. Otherwise, it is 0.
    long msgID;
    TimeRange TR=new TimeRange();
    SpaceRange SR=new SpaceRange();
    parameters para=new parameters();

    int TFIDF_DIM=para.TFIDF_DIM;

    float ConceptTFIDFVec[]=new float[TFIDF_DIM];


    ArrayList<String> HashtagList = new ArrayList();
    List<EUserFrePair> EventUserIDsFre=new ArrayList<>();

    void setMSGID(long mid) {
        msgID = mid;
    }

    public SocialMSG(long retweetedNo, float[] vec, TimeRange tr, SpaceRange sr, ArrayList<EUserFrePair> euids) {
        for (int i = 0; i < TFIDF_DIM; i++) {
            this.ConceptTFIDFVec[i] = vec[i];
        }
        this.retweetedStatus = retweetedNo;
        this.TR.TimeStampCentre = tr.TimeStampCentre;
        this.TR.range = tr.range;
        this.SR.lat = sr.lat;
        this.SR.longi = sr.longi;
        this.SR.radius = sr.radius;
        this.hashtaged = 0;
        this.msgID = 0;
        this.EventUserIDsFre.addAll(euids);
    }

    public void SocialMSGInitial(long retweetedNo, float[] vec, TimeRange tr, SpaceRange sr, ArrayList<EUserFrePair> euids) {
        for (int i = 0; i < TFIDF_DIM; i++) {
            this.ConceptTFIDFVec[i] = vec[i];
        }
        this.retweetedStatus = retweetedNo;
        this.TR.TimeStampCentre = tr.TimeStampCentre;
        this.TR.range = tr.range;
        this.SR.lat = sr.lat;
        this.SR.longi = sr.longi;
        this.SR.radius = sr.radius;
        this.hashtaged = 0;
        this.msgID = 0;
        this.EventUserIDsFre.addAll(euids);
    }



    public long getMSGID() {
        return msgID;
    }

    void cleanMSG() {
        EventUserIDsFre.clear();
        HashtagList.clear();
    }
    public List<EUserFrePair> getEventUserIDsFre() { return EventUserIDsFre;}
    public float[] getConceptTFIDFVec(){return ConceptTFIDFVec;}
//    getTimeRange(){}
    TimeRange getTimeRange() { return TR;}

}
