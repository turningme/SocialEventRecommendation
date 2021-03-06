package Xi_recommendation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
*@authorXi Chen 02-Oct-2020
 */

public class EventRecommendation  implements Serializable {

    parameters para=new parameters();

    private int MAXEVENTNUM= parameters.MAXEVENTNUM; //20  //The maximal number of incoming events in a time slot
    private double ALPHA= parameters.ALPHA;
    private int KUNUM= parameters.KUNUM;


    public ArrayList<SubEvent> UPEventList=new ArrayList<>();


//    private int MAXEVENTNUM=20000; //20  //The maximal number of incoming events in a time slot
//    private double ALPHA=0.4;
//    private int KUNUM=100;



    int curEventNum; //the number of incoming events in the current time slot
    private float alpha;

//    public EventRecommendation(float alpha, int size) {
//    }

//    public EventRecommendation(){}


    public class RecItem implements Serializable{  //remember: recommend a given item to users, USERID should be output
        public int UserID;
        public float simi;
    }

    public EventRecommendation(float alphaV, int curEnum) {
        ALPHA = alphaV; //the weight of ESim
        curEventNum=curEnum;
    }

//    public void EventRecommendation() {}

    public void setcurEventNum(int curEnum) {
        curEventNum = curEnum;
    }

    public int getEventNum() { return curEventNum; }

    //matching two events
    private float GetESim(ArrayList<SubEvent> En, ArrayList<SubEvent> Eu) {
        return 0;
    }

    public float GetESim(SubEvent En, SubEvent Eu, ArrayList<UserProfile> UserProfileHashMap){
        EventMigration emig=new EventMigration();
        float probr=EventMigration.EventMigrationProb(En, Eu, UserProfileHashMap);
        if (probr > 0)
            //if(probr==1)
            System.out.println("probr=%f\t"+ probr+ "EventID=%d\t"+ En.GetEventNo()+" UserEventID=%d\n"+ Eu.GetEventNo());
        float Sim = En.EventSimi(Eu);
        float overallsim = (1 - alpha)* Sim + alpha *probr;
        return overallsim;
    };

    public float GetESimV2(SubEvent En, SubEvent Eu, Map<Long,UserProfile> UserProfileHashMap){
        EventMigration emig=new EventMigration();
        float probr=EventMigration.EventMigrationProbV2(En, Eu, UserProfileHashMap);
        if (probr > 0)
            //if(probr==1)
            System.out.println("probr=%f\t"+ probr+ "EventID=%d\t"+ En.GetEventNo()+" UserEventID=%d\n"+ Eu.GetEventNo());
        float Sim = En.EventSimi(Eu);
        float overallsim = (1 - alpha)* Sim + alpha *probr;
        return overallsim;
    };

    //matching an incoming event with a user profile
    private float GetESimUser(ArrayList<SubEvent> En, ArrayList<UserProfile> up){return 0;};

    private float GetIntraSjIa(int gjattrJx_eventlistSize, int gjattrJy_eventlistSize){return 0;};

    private int Get_gjx_eventlist_Size(int j, float x){return 0;};

    private float GetProbEventClusters(int EnNo, int EuNo){return 0;};
    private float GetProbUserEvent(ArrayList<SubEvent> En, int EnNo, ArrayList<UserProfile> up){return 0;};


    public float GetESimUser(SubEvent En, UserProfile up) {
        float ESim = 0;

        for (SubEvent eit : up.UserInterestEvents) {
            //float overallsim = GetESim(En, (*eit));
            //ignore the events in the current time slot
            if (eit.GetEventNo() > curEventNum) {
                continue;
            }
            //=================================
            //float overallsim = GetESim(En, UPEventList[(*eit).GetEventNo()]);
            //use pre stored similarity
            float overallsim = UPEventList.get((eit).GetEventNo()).HistEventSimilarity;
            if (overallsim > ESim){
                ESim = overallsim;
            }
        }
        return ESim;
    }

    public float GetESimUserV2(SubEvent En, UserProfile up,SubEvent HistoryEvent) {
        float ESim = 0;

        for (SubEvent eit : up.UserInterestEvents) {
            //float overallsim = GetESim(En, (*eit));
            //ignore the events in the current time slot
            if (eit.GetEventNo() > curEventNum) {
                continue;
            }
            //=================================
            //float overallsim = GetESim(En, UPEventList[(*eit).GetEventNo()]);
            //use pre stored similarity
            if (HistoryEvent.GetEventNo() == eit.GetEventNo()){
                float overallsim = HistoryEvent.HistEventSimilarity;
                if (overallsim > ESim){
                    ESim = overallsim;
                }
            }

        }
        return ESim;
    }
}
