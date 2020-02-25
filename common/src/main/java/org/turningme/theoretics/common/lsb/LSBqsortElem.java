package org.turningme.theoretics.common.lsb;

import java.io.Serializable;

/**
 * Created by jpliu on 2020/2/24.
 */
public class LSBqsortElem  implements Serializable {
    public int[] ds;
    public int	pos;		/* position of the object in array ds */
    public int	pz;			/* size of the z array below */
    public int[]  z;

}
