package Xi_recommendation;

import java.io.Serializable;
import java.util.ArrayList;

public class parameters implements Serializable {
    public static float ALPHA=0.4f;
    public static int MAXEVENTNUM=20000; //20  //The maximal number of incoming events in a time slot
    public static int KUNUM=100;
    public static float WUIPV=0.7f;
    public static int TUNUM=135782; //total number of users in training dataset
    public static int MVALUE=10;
    public static int TFIDF_DIM=50;
    public static float MAXFLOAT=0.12f;
    public static float TIMERADIUST=0.5f;

    //float migrateT=0.001f
    public static int coupling=0;

    ArrayList<SubEvent> UPEventList=new ArrayList<>();
    public static float omeg1=0.1f;
    public static float omeg2=0.1f;

    public static int TOPK=10;
}
