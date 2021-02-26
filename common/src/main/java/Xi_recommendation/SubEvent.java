package Xi_recommendation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static Xi_recommendation.EventMigrationRecom.*;
import static cern.clhep.Units.rad;
import static cern.jet.math.Functions.sqrt;
import static java.lang.Math.*;
import static org.turningme.theoretics.common.Constants.EARTH_RADIUS;
import static org.turningme.theoretics.common.Constants.TFIDF_DIM;

/**
 * @author helen ding on 26/09/2020.
 */
public class SubEvent implements Serializable {
    private boolean flag = false;

    public int retweetedStatus;
    public TimeRange Cluster_TR = new TimeRange();
    public SpaceRange Cluster_SR = new SpaceRange();

    public ArrayList<SpaceRange> msgSRset=new ArrayList<>();
    public ArrayList<TimeRange> msgTRset=new ArrayList<>();

    public List<Float> Cluster_ConceptTFIDFVec = new ArrayList<>();
    public List<EUserFrePair> eventUserIdsFre = new ArrayList<>();
    public List<SocialMSG> EventMSG=new ArrayList<>();

    public int eventno;
    public int migEno;  //this subevent eventno is relevant to migEno based on similarity or migration

    public List<Integer> EHashV = new ArrayList<>(); //leave it for hash-based clustering.
    public int EventHashV; //leave it for hash-based data partition for parallel processing over spark.
    public List<EventUserSimi> RecUserSimi; //the user id list with similarity for recommending this event to,
    // for parallel processing over spark only
    //	std::vector<int> userlist; //a list of users whose user profile contains this event

    public List<String> HashtagList=new ArrayList<>();
    public int eventCluster; //for checking if the current subEvent is in groudtruth event
    public float HistEventSimilarity; //for history events similarity precomputation for fast recommendation

    public void SetEventNo(int eno) {
        eventno = eno;
    }



    public void addConceptTFIDFVec(float vec) {
        Cluster_ConceptTFIDFVec.add(vec);
    }

    public void setSpaceRange(SpaceRange sr) {
        Cluster_SR.lat = sr.lat;
        Cluster_SR.longi = sr.longi;
        Cluster_SR.radius = sr.radius;
    }
    public void setTimeRange(TimeRange tr) {
        Cluster_TR.TimeStampCentre = tr.TimeStampCentre;
        Cluster_TR.range = tr.range;
    }
    public void setEventUserIDs(List<EUserFrePair> euidlist) {
        eventUserIdsFre.addAll(euidlist);
    }
    public int GetEventNo() {
        return eventno;
    }

    /**
     * @author Xi chen on 27/09/2020
     */
    private ArrayList<String> EventUserIDs=new ArrayList<>();

    void uploadEventMsg(SocialMSG newsocialmsg){EventMSG.add(newsocialmsg);}
    List<SocialMSG> GetEventMSGs(){return EventMSG;}

    public void uploadmsgSRset(SpaceRange sr) {
        msgSRset.add(sr);
    }
    public void uploadmsgTRset(TimeRange tr){
        msgTRset.add(tr);
    }


    public void EventReset() {
        int TFIDF_DIM=new parameters().TFIDF_DIM;
        for (int i=0; i<TFIDF_DIM; i++){
            Cluster_ConceptTFIDFVec.add(0f);
        }
        for (int i = 0; i < TFIDF_DIM; i++) {
            Cluster_ConceptTFIDFVec.set(i,(float) 0);
        }
        Cluster_TR.TimeStampCentre = 0;
        Cluster_TR.range = 0;
        Cluster_SR.lat = 0;
        Cluster_SR.longi =0;
        Cluster_SR.radius = 0;
        eventno = 0;
        migEno = 0;
        eventCluster = 0;
        HistEventSimilarity = 0;
        cleansubEvent();
        EventHashV=0;
    }

    public void cleansubEvent() {
        EventMSG.clear();
        HashtagList.clear();
        eventUserIdsFre.clear();
    }

    public int SetEventUserIDs(){
        int usernumber = 0;
        for(SocialMSG smgit:EventMSG) {
            List<EUserFrePair> euit=smgit.getEventUserIDsFre();
            for(int i=0;i<euit.size();i++) {
                if (i != euit.size()-1) {
                    for(EUserFrePair eufpit:eventUserIdsFre) {
                        if(eufpit.userid==euit.get(i).userid) {
                            eufpit.frequency+=euit.get(i).frequency;
                            break;
                        }
                    }
                }
                if(i==euit.size()-1) {
                    EUserFrePair newUFpair=new EUserFrePair();
                    newUFpair.userid=euit.get(i).userid;
                    newUFpair.frequency=euit.get(i).frequency;
                    eventUserIdsFre.add(newUFpair);
                }
            }
        }
        return eventUserIdsFre.size();
    }

    public void setConceptTFIDFVec(float[] vec) {
//        System.out.println(vec.length);

        for (int i = 0; i < vec.length; i++) {
            Cluster_ConceptTFIDFVec.add(i,vec[i]);
        }
    }

    public void SetCluster_ConceptTFIDFVec()
    {
        int TFIDF_DIM=new parameters().TFIDF_DIM;
        for (int i = 0; i < TFIDF_DIM; i++) {
            Cluster_ConceptTFIDFVec.set(i,(float)0);
        }

        for(SocialMSG emit:EventMSG){
            for (int i = 0; i < TFIDF_DIM; i++) {
                float temp=Cluster_ConceptTFIDFVec.get(i)+emit.getConceptTFIDFVec()[i];
                Cluster_ConceptTFIDFVec.set(i,temp);
            }
        }
        for (int i = 0; i < TFIDF_DIM; i++) {
            float temp=Cluster_ConceptTFIDFVec.get(i)/EventMSG.size();
            Cluster_ConceptTFIDFVec.set(i,temp);
        }
    }

    public void SetTimeRange() {
        float MAXFLOAT=new parameters().MAXFLOAT;
        float minT = MAXFLOAT;
        float TIMERADIUST=new parameters().TIMERADIUST;


        float maxT = 0;
        //find the minimal time point, and maximal time point
        for(SocialMSG emit:EventMSG){
            float tmin=emit.getTimeRange().TimeStampCentre-emit.getTimeRange().range;
            float tmax=emit.getTimeRange().TimeStampCentre+emit.getTimeRange().range;
            if (tmin < minT)
                minT = tmin;
            if (tmax > maxT)
                maxT = tmax;
        }
        Cluster_TR.TimeStampCentre = (minT + maxT) / 2;
        //Cluster_TR.range = (maxT - minT) / 2;
        Cluster_TR.range = TIMERADIUST; //fix the time range
    }
    public void SetmigEventNo(int eno) {
        migEno = eno;
    }
    public List<EUserFrePair> GetEventUserIDs() {
        return eventUserIdsFre;
    }
    public float EventSimi(SubEvent otherSubEvent) {
        float simi = 1;
        //SocialMSG thisMSG(retweetedStatus, Cluster_ConceptTFIDFVec, Cluster_TR, Cluster_SR, eventUserIdsFre);
        //SocialMSG otherMSG(otherSubEvent.retweetedStatus, otherSubEvent.Cluster_ConceptTFIDFVec, otherSubEvent.Cluster_TR, otherSubEvent.Cluster_SR, otherSubEvent.eventUserIdsFre);

        //simi = thisMSG.GetGlobalSimilarity(&otherMSG);
        simi = GetGlobalSimilarity(otherSubEvent);
        return simi;
    }

    public float GetGlobalSimilarity(SubEvent sevt)
    {
        float Gsim = 0;

        switch (MethodChoice) {
            case 1:
                Gsim = GetConceptSimilarity(sevt);
                break;
            case 2:
                Gsim = GetTimeSimilarity(sevt);
                break;
            case 3:
                Gsim = GetSpaceSimilarity(sevt);
                break;
            case 4:
                //Gsim = GetConceptSimilarity(sevt) * GetTimeSimilarity(sevt);
                Gsim = omeg1 * GetConceptSimilarity(sevt) + (1 - omeg1)*GetTimeSimilarity(sevt);
                break;
            case 5:
                Gsim = omeg1* GetConceptSimilarity(sevt) + (1 - omeg1)*GetSpaceSimilarity(sevt);
                break;
            case 6:
                Gsim = omeg1 * GetConceptSimilarity(sevt) + omeg2 * GetTimeSimilarity(sevt) + (1 - omeg1 - omeg2) * GetSpaceSimilarity(sevt);
                break;
            case 7:
                Gsim = GetConceptSimilarity(sevt) * GetTimeSimilarity(sevt);
                break;
            default:
                Gsim = GetConceptSimilarity(sevt) * GetTimeSimilarity(sevt) * GetSpaceSimilarity(sevt);
        }

        return Gsim;
    }

    public float GetConceptSimilarity(SubEvent sevt) {
        float simi = 0;
        for (int i = 0; i < TFIDF_DIM; i++) {
            simi += sevt.Cluster_ConceptTFIDFVec.get(i) * Cluster_ConceptTFIDFVec.get(i);
        }
        float thisModule = this.CVModule();
        float smsgModule = sevt.CVModule();
        simi /= thisModule;
        simi /= smsgModule;

        return simi;
    }

    public float CVModule() {
        float ret = 0;
        for (int i = 0; i < TFIDF_DIM; i++) {
            ret += Cluster_ConceptTFIDFVec.get(i) * Cluster_ConceptTFIDFVec.get(i);
        }
        return (float) sqrt(ret);
    }
    public float GetTimeSimilarity(SubEvent sevt) {
        float Tsim;
        Tsim = getTimeDiff(Cluster_TR, sevt.Cluster_TR);
        return Tsim;
    }

    public float getTimeDiff(TimeRange thisTR, TimeRange sevtR) {
        float Tsim=0;
        //for tau=0
        if (TIMERADIUST == 0)
        {
            if (thisTR.TimeStampCentre == sevtR.TimeStampCentre)
                Tsim = 1;

            return Tsim;
        }
        //============
        TimeRange maxPtr,  minPtr;
        if (thisTR.TimeStampCentre >= sevtR.TimeStampCentre) {
            maxPtr = thisTR;
            minPtr = sevtR;
        }
        else {
            maxPtr = sevtR;
            minPtr = thisTR;
        }

        if (maxPtr.TimeStampCentre - minPtr.TimeStampCentre >= 2 * maxPtr.range)
            Tsim = 0;
        else {
            float intersectT = (minPtr.TimeStampCentre + minPtr.range) - (maxPtr.TimeStampCentre - maxPtr.range);
            float unionT = (maxPtr.TimeStampCentre + maxPtr.range) - (minPtr.TimeStampCentre - minPtr.range);
            Tsim = intersectT / unionT;
        }
        return Tsim;
    }

    public float GetSpaceSimilarity(SubEvent sevt) {
        float Ssim;
        double GreatCircleDist = get_distance(this.Cluster_SR.lat, this.Cluster_SR.longi, sevt.Cluster_SR.lat, sevt.Cluster_SR.longi);
        //get the Hausdorff distance
        double haus = get_Hausdorff_distance(sevt);

        //Ssim = 1 - GreatCircleDist / 500;  //put 500 first, default, it can be the maximal Great Circle distance between two locations in dataset
        Ssim =(float) (1 - (haus+GreatCircleDist)/ 1000);
        if (Ssim < 0)
            Ssim = 0;

        return Ssim;
    }

    public double get_distance(double lat1, double lng1, double lat2, double lng2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * asin(sqrt(pow(sin(a / 2), 2) + cos(radLat1)*cos(radLat2)*pow(sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = round(s * 10000) / 10000;
        return s;
    }

    public double rad(double d) {
        return d * PI / 180.0;
    }

    public double get_Hausdorff_distance(SubEvent sevt) {
        double HausDist = 0;

        double ThisPoint2SevtSet=0;

        for(int i=0; i<sevt.msgSRset.size(); i++){
            double nearDist = EARTH_RADIUS;

            for(int j=0; j<sevt.msgSRset.size();j++){

                double tmpDist = get_distance(sevt.msgSRset.get(i).lat, sevt.msgSRset.get(i).longi, sevt.msgSRset.get(j).lat, sevt.msgSRset.get(j).longi);
                if (tmpDist < nearDist)
                    nearDist = tmpDist;
            }
            if (nearDist > ThisPoint2SevtSet)
                ThisPoint2SevtSet = nearDist;
        }

        double SevtSet2ThisPoint = 0;

        for(int i=0; i<sevt.msgSRset.size();i++){
            double nearDist = EARTH_RADIUS;
            for(int j=0; j<sevt.msgSRset.size();j++){
                double tmpDist = get_distance(sevt.msgSRset.get(i).lat, sevt.msgSRset.get(i).longi, sevt.msgSRset.get(j).lat, sevt.msgSRset.get(j).longi);
                if (tmpDist < nearDist)
                    nearDist = tmpDist;
            }
            if (nearDist > SevtSet2ThisPoint)
                SevtSet2ThisPoint = nearDist;
        }
        HausDist = (ThisPoint2SevtSet + SevtSet2ThisPoint) / 2;
        return HausDist;
    }


    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
