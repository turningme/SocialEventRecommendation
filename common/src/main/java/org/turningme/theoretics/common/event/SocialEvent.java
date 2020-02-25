package org.turningme.theoretics.common.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.turningme.theoretics.common.beans.EUserFrePair;
import org.turningme.theoretics.common.beans.EventUserSimi;
import org.turningme.theoretics.common.beans.SocialMSG;
import org.turningme.theoretics.common.beans.SpaceRange;
import org.turningme.theoretics.common.beans.TimeRange;
import static org.turningme.theoretics.common.Constants.EARTH_RADIUS;
import static org.turningme.theoretics.common.Constants.MAXFLOAT;
import static org.turningme.theoretics.common.Constants.MVALUE;
import static org.turningme.theoretics.common.Constants.PI;
import static org.turningme.theoretics.common.Constants.TFIDF_DIM;

/**
 * Created by jpliu on 2020/2/23.
 *
 *
 */



public class SocialEvent implements Event  , Serializable {


    //float *Cluster_ConceptTFIDFVec;
    float[] Cluster_ConceptTFIDFVec = new float[TFIDF_DIM];
    TimeRange Cluster_TR = new TimeRange();
    SpaceRange Cluster_SR = new SpaceRange();
    List<EUserFrePair> eventUserIdsFre = new ArrayList<>();
    List<SocialMSG> EventMSG = new ArrayList<>();
    int eventno;

    public float EventSimi(SocialEvent  otherEvent){
        SocialMSG thisMSG= new SocialMSG(Cluster_ConceptTFIDFVec, Cluster_TR, Cluster_SR, eventUserIdsFre);
        SocialMSG otherMSG= new SocialMSG(otherEvent.Cluster_ConceptTFIDFVec, otherEvent.Cluster_TR, otherEvent.Cluster_SR, otherEvent.eventUserIdsFre);
        float simi = thisMSG.GetGlobalSimilarity(otherMSG);
        return simi;

    }

    public float[] getCluster_ConceptTFIDFVec() {
        return Cluster_ConceptTFIDFVec;
    }

    public void setCluster_ConceptTFIDFVec(float[] cluster_ConceptTFIDFVec) {
        Cluster_ConceptTFIDFVec = cluster_ConceptTFIDFVec;
    }

    public SpaceRange getCluster_SR() {
        return Cluster_SR;
    }

    public void setCluster_SR(SpaceRange cluster_SR) {
        Cluster_SR = cluster_SR;
    }

    public TimeRange getCluster_TR() {
        return Cluster_TR;
    }

    public void setCluster_TR(TimeRange cluster_TR) {
        Cluster_TR = cluster_TR;
    }

    public List<SocialMSG> getEventMSG() {
        return EventMSG;
    }

    public void setEventMSG(List<SocialMSG> eventMSG) {
        EventMSG = eventMSG;
    }

    public int getEventno() {
        return eventno;
    }

    public void setEventno(int eventno) {
        this.eventno = eventno;
    }

    void SetEventNo(int eno) {
        eventno = eno;
    }

    int GetEventNo() {
        return eventno;
    }

    public List<EUserFrePair> getEventUserIdsFre() {
        return eventUserIdsFre;
    }

    public void setEventUserIdsFre(List<EUserFrePair> eventUserIdsFre) {
        this.eventUserIdsFre = eventUserIdsFre;
    }


    public int[] EHashV = new int[MVALUE]; //leave it for hash-based clustering.
    public int[] EventHashV = null; //leave it for hash-based data partition for parallel processing over spark.
    public List<EventUserSimi> RecUserSimi = new ArrayList<>(); //the user id list with similarity for recommending this event to, for parallel processing over spark only
    public List<Integer> userlist = new ArrayList<>(); //a list of users whose user profile contains this event

    public int[] getEHashV() {
        return EHashV;
    }

    public void setEHashV(int[] EHashV) {
        this.EHashV = EHashV;
    }

    public int[] getEventHashV() {
        return EventHashV;
    }

    public void setEventHashV(int[] eventHashV) {
        EventHashV = eventHashV;
    }

    public List<EventUserSimi> getRecUserSimi() {
        return RecUserSimi;
    }

    public void setRecUserSimi(List<EventUserSimi> recUserSimi) {
        RecUserSimi = recUserSimi;
    }

    public List<Integer> getUserlist() {
        return userlist;
    }

    public void setUserlist(List<Integer> userlist) {
        this.userlist = userlist;
    }

    /**
     * constructor tpl
     */
    public SocialEvent() {
    }


    /**
     * the object field should be private , but not by now , for quick modification
     */
    public void EventReset() {
        for (int i = 0; i < TFIDF_DIM; i++) {
            Cluster_ConceptTFIDFVec[i] = 0;
        }
        Cluster_TR.TimeStampCentre = 0;
        Cluster_TR.range = 0;
        Cluster_SR.lat = 0;
        Cluster_SR.longi = 0;
        Cluster_SR.radius = 0;

        eventUserIdsFre.clear();
        EventMSG.clear();
        EventHashV = null;
        RecUserSimi.clear();
        userlist.clear();
    }

    public int SetEventUserIDs() {
        int usernumber = 0;
        for (SocialMSG smgit : EventMSG) {
            List<EUserFrePair> eUserFrePairs = smgit.getEventUserIDsFre();
            for (EUserFrePair euit : eUserFrePairs) {

                int i;
                for (i = 0; i < eventUserIdsFre.size(); i++) {
                    EUserFrePair eufpit = eventUserIdsFre.get(i);
                    if (eufpit.userid == euit.userid) //found from the eventUserIDsFre
                    {
                        eufpit.frequency += euit.frequency;
                        break;
                    }

                }

                // if all above failed , then insert new one
                if (eventUserIdsFre.size() == i) {
                    EUserFrePair newUFpair = new EUserFrePair(euit.userid, euit.frequency);
                    eventUserIdsFre.add(newUFpair);
                }

            }

        }
        return eventUserIdsFre.size();
    }


    public int SetEventUserIDs_Vec(List<Integer> userlist) {
        int usernumber = userlist.size();
        EUserFrePair upair = new EUserFrePair();
        upair.frequency = 1;
        for (int i = 0; i < usernumber; i++) {
            upair.userid = userlist.get(i);
            eventUserIdsFre.add(upair);
        }
        return usernumber;
    }

    public List<SocialMSG> GetEventMSGs() {
        return EventMSG;
    }

    public List<EUserFrePair> GetEventUserIDs() {
        return eventUserIdsFre;
    }


    public void setConceptTFIDFVec(float[] vec) {
        for (int i = 0; i < TFIDF_DIM; i++) {
            Cluster_ConceptTFIDFVec[i] = vec[i];
        }
    }

    public float GetGlobalSimilarity(SocialMSG smsg)  //get the maximal similarity of smsg to a message in this event
    {
        float Gsim = 0;
        //SocialMSG thisMSG(Cluster_ConceptTFIDFVec, Cluster_TR, Cluster_SR, eventUserIdsFre);

        for (SocialMSG seit : EventMSG
                ) {
            float sim = seit.GetGlobalSimilarity(smsg);
            if (sim > Gsim)
                Gsim = sim;
        }

        return Gsim;
    }


    public TimeRange GetTimeRange() {
        return Cluster_TR;
    }

    public void SetTimeRange() {


        float minT = MAXFLOAT;
        float maxT = 0;

        for (SocialMSG emit : EventMSG) {
            float tmin = emit.getTimeRange().TimeStampCentre - emit.getTimeRange().range;
            float tmax = emit.getTimeRange().TimeStampCentre + emit.getTimeRange().range;
            if (tmin < minT)
                minT = tmin;
            if (tmax > maxT)
                maxT = tmax;
        }
        Cluster_TR.TimeStampCentre = (minT + maxT) / 2;
        Cluster_TR.range = (maxT - minT) / 2;
    }

    public SpaceRange GetSpaceRange() {
        return Cluster_SR;
    }


    public void SetSpaceRange() {
        //find the minimal time point, and maximal time point
        float minLat = MAXFLOAT;
        float minLon = MAXFLOAT;

        float maxLat = 0;
        float maxLon = 0;

        for (SocialMSG emit : EventMSG
                ) {
            float tminLat = emit.getSpaceRange().lat - emit.getSpaceRange().radius;
            float tmaxLat = emit.getSpaceRange().lat + emit.getSpaceRange().radius;
            if (tminLat < minLat)
                minLat = tminLat;
            if (tmaxLat > maxLat)
                maxLat = tmaxLat;

            float tminLon = emit.getSpaceRange().longi - emit.getSpaceRange().radius;
            float tmaxLon = emit.getSpaceRange().longi + emit.getSpaceRange().radius;
            if (tminLon < minLon)
                minLon = tminLon;
            if (tmaxLon > maxLon)
                maxLon = tmaxLon;
        }


        Cluster_SR.lat = (minLat + maxLat) / 2;
        Cluster_SR.longi = (minLon + maxLon) / 2;

        Cluster_SR.radius = (float) get_distance(minLat, minLon, maxLat, maxLon) / 2;
    }


    public void SetCluster_ConceptTFIDFVec() {


        for (int i = 0; i < TFIDF_DIM; i++) {
            Cluster_ConceptTFIDFVec[i] = 0;
        }

        for (SocialMSG emit : EventMSG) {
            for (int i = 0; i < TFIDF_DIM; i++) {
                Cluster_ConceptTFIDFVec[i] += emit.getConceptTFIDFVec()[i];
            }
        }
        for (int i = 0; i < TFIDF_DIM; i++) {
            Cluster_ConceptTFIDFVec[i] /= EventMSG.size();
        }
    }


    public float[] GetCluster_ConceptTFIDFVec() {
        return Cluster_ConceptTFIDFVec;
    }

    public double rad(double d) {
        return d * PI / 180.0;
    }

    public double round(double d) {
        return Math.floor(d + 0.5);
    }

    public double get_distance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = round(s * 10000) / 10000;
        return s;
    }


    public void setTimeRange(TimeRange tr) {
        Cluster_TR.TimeStampCentre = tr.TimeStampCentre;
        Cluster_TR.range = tr.range;
    }

    public void setSpaceRange(SpaceRange sr) {
        Cluster_SR.lat = sr.lat;
        Cluster_SR.longi = sr.longi;
        Cluster_SR.radius = sr.radius;
    }

    public void uploadEventMsg(SocialMSG smg) {
        EventMSG.add(smg);
    }




}
