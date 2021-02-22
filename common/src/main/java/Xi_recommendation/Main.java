package Xi_recommendation;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        String ext="";
        String MTimeSLotFlist="data/mgires/"; //file name

        String path="data/filtered_earthquake_4GramFeatures/"; //event migration detection res file path
        File file = new File(path);
        File[] filelist = new File(path).listFiles();

        String outpath="res/";
        float migrateT=1;
        String UserInflunFilePath="data/";
        int coupling=0;

//        for(File filename:filelist){
//            MTimeSLotFlist=filename.toString();
            ContinuousEventRecommedation EventRec=new ContinuousEventRecommedation(MTimeSLotFlist,path,outpath,ext,migrateT,UserInflunFilePath,coupling);
//        }
     }
}

