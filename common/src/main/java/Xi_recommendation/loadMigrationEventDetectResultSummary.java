package Xi_recommendation;

import java.io.*;
import java.util.ArrayList;

import static org.turningme.theoretics.common.Constants.MVALUE;
import static org.turningme.theoretics.common.Constants.TFIDF_DIM;

public class loadMigrationEventDetectResultSummary {

    public static int loadMigrationEventDetectResultSummary(String filename, ArrayList<SubEvent> eventClusters) throws IOException {
        int ret = 0;

        float[] vec = new float[TFIDF_DIM];

        TimeRange newTR = new TimeRange();
        SpaceRange newSR = new SpaceRange();
        ArrayList<EUserFrePair> euids = new ArrayList<>();
//        ArrayList<EUserFrePair> euit=new ArrayList<>();

        String line;
        SubEvent curEvent = new SubEvent();
        int eid = 0;

        if (new File(filename).exists()) {
            BufferedReader f_open = new BufferedReader(new FileReader(filename));
            line = f_open.readLine();
            while (line != null) {
                if (line.length() <= 0) {
                    System.out.println("Invalid (empty) line!\n");
                    return 1;
                }
                curEvent.SetEventNo(Integer.parseInt(line.substring(line.indexOf("<clusterid>") + 11, line.indexOf("</clusterid>")).trim()));

                String[] tfidfstr = line.substring(line.indexOf("<msgcontfidf>") + 13, line.indexOf("</msgcontfidf>")).trim().split(" ");
                for (int i = 0; i < tfidfstr.length; i++) {
                    vec[i] = Float.parseFloat(tfidfstr[i]);
                }
                curEvent.setConceptTFIDFVec(vec);

                String[] Hashvalues = line.substring(line.indexOf("<HashkeyValues>") + 15, line.indexOf("</HashkeyValues>")).trim().split(" ");
                for (int i = 0; i < Hashvalues.length; i++) {
                    for (int j = 0; j < MVALUE; j++) {
                        curEvent.EHashV.set(j, Integer.parseInt(Hashvalues[i]));
                    }
                }

                String[] SpaceRange = line.substring(line.indexOf("<SpaceRange>") + 12, line.indexOf("</SpaceRange>")).trim().split(" ");
                newSR.lat = Float.parseFloat(SpaceRange[0]);
                newSR.longi = Float.parseFloat(SpaceRange[1]);
                newSR.radius = Float.parseFloat(SpaceRange[2]);
                curEvent.setSpaceRange(newSR);

                String[] TimeRange = line.substring(line.indexOf("<TimeRange>") + 11, line.indexOf("</TimeRange>")).trim().split(" ");
                newTR.TimeStampCentre = Float.parseFloat(TimeRange[0]);
                newTR.range = Float.parseFloat(TimeRange[1]);
                curEvent.setTimeRange(newTR);

                String[] SpaceRangeSet = line.substring(line.indexOf("<SpaceRangeSet>") + 15, line.indexOf("</SpaceRangeSet>")).trim().split(" ");
                SpaceRange srtmp = new SpaceRange();
                srtmp.lat = Float.parseFloat(SpaceRangeSet[0]);
                srtmp.longi = Float.parseFloat(SpaceRangeSet[1]);
                srtmp.radius = Float.parseFloat(SpaceRangeSet[2]);
                curEvent.uploadmsgSRset(srtmp);

                String[] TimeRangeSet = line.substring(line.indexOf("<TimeRangeSet>") + 14, line.indexOf("</TimeRangeSet>")).trim().split(" ");
                TimeRange trtmp = new TimeRange();
                trtmp.TimeStampCentre = Float.parseFloat(TimeRangeSet[0]);
                trtmp.range = Float.parseFloat(TimeRangeSet[1]);
                curEvent.uploadmsgTRset(trtmp);

                String[] useridlist = line.substring(line.indexOf("<useridlist>") + 12, line.indexOf("</useridlist>")).trim().split(" ");
                for (int i = 0; i < useridlist.length; i++) {

                    EUserFrePair eufPair = new EUserFrePair();
                    eufPair.userid = Integer.parseInt(useridlist[i].split(" ")[0]);
                    eufPair.frequency = Integer.parseInt(useridlist[i].split(" ")[1]);

                    int euit = 0;
                    while (euit < euids.size()) {
                        if (euids.get(euit).userid > eufPair.userid) {
                            euids.set(euit, eufPair);
                            euit = 0;
                            break;
                        }
                        euit++;
                    }
                    if (euit == euids.size() - 1)
                        euids.add(eufPair);
                }

                curEvent.setEventUserIDs(euids);
                euids.clear();

                eventClusters.add(curEvent);
//                i++;
                //reset curEvent;
                curEvent.EventReset();
                line = f_open.readLine();
            }
            f_open.close();
        }
        ret = eventClusters.size();
        return ret;
    }
}
