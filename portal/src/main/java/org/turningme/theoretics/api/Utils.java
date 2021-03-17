package org.turningme.theoretics.api;

import Xi_recommendation.EventUserSimi;
import Xi_recommendation.SubEvent;
import Xi_recommendation.UserProfile;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class Utils {
    public static String loadFileInClassPath(String fileName)  {
        try {
            String path = Utils.class.getClassLoader().getResource(fileName).toURI().getPath();
            return  path;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public static String loadFileInClassPathParent(String fileName)  {
        int pos = fileName.lastIndexOf("/");
        return fileName.substring(0,pos);
    }

    public static ArrayList<UserProfile> fromMap(HashMap<Long, UserProfile> input) {
        ArrayList<UserProfile> res = new ArrayList<>(input.size());
        for (UserProfile partition:input.values()) {
            res.add(partition);
        }
        return res;
    }


    public static String collectionRecommendInfo(SubEvent iesit){
        StringBuilder stringBuilder = new StringBuilder();
        String s="<clusterid> "+iesit.GetEventNo()+" </clusterid>\n";
        stringBuilder.append(s).append("\n");
        for(EventUserSimi eusit:iesit.RecUserSimi){
            s="<recitem>\t"+eusit.userid+"\t"+eusit.simi+"</recitem>\n";
            stringBuilder.append(s).append("\n");
        }

        return stringBuilder.toString();
    }
}
