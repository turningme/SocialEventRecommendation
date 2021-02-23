package org.turningme.theoretics.api;

import Xi_recommendation.SubEvent;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.LineRecordReader;
import org.apache.hadoop.mapred.RecordReader;

/**
 * @author jpliu
 */
public class SubEventRecordReader implements RecordReader<LongWritable, SubEvent> {
    private LineRecordReader lineRecordReader;
    private LongWritable currentKeyHolder = new LongWritable();
    private Text currentValueHolder = new Text();
    private boolean stop ;


    public SubEventRecordReader(Configuration job, FileSplit split) throws IOException {
        stop = false;
        // invalid file name style
        if (split.getPath().getName().contains("DS")){
            stop =true;
        }
        this.lineRecordReader = new LineRecordReader(job, split);
    }
    @Override
    public boolean next(LongWritable longWritable, SubEvent subEvent) throws IOException {
        //do not read stop stamped file
        if (stop) {
            return false;
        }


        return  false;
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
