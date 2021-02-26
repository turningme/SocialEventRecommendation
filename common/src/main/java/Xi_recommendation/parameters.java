package Xi_recommendation;

import java.io.Serializable;
import java.util.ArrayList;

public class parameters implements Serializable {
    float ALPHA=0.4f;
    int MAXEVENTNUM=20000; //20  //The maximal number of incoming events in a time slot
    int KUNUM=100;
    float WUIPV=0.7f;
    int TUNUM=135782; //total number of users in training dataset
    int MVALUE=10;
    int TFIDF_DIM=50;
    float MAXFLOAT=0.12f;
    float TIMERADIUST=0.5f;

    //float migrateT=0.001f
    int coupling=0;

    ArrayList<SubEvent> UPEventList=new ArrayList<>();
}
