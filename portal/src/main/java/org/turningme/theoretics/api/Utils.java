package org.turningme.theoretics.api;

import java.net.URISyntaxException;

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
}
