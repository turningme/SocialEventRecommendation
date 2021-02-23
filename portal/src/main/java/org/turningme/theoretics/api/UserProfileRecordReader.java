package org.turningme.theoretics.api;

import Xi_recommendation.UPInfluDistriEle;
import Xi_recommendation.UserProfile;

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
public class UserProfileRecordReader implements RecordReader<LongWritable, UserProfile> {
    private LineRecordReader lineRecordReader;
    private LongWritable currentKeyHolder = new LongWritable();
    private Text currentValueHolder = new Text();


    public UserProfileRecordReader(Configuration job, FileSplit split) throws IOException {
        this.lineRecordReader = new LineRecordReader(job, split);
    }
    @Override
    public boolean next(LongWritable longWritable, UserProfile userProfile) throws IOException {
        boolean success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success){
            String line =  this.currentValueHolder.toString();
            if (line.contains("<USERID>")) {
                userProfile.reset();
                int uid = Integer.parseInt(line.substring(9, line.indexOf("</USERID>")).trim());
                longWritable.set(uid);

                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                if (success){
                    line =  this.currentValueHolder.toString();
                    UserProfile up = userProfile;
                    up.UserInfluenceDistri.clear();
                    String[] userdistri = line.substring(22, line.indexOf("</USERINFLUENCEDISTRI>")).trim().split("\t");
                    for (int i = 0; i < userdistri.length; i++) {
                        int userid = Integer.parseInt(userdistri[i].split(" ")[0]);
                        float value = Float.parseFloat(userdistri[i].split(" ")[1]);
                        UPInfluDistriEle upide = new UPInfluDistriEle();
                        upide.userid = userid;
                        upide.userInflu = value;
                        up.userId = userid;
                        up.UserInfluenceDistri.add(upide);
                    }
                }


            }
        }
        return success;
    }

    @Override
    public LongWritable createKey() {
        return new LongWritable();
    }

    @Override
    public UserProfile createValue() {
        return new UserProfile();
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
