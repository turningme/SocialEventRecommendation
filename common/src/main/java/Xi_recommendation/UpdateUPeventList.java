package Xi_recommendation;

import java.util.ArrayList;
import java.util.List;

public class UpdateUPeventList {

//    public int UpdateUPeventList(ArrayList<SubEvent> incomingEvents, int AddorDel, int DeleteNum) {
//        int ret = 0;
//        if (AddorDel!=0) {
//            EventMigrationRecom.UPEventList.addAll(incomingEvents);
//        } else {
//            // erase the first DeleteNum elements:
//            for (int i = 0; i < DeleteNum; i++) {
//                EventMigrationRecom.UPEventList.remove(i);
//            }
//            for (int i = 0; i < EventMigrationRecom.UPEventList.size(); i++) {
//                EventMigrationRecom.UPEventList.get(i).SetEventNo(EventMigrationRecom.UPEventList.get(i).GetEventNo() - DeleteNum);
//            }
//
//            for (int i = 0; i < EventMigrationRecom.UserProfileHashMap.size(); i++) {
//                List<SubEvent> eit = EventMigrationRecom.UserProfileHashMap.get(i).GetUserInterestEvents();
//                int offset = 0;
//
//                for (int j = 0; j < eit.size(); j++) {
//                    if (eit.get(j).GetEventNo() < DeleteNum) {
//                        EventMigrationRecom.UserProfileHashMap.get(i).GetUserInterestEvents().remove(j);
//                        j = j + offset;
//                    } else {
//                        eit.get(j).SetEventNo(eit.get(j).GetEventNo() - DeleteNum);
//                        offset += 1;
//                    }
//                }
//            }
//        }
//        ret = EventMigrationRecom.UPEventList.size();
//        return ret;
//    }

    public int UpdateUPeventList(EventMigrationRecom eventMigrationRecom, ArrayList<SubEvent> incomingEvents, int AddorDel, int DeleteNum) {
        int ret = 0;
        if (AddorDel != 0) {
            eventMigrationRecom.UPEventList.addAll(incomingEvents);
        } else {
            // erase the first DeleteNum elements:
            for (int i = 0; i < DeleteNum; i++) {
                eventMigrationRecom.UPEventList.remove(i);
            }
            for (int i = 0; i < eventMigrationRecom.UPEventList.size(); i++) {
                eventMigrationRecom.UPEventList.get(i).SetEventNo(eventMigrationRecom.UPEventList.get(i).GetEventNo() - DeleteNum);
            }

            for (int i = 0; i < eventMigrationRecom.UserProfileHashMap.size(); i++) {
                List<SubEvent> eit = eventMigrationRecom.UserProfileHashMap.get(i).GetUserInterestEvents();
                int offset = 0;

                for (int j = 0; j < eit.size(); j++) {
                    if (eit.get(j).GetEventNo() < DeleteNum) {
                        eventMigrationRecom.UserProfileHashMap.get(i).GetUserInterestEvents().remove(j);
                        j = j + offset;
                    } else {
                        eit.get(j).SetEventNo(eit.get(j).GetEventNo() - DeleteNum);
                        offset += 1;
                    }
                }
            }
        }
        ret = eventMigrationRecom.UPEventList.size();
        return ret;
    }

}
