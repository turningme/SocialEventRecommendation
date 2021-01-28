package org.turningme.theoretics.api;


import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsTest {
    static final Logger LOG = LoggerFactory.getLogger(UtilsTest.class);

    @Test
    public void TestLoadFileInClassPath(){
        String path = Utils.loadFileInClassPath("testFile1");
        LOG.info("path = {}", path);
        System.out.println("path = " +  path);
    }

    @Test
    public void TestLoadFileInClassPathParent(){
        String p = "/a/a/a";
        String pp = Utils.loadFileInClassPathParent(p);
        System.out.println(pp);
        Assert.assertEquals(pp,"/a/a");
    }
}
