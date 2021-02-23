package org.turningme.theoretics.api;

import Xi_recommendation.UserProfile;

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
public class UserProfileInputFormat extends FileInputFormat<LongWritable, UserProfile> {
    @Override
    public RecordReader<LongWritable, UserProfile> getRecordReader(InputSplit inputSplit, JobConf jobConf, Reporter reporter) throws IOException {
        return new UserProfileRecordReader(jobConf,(FileSplit)inputSplit);
    }
}
