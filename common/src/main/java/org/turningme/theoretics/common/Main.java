package org.turningme.theoretics.common;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.turningme.theoretics.common.event.EventDetectionAndMigration;
import org.turningme.theoretics.common.event.EventMigration;
import org.turningme.theoretics.common.lsb.LSB;

/**
 * Created by jpliu on 2020/2/23.
 */
public class Main {
    public static void main(String[] args){
        System.out.print("hello world" );
//        testRefAndValueDelivery();
//        testArray();
        EventDetectionAndMigration.UserProfileAllocation();
    }

    public static void test1(){

        ByteArrayInputStream inputStream = new ByteArrayInputStream("m = %d\n".getBytes());
        Scanner input = new Scanner(inputStream);
//        System.out.println("请输入一个字符串(中间能加空格或符号)");
//        String a = input.nextLine();
//        System.out.println("请输入一个字符串(中间不能加空格或符号)");
//        String b = input.next();
//        System.out.println("请输入一个整数");
//        int c;
//        c = input.nextInt();
//        System.out.println("请输入一个double类型的小数");
//        double d = input.nextDouble();
//        System.out.println("请输入一个float类型的小数");
//        float f = input.nextFloat();
//        System.out.println("按顺序输出abcdf的值：");
//        System.out.println(a);
//        System.out.println(b);
//        System.out.println(c);
//        System.out.println(d);
//        System.out.println(f);

        String a = input.next("m = %d\n");
        System.out.print("  " + a);
    }


    public static void testReadLSBFile(){
        LSB lsb = new LSB();
        lsb.readParaFile("/Users/jpliu/CLionProjects/EventRecoHelper/para.txt");

    }


    public static void testUserProfile(){
        EventMigration eventMigration = new EventMigration();
        eventMigration.loadUserProfileHashMap("/Users/jpliu/CLionProjects/EventRecoHelper/UserInfluDictfile.txt");
    }


    public static void testUserProfileInfo(){
        EventMigration eventMigration = new EventMigration();
        eventMigration.uploadUserProfilesIntoHashMap("/Users/jpliu/CLionProjects/EventRecoHelper/userprofile217.txt");
    }



    static class DT{
        Integer j;
        String i;
        long x;

        public DT(String i, Integer j, long x) {
            this.i = i;
            this.j = j;
            this.x = x;
        }

        public String getI() {
            return i;
        }

        public void setI(String i) {
            this.i = i;
        }

        public Integer getJ() {
            return j;
        }

        public void setJ(Integer j) {
            this.j = j;
        }

        public long getX() {
            return x;
        }

        public void setX(long x) {
            this.x = x;
        }

        @Override
        public String toString() {
            return "DT{" +
                    "i='" + i + '\'' +
                    ", j=" + j +
                    ", x=" + x +
                    '}';
        }
    }
    public static void testRefAndValueDelivery(){
        DT dt1 = new DT("1",1,1L);


        DT dt2 = new DT(dt1.getI(), dt1.getJ(), dt1.getX());
        System.out.println("dt2 " + dt2);
        dt1.setI("2");
        dt1.setJ(2);
        dt1.setX(2L);
        System.out.println("dt2 " + dt2);

    }


    public static void testArrayRemove(){
        List<Integer> t1 = new ArrayList<>();
        t1.add(1);
        t1.add(2);
        t1.add(3);

        t1.remove(2);
        System.out.println(t1);
        t1.get(1);

        t1.remove(1);
        t1.get(0);
    }


  public static void testArray(){
      int[] t1 = new int[]{1,3,5};
      System.out.println();
      System.out.println(t1[0]);
      testArrayModify(t1);
      System.out.println(t1[0]);
  }

    public static void testArrayModify(int[] t1){
        t1[0]=2;
        t1[1]=4;
        t1[2]=6;
    }


}
