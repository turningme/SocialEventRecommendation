package org.turningme.theoretics.api;

import Xi_recommendation.SubEvent;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

/**
 * @author jpliu
 */
public class IncomingSubEventInputFormat extends FileInputFormat<LongWritable,  SubEvent> {
    @Override
    public RecordReader<LongWritable, SubEvent> getRecordReader(InputSplit inputSplit, JobConf jobConf, Reporter reporter) throws IOException {
        return new IncomingSubEventRecordReader(jobConf,(FileSplit)inputSplit);
    }
}
