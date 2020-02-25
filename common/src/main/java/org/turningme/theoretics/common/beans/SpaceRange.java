package org.turningme.theoretics.common.beans;

/**
 * Created by jpliu on 2020/2/23.
 */
public class SpaceRange {
    public float lat;
    public float longi;
    public float radius;

    public SpaceRange() {
    }

    public SpaceRange(float lat, float longi, float radius) {
        this.lat = lat;
        this.longi = longi;
        this.radius = radius;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLongi() {
        return longi;
    }

    public void setLongi(float longi) {
        this.longi = longi;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
