package org.turningme.theoretics.api;

import Xi_recommendation.EUserFrePair;
import Xi_recommendation.SpaceRange;
import Xi_recommendation.SubEvent;
import Xi_recommendation.TimeRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.LineRecordReader;
import org.apache.hadoop.mapred.RecordReader;

/**
 * @author jpliu
 */
public class IncomingSubEventRecordReader implements RecordReader<LongWritable, SubEvent> {
    private LineRecordReader lineRecordReader;
    private LongWritable currentKeyHolder = new LongWritable();
    private Text currentValueHolder = new Text();
    private boolean stop ;
    private int StartClusterNo = 0;


    public IncomingSubEventRecordReader(Configuration job, FileSplit split) throws IOException {
        stop = false;
        // invalid file name style
        if (split.getPath().getName().contains("DS")){
            stop =true;
        }
        this.lineRecordReader = new LineRecordReader(job, split);
    }
    @Override
    public boolean next(LongWritable longWritable, SubEvent curEvent) throws IOException {
        //do not read stop stamped file
        if (stop) {
            return false;
        }

        boolean success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success){
            String line =  this.currentValueHolder.toString();
            if (line.contains("<clusterid>")) {
                curEvent.EventReset();

                String value = line.substring(line.indexOf("<clusterid>") + 11, line.indexOf("</clusterid>")).trim();
                curEvent.SetEventNo(Integer.valueOf(value) - StartClusterNo);
                longWritable.set(curEvent.GetEventNo());

                 success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                if (success){
                    if (line.contains("<msgcontfidf>")) {
                        line =  this.currentValueHolder.toString();
                        String[] values = line.substring(line.indexOf("<msgcontfidf>") + 13, line.indexOf("</msgcontfidf>")).trim().split("\t");
                        for (int i = 0; i < values.length; i++) {
                            curEvent.addConceptTFIDFVec(Float.parseFloat(values[i]));
                        }

                        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                        if (success){
                            line =  this.currentValueHolder.toString();
                            if (line.contains("<SpaceRange>")) {
                                SpaceRange newSR = new SpaceRange();
                                String[] valuess = line.substring(line.indexOf("<SpaceRange>") + 12, line.indexOf("</SpaceRange>")).trim().split("\t");
                                newSR.lat = Float.valueOf(valuess[0]);
                                newSR.longi = Float.valueOf(valuess[1]);
                                newSR.radius = Float.valueOf(valuess[2]);
                                curEvent.Cluster_SR = newSR;


                                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                                if (success) {
                                    line = this.currentValueHolder.toString();
                                    if (line.contains("<TimeRange>")) {
                                        String[] value2 = line.substring(line.indexOf("<TimeRange>") + 11, line.indexOf("</TimeRange>")).trim().split("\t");
                                        TimeRange newTR = new TimeRange();
                                        newTR.TimeStampCentre = Float.valueOf(value2[0]);
                                        newTR.range = Float.valueOf(value2[1]);
                                        curEvent.setTimeRange(newTR);


                                        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                                        if (success) {
                                            line = this.currentValueHolder.toString();

                                            if (line.contains("<SpaceRangeSet>")) {
                                                String[] SpaceRangeSet = line.substring(line.indexOf("<SpaceRangeSet>") + 15, line.indexOf("</SpaceRangeSet>")).trim().split(" ");
                                                SpaceRange srtmp = new SpaceRange();
                                                srtmp.lat = Float.parseFloat(SpaceRangeSet[0]);
                                                srtmp.longi = Float.parseFloat(SpaceRangeSet[1]);
                                                curEvent.uploadmsgSRset(srtmp);

                                                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                                                if (success) {
                                                    line = this.currentValueHolder.toString();
                                                    if (line.contains("<TimeRangeSet>")) {
                                                        String[] TimeRangeSet = line.substring(line.indexOf("<TimeRangeSet>") + 14, line.indexOf("</TimeRangeSet>")).trim().split(" ");
                                                        TimeRange trtmp = new TimeRange();
                                                        trtmp.TimeStampCentre = Float.parseFloat(TimeRangeSet[0]);
                                                        trtmp.range = Float.parseFloat(TimeRangeSet[1].split("\t")[0]);
                                                        curEvent.uploadmsgTRset(trtmp);


                                                        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                                                        if (success) {
                                                            line = this.currentValueHolder.toString();
                                                            if (line.contains("<useridlist>")) {
                                                                List<EUserFrePair> euids = new ArrayList();
                                                                String[] value3 = line.substring(line.indexOf("<useridlist>") + 12, line.indexOf("</useridlist>")).trim().split("\t");
                                                                for (int i = 0; i < value3.length; i++) {
                                                                    EUserFrePair eufPair = new EUserFrePair();
                                                                    eufPair.userid = Integer.valueOf(value3[i].split(" ")[0]);
                                                                    eufPair.frequency = Integer.valueOf(value3[i].split(" ")[1]);
                                                                    euids.add(eufPair);


                                                                    int euit = 0;
                                                                    while (euit < euids.size()) {
                                                                        if (euids.get(euit).userid > eufPair.userid) {
                                                                            euids.set(euit, eufPair);
                                                                            euit = 0;
                                                                            break;
                                                                        }
                                                                        euit++;
                                                                    }
                                                                    if (euit == euids.size() - 1){
                                                                        euids.add(eufPair);
                                                                    }

                                                                }
                                                                curEvent.setEventUserIDs(euids);
                                                            }
                                                        }

                                                    }
                                                }


                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

            }
        }




        return  success;
    }

    @Override
    public LongWritable createKey() {
        return new LongWritable();
    }

    @Override
    public SubEvent createValue() {
        return new SubEvent();
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
