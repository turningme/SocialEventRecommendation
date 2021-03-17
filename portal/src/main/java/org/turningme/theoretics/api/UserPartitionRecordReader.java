package org.turningme.theoretics.api;

import Xi_recommendation.EUserFrePair;
import Xi_recommendation.SpaceRange;
import Xi_recommendation.SubEvent;
import Xi_recommendation.TimeRange;
import Xi_recommendation.UPEventPartition;
import Xi_recommendation.UserValueRangePair;
import Xi_recommendation.ValueRangePair;
import Xi_recommendation.parameters;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.LineRecordReader;
import org.apache.hadoop.mapred.RecordReader;

/**
 * @author jpliu
 */
public class UserPartitionRecordReader implements RecordReader<LongWritable, UPEventPartition> {
    private LineRecordReader lineRecordReader;
    private LongWritable currentKeyHolder = new LongWritable();
    private Text currentValueHolder = new Text();
    private String line;
    private boolean needRead;


    public UserPartitionRecordReader(Configuration job, FileSplit split) throws IOException {
        this.lineRecordReader = new LineRecordReader(job, split);
        needRead = true;
    }

    @Override
    public boolean next(LongWritable longWritable, UPEventPartition userProfile) throws IOException {
        String[] tr = null;
        boolean success = false;
        if (needRead) {
            success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        }

        if (success || !needRead) {
            if (needRead) {
                needRead = false;
            }
            line = this.currentValueHolder.toString();
            if (line.contains("<groupid>")) {
                UPEventPartition group = new UPEventPartition();
                ArrayList<SubEvent> subevents = new ArrayList<SubEvent>();

                int groupid = Integer.parseInt(line.substring(line.indexOf("<groupid>") + 9, line.indexOf("</groupid>")).trim());
//                group.SetGroupId(groupid);

                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                if (success) {
                    line = this.currentValueHolder.toString();
                    tr = line.substring(line.indexOf("<TopicRange>") + 12, line.indexOf("</TopicRange>")).trim().split("\t");
                    ValueRangePair[] topicrangevec = new ValueRangePair[parameters.TFIDF_DIM];
//                ArrayList<float[]> topicrange=new ArrayList<>();
                    for (int i = 0; i < tr.length; i++) {
                        float[] temp = new float[2];
                        ValueRangePair vrp = new ValueRangePair();
                        vrp.minV = Float.parseFloat(tr[i].split(" ")[0]);
                        vrp.maxV = Float.parseFloat(tr[i].split(" ")[1]);
                        topicrangevec[i] = vrp;
                    }
                    group.setTopicRangeVector(topicrangevec);
                }

                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                if (success) {
                    line = this.currentValueHolder.toString();
                    ValueRangePair timerange = new ValueRangePair();
                    String[] timereangepair = line.substring(line.indexOf("<TimeRangePair>") + 15, line.indexOf("</TimeRangePair>")).trim().split(
                            "\t");
                    timerange.minV = Float.parseFloat(timereangepair[0]);
                    timerange.maxV = Float.parseFloat(timereangepair[1]);
                    group.setTimeRangePair(timerange);
                }


                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                if (success) {
                    line = this.currentValueHolder.toString();
                    ValueRangePair[] spacerangevec = new ValueRangePair[2];
                    String[] spacereangepair = line.substring(line.indexOf("<SpaceRangePair>") + 16, line.indexOf("</SpaceRangePair>")).trim().split(
                            "\t");
                    for (int i = 0; i < 2; i++) {
                        spacerangevec[i].minV = Float.parseFloat(spacereangepair[i].split(" ")[0]);
                        spacerangevec[i].maxV = Float.parseFloat(spacereangepair[i].split(" ")[1]);
                    }

                    group.setSpaceRangePair(spacerangevec);
                }

                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                if (success) {
                    line = this.currentValueHolder.toString();
                    String[] infvecs = line.substring(line.indexOf("<influenceRangeVec>") + 19, line.indexOf("</influenceRangeVec>")).trim().split(
                            "\t");
                    ArrayList<UserValueRangePair> infvec = new ArrayList<>();

                    for (String s : tr) {
//                    float[] temp=new float[3];
                        UserValueRangePair temp = new UserValueRangePair();
                        temp.userid = Integer.parseInt(s.split(" ")[0]);
                        temp.minV = Float.parseFloat(s.split(" ")[1]);
                        temp.maxV = Float.parseFloat(s.split(" ")[2]);
                        infvec.add(temp);
                    }
                    group.setInfluenceRangeVector(infvec);
                }

                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                if (success) {
                    line = this.currentValueHolder.toString();
                    String[] un = line.substring(line.indexOf("<MaxMinUN>") + 10, line.indexOf("</MaxMinUN>")).trim().split("\t");
                    ValueRangePair UN = new ValueRangePair();
                    UN.minV = Integer.parseInt(un[0]);
                    UN.maxV = Integer.parseInt(un[1]);
                    group.SetUN(UN);
                }

                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                if (success) {
                    line = this.currentValueHolder.toString();
                    while (line.contains("<clusterid")) {
                        SubEvent curevent = new SubEvent();
                        curevent = parseSubEvent(line);
                        subevents.add(curevent);

                        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                        if (success) {
                            line = this.currentValueHolder.toString();
                        } else {
                            break;
                        }
                    }
                }

            }
        }
        return success;
    }

    private SubEvent parseSubEvent(String line) throws IOException {
        SubEvent curEvent = new SubEvent();
        float[] vec = new float[50];
        TimeRange newTR = new TimeRange();
        SpaceRange newSR = new SpaceRange();
        ArrayList<EUserFrePair> euids = new ArrayList<>();

        if (line.contains("<clusterid>")) {
            curEvent.SetEventNo(Integer.parseInt(line.substring(line.indexOf("<clusterid>") + 11, line.indexOf("</clusterid>")).trim()));
        }

        boolean success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success) {
            line = this.currentValueHolder.toString();
            if (line.contains("<msgcontfidf>")) {
//                    System.out.println(line.substring(line.indexOf("<msgcontfidf>") + 13, line.indexOf("</msgcontfidf>")).trim());
                String[] tfidfstr = line.substring(line.indexOf("<msgcontfidf>") + 13, line.indexOf("</msgcontfidf>")).trim().split("\t");
                for (int i = 0; i < tfidfstr.length; i++) {
                    vec[i] = Float.parseFloat(tfidfstr[i]);
                }
                curEvent.setConceptTFIDFVec(vec);
            }
        }


        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success) {
            line = this.currentValueHolder.toString();
            if (line.contains("<HashkeyValues>")) {
                String[] Hashvalues = line.substring(line.indexOf("<HashkeyValues>") + 15, line.indexOf("</HashkeyValues>")).trim().split(" ");
                for (int i = 0; i < Hashvalues.length; i++) {
                    for (int j = 0; j < parameters.MVALUE; j++) {
                        curEvent.EHashV.set(j, Integer.parseInt(Hashvalues[i]));
                    }
                }
            }
        }


        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success) {
            line = this.currentValueHolder.toString();
            if (line.contains("<SpaceRange>")) {
                String[] SpaceRange = line.substring(line.indexOf("<SpaceRange>") + 12, line.indexOf("</SpaceRange>")).trim().split("\t");
                newSR.lat = Float.parseFloat(SpaceRange[0]);
                newSR.longi = Float.parseFloat(SpaceRange[1]);
                newSR.radius = Float.parseFloat(SpaceRange[2]);
                curEvent.setSpaceRange(newSR);
            }

        }


        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success) {
            line = this.currentValueHolder.toString();
            if (line.contains("<TimeRange>")) {
                String[] TimeRange = line.substring(line.indexOf("<TimeRange>") + 11, line.indexOf("</TimeRange>")).trim().split("\t");
                newTR.TimeStampCentre = Float.parseFloat(TimeRange[0]);
                newTR.range = Float.parseFloat(TimeRange[1]);
                curEvent.setTimeRange(newTR);
            }
        }


        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success) {
            line = this.currentValueHolder.toString();
            if (line.contains("<SpaceRangeSet>")) {
                String[] SpaceRangeSet = line.substring(line.indexOf("<SpaceRangeSet>") + 15, line.indexOf("</SpaceRangeSet>")).trim().split(" ");
                SpaceRange srtmp = new SpaceRange();
                srtmp.lat = Float.parseFloat(SpaceRangeSet[0]);
                srtmp.longi = Float.parseFloat(SpaceRangeSet[1]);
//                    srtmp.radius = Float.parseFloat(SpaceRangeSet[2]);
                curEvent.uploadmsgSRset(srtmp);
            }
        }


        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success) {
            line = this.currentValueHolder.toString();

            if (line.contains("<TimeRangeSet>")) {
                String[] TimeRangeSet = line.substring(line.indexOf("<TimeRangeSet>") + 14, line.indexOf("</TimeRangeSet>")).trim().split(" ");
                TimeRange trtmp = new TimeRange();
                trtmp.TimeStampCentre = Float.parseFloat(TimeRangeSet[0]);
                trtmp.range = Float.parseFloat(TimeRangeSet[1].split("\t")[0]);
                curEvent.uploadmsgTRset(trtmp);
            }
        }


        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success) {
            line = this.currentValueHolder.toString();

            if (line.contains("<useridlist>")) {
                String[] useridlist = line.substring(line.indexOf("<useridlist>") + 12, line.indexOf("</useridlist>")).trim().split("\t");
                for (int i = 0; i < useridlist.length; i++) {

                    EUserFrePair eufPair = new EUserFrePair();
                    eufPair.userid = Integer.parseInt(useridlist[i].split(" ")[0]);
                    eufPair.frequency = Integer.parseInt(useridlist[i].split(" ")[1]);

                    int euit = 0;
                    while (euit <= euids.size()) {
                        if (euids.get(euit).userid > eufPair.userid) {
                            euids.set(euit, eufPair);
                            euit = 0;
                            break;
                        }
                        euit++;
                    }
                    if (euit == euids.size() - 1) {
                        euids.add(eufPair);
                    }

                }
            }
            curEvent.setEventUserIDs(euids);
        }

        return curEvent;
    }


    @Override
    public LongWritable createKey() {
        return new LongWritable();
    }

    @Override
    public UPEventPartition createValue() {
        return new UPEventPartition();
    }

    @Override
    public long getPos() throws IOException {
        return this.lineRecordReader.getPos();
    }

    @Override
    public void close() throws IOException {
        this.lineRecordReader.close();
    }

    @Override
    public float getProgress() throws IOException {
        return this.lineRecordReader.getProgress();
    }
}
