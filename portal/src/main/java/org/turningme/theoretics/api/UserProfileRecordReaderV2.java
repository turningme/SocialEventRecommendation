package org.turningme.theoretics.api;

import Xi_recommendation.SpaceRange;
import Xi_recommendation.UPInfluDistriEle;
import Xi_recommendation.UPRespnseEle;
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
public class UserProfileRecordReaderV2 implements RecordReader<LongWritable, UserProfile> {
    private LineRecordReader lineRecordReader;
    private LongWritable currentKeyHolder = new LongWritable();
    private Text currentValueHolder = new Text();


    public UserProfileRecordReaderV2(Configuration job, FileSplit split) throws IOException {
        this.lineRecordReader = new LineRecordReader(job, split);
    }

    @Override
    public boolean next(LongWritable longWritable, UserProfile userProfile) throws IOException {
        boolean success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
        if (success) {
            String line = this.currentValueHolder.toString();
            if (line.contains("<userid>")) {
                userProfile.reset();
                userProfile.userId = Integer.valueOf(line.substring(line.indexOf("<userid>") + 9, line.indexOf("</userid>")).trim());
                userProfile.userOId = line.substring(line.indexOf("<userOid>") + 10, line.indexOf("</userOid>")).trim();
                longWritable.set(userProfile.userId);

                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                if (success) {
                    line = this.currentValueHolder.toString();
                    if (line.contains("<location>")) {
                        userProfile.UserPhysicalLocation = new SpaceRange();
                        userProfile.UserPhysicalLocation.lat = Float.valueOf(
                                line.substring(line.indexOf("<location>") + 11, line.indexOf("</location>")).trim().split(" ")[0]);
                        userProfile.UserPhysicalLocation.longi = Float.valueOf(
                                line.substring(line.indexOf("<location>") + 11, line.indexOf("</location>")).trim().split(" ")[1]);

                        success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                        if (success) {
                            line = this.currentValueHolder.toString();
                            if (line.contains("<MSGNUM>")) {
                                userProfile.PostNum = Integer.valueOf(line.substring(line.indexOf("<MSGNUM>") + 8, line.indexOf("</MSGNUM>")).trim());

                                success = this.lineRecordReader.next(this.currentKeyHolder, this.currentValueHolder);
                                if (success) {
                                    if (line.contains("<RESPONSENUMBERS>")) {

                                        String[] responsenumber = line.substring(line.indexOf("<RESPONSENUMBERS>") + 17,
                                                                                 line.indexOf("</RESPONSENUMBERS>")).trim().split("\t");
                                        for (int i = 0; i < responsenumber.length; i++) {
                                            int userid = Integer.parseInt(responsenumber[i].split(" ")[0]);
                                            int value = Integer.parseInt(responsenumber[i].split(" ")[1]);

                                            UPRespnseEle UPRele = new UPRespnseEle();
                                            UPInfluDistriEle UPIde = new UPInfluDistriEle();
                                            UPRele.userid = Integer.valueOf(userid);
                                            UPRele.userResponse = Integer.valueOf(value);
                                            userProfile.UserResponseNumbers.add(UPRele);

                                            if (userProfile.userId == UPIde.userid) {
                                                UPIde.userInflu = 1;
                                            } else {
                                                //initialization to 0.
                                                UPIde.userInflu = 0;
                                                userProfile.UserInfluenceDistri.add(UPIde);
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
