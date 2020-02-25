package org.turningme.theoretics.common;

/**
 * Created by jpliu on 2020/2/23.
 */
public class Constants {


    /**
     * from event cpp header file
     */
    public static final int FLT_MAX = 5256000;
    public static final int FLT_MIN = 0;

    public static final int  MAXFLOAT =  FLT_MAX;//5256000 //minutes for one year
    public static final int MINFLOAT =  FLT_MIN;
    public static final int MAXINT = Integer.MAX_VALUE;
    public static final int MININT = Integer.MIN_VALUE;
    public static final float ELIPSE =  0.8f;  //social message similarity threshold, can be variable.
    public static final float INFLUENCETHRE = 0.300000001f; //user influence threshold
    public static final int ROWSV = 40;//670  //for c[ROWSV+1][ROWSV+1]


    /**
     * from  SocialMessage cpp header file
     */

    public static final int TFIDF_DIM = 50;
    public static final int SPACERADIUST = 20;
    public static final int TIMERADIUST = 30;
    public static final double PI = 3.14159265358979;
    public static final double  EARTH_RADIUS = 6378.137; //地球半径
    public static final int  BUFFERSIZE = 20000;
    public static final int  USERLEN = 100;
    public static final int  MSGLEN = 200;
    public static final int  MVALUE = 9; //the number of hash functions , old is 5 , but it is 9 while computing , // TODO: 2020/2/25  

    /**
     *
     * from event migration
     */

    public static final int  TUNUM = 14978;//20//10000 //total number of users in training dataset
    public static final double WUIPV = 0.7;

}
