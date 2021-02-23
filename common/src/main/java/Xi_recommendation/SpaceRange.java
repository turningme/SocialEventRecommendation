package Xi_recommendation;

import java.io.Serializable;

/**
 * @author helen ding on 26/09/2020.
 */
public class SpaceRange implements Serializable {
    public float lat;
    public float longi;
    public float radius;


    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }
}
