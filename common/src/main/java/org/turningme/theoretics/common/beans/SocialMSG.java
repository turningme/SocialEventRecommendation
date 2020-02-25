package org.turningme.theoretics.common.beans;

import java.util.ArrayList;
import java.util.List;

import static org.turningme.theoretics.common.Constants.MVALUE;
import static org.turningme.theoretics.common.Constants.PI;
import static org.turningme.theoretics.common.Constants.TFIDF_DIM;

/**
 * Created by jpliu on 2020/2/23.
 */
public class SocialMSG {
    public int msgID;
    public float[] ConceptTFIDFVec = new float[TFIDF_DIM];
    public TimeRange TR = new TimeRange();
    public SpaceRange SR = new SpaceRange();  //
    public List<EUserFrePair> EventUserIDsFre = new ArrayList<>();  // a number of user-event interaction recorders, here users are those attached with this message

    //int *HashV=NULL; //leave it for hash-based clustering.
    public int[] HashV = new int[MVALUE]; //leave it for hash-based clustering.
    public int conflict; //leave it for hash-based clustering
    public int hashtaged;  //for keep record on if it is hashtag


    public SocialMSG() {

    }

    public SocialMSG(float[] vec, TimeRange tr, SpaceRange sr, List<EUserFrePair> euids) {
        //HashV = NULL;
        //ConceptTFIDFVec = new float[TFIDF_DIM];
        for (int i = 0; i < TFIDF_DIM; i++) {
            ConceptTFIDFVec[i] = vec[i];
        }

        TR.TimeStampCentre = tr.TimeStampCentre;
        TR.range = tr.range;
        SR.lat = sr.lat;
        SR.longi = sr.longi;
        SR.radius = sr.radius;
        hashtaged = 0;
        msgID = 0;


        for (EUserFrePair ele : euids) {
            EventUserIDsFre.add(ele);
        }


        //		.userid].userid = (*eit).userid;
        //EventUserIDsFre[(*eit).userid].frequency = (*eit).frequency;

        //	EventUserIDsFre[(*eit).userid].userid = (*eit).userid;
        //	EventUserIDsFre[(*eit).userid].frequency = (*eit).frequency;


    }


    /*
    void setConceptTFIDFVec(float * vec) {
        ConceptTFIDFVec = new float[TFIDF_DIM];
        for (int i = 0; i < TFIDF_DIM; i++) {
            ConceptTFIDFVec[i] = vec[i];
        }
    }

    void setTimeRange(TimeRange tr) {
        TR.TimeStampCentre = tr.TimeStampCentre;
        TR.range = tr.range;
    }

    void setSpaceRange(SpaceRange sr) {
        SR.lat = sr.lat;
        SR.longi = sr.longi;
        SR.radius = sr.radius;
    }

    void setEUID(std::vector<EUserFrePair> euids) {
        std::vector<EUserFrePair>::iterator eit = euids.begin();
        while (eit != euids.end()) {
            EventUserIDsFre[(*eit).userid].userid = (*eit).userid;
            EventUserIDsFre[(*eit).userid].frequency = (*eit).frequency;
            eit++;
        }
    }
    */

    public void setMSGID(int mid) {
        msgID = mid;
    }

    public int getMSGID() {
        return msgID;
    }

    public List<EUserFrePair> getEventUserIDsFre() {
        return EventUserIDsFre;
    }

    public float[] getConceptTFIDFVec() {
        return ConceptTFIDFVec;
    }

    public TimeRange getTimeRange() {
        return TR;
    }

    public SpaceRange getSpaceRange() {
        return SR;
    }

    public float CVModule() {
        double ret = 0;
        for (int i = 0; i < TFIDF_DIM; i++) {
            ret = ret + ConceptTFIDFVec[i] * ConceptTFIDFVec[i];
        }
        return (float) Math.sqrt(ret);
    }

    public float GetConceptSimilarity(SocialMSG smsg) {
        float simi = 0;
        for (int i = 0; i < TFIDF_DIM; i++) {
            simi += ConceptTFIDFVec[i] * ConceptTFIDFVec[i];
        }
        float thisModule = CVModule();
        float smsgModule = smsg.CVModule();
        simi /= thisModule;
        simi /= smsgModule;

        return simi;
    }

    public float GetTimeSimilarity(SocialMSG smsg) {
        float Tsim;

        SocialMSG maxPtr, minPtr;
        if (TR.TimeStampCentre >= smsg.TR.TimeStampCentre) {
            maxPtr = this;
            minPtr = smsg;
        } else {
            maxPtr = smsg;
            minPtr = this;
        }

        if (maxPtr.TR.TimeStampCentre - minPtr.TR.TimeStampCentre >= 2 * maxPtr.TR.range)
            Tsim = 0;
        else {
            float intersectT = (minPtr.TR.TimeStampCentre + minPtr.TR.range) - (maxPtr.TR.TimeStampCentre - maxPtr.TR.range);
            float unionT = (maxPtr.TR.TimeStampCentre + maxPtr.TR.range) - (minPtr.TR.TimeStampCentre - minPtr.TR.range);
            Tsim = intersectT / unionT;
        }
        return Tsim;
    }

    //以下是直接封装好的函数，六个参数，意思分别是第一个圆的圆心，半径，第二个圆的圆心，半径，返回相交部分的面积，如果不相交，则返回零。
    public float intersect(float x1, float y1, float r1, float x2, float y2, float r2) {
        float s, temp, p, l, ans;
        l = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        if (l >= r1 + r2) ans = 0;
        else if (l <= Math.abs(r1 - r2)) {
            if (r1 <= r2) ans = (float) PI * r1 * r1;
            else ans = (float) PI * r2 * r2;
        } else {
            p = (l + r1 + r2) / 2;
            s = (float) (2 * Math.sqrt(p * (p - l) * (p - r1) * (p - r2)));
            if (r1 > r2) {
                temp = x1;
                x1 = x2;
                x2 = temp;
                temp = y1;
                y1 = y2;
                y2 = temp;
                temp = r1;
                r1 = r2;
                r2 = temp;
            }
            ans = (float) (Math.acos((r1 * r1 + l * l - r2 * r2) / (2 * r1 * l)) * r1 * r1 + Math.acos(
                    (r2 * r2 + l * l - r1 * r1) / (2 * r2 * l)) * r2 * r2 - s);
        }
        return ans;
    }

    public float getCircleArea(float r) {
        float ret;
        ret = (float) PI * r * r;
        return ret;
    }

    public float GetSpaceSimilarity(SocialMSG smsg) {
        float Ssim;

        float inters = intersect(SR.lat, SR.longi, SR.radius, smsg.SR.lat, smsg.SR.longi, smsg.SR.radius);
        float unionV = 2 * getCircleArea(SR.radius) - inters;

        Ssim = inters / unionV;

        return Ssim;
    }

    public float GetGlobalSimilarity(SocialMSG smsg) {
        float Gsim = 0;
        Gsim = GetConceptSimilarity(smsg) * GetTimeSimilarity(smsg) * GetSpaceSimilarity(smsg);
        return Gsim;
    }

    //for EventRecomOpti.cpp
    public float GetConceptVectorSimilarity(float[] con1, float[] con2) {
        float simi = 0;
        for (int i = 0; i < TFIDF_DIM; i++) {
            simi += con1[i] * con2[i];
        }
        float thisModule = 0;
        for (int i = 0; i < TFIDF_DIM; i++) {
            thisModule += con1[i] * con1[i];
        }
        thisModule = (float) Math.sqrt(thisModule);

        float smsgModule = 0;
        for (int i = 0; i < TFIDF_DIM; i++) {
            smsgModule += con2[i] * con2[i];
        }
        smsgModule = (float) Math.sqrt(smsgModule);

        simi /= thisModule;
        simi /= smsgModule;

        return simi;
    }
    ///////////////////////////////


    @Override
    public  void finalize() throws Throwable {
                /*delete ConceptTFIDFVec;
        if (HashV != NULL)
            delete HashV;*/
        EventUserIDsFre.clear();
    }

}
