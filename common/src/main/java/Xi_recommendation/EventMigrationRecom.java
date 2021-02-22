package Xi_recommendation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author helen ding on 26/09/2020.
 */
public class EventMigrationRecom {

    public List<SubEvent> UPEventList = new ArrayList<>(); //store the events in user profiles. user profiles only keep the event
    // no, not the attributes of events
    public static Map<Integer, UserProfile> UserProfileHashMap = new HashMap<>();
//    List<String> GTHashtagHashTable[HTSize]; //keep event related hashtags
//    List<strFre> DocHashtagHashTable[HTSize]; //keep hashtag in a time slot

    public static float ELIPSE;//social message similarity threshold, can be variable. Get value from main parameter
    // list
    public static float SPACERADIUST;
    public static float TIMERADIUST;
    public static float omeg1; //concept net weight
    public static float omeg2; //time weight, then the weight of location is 1-omeg1-omeg2
    public static int MethodChoice;

}
