package org.turningme.theoretics.common.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.turningme.theoretics.common.beans.SocialMSG;

/**
 * Created by jpliu on 2020/2/24.
 */
public class MSGSET  implements Serializable {
    List<SocialMSG> socialMSGs = new ArrayList<>();

    public MSGSET(List<SocialMSG> socialMSGs) {
        this.socialMSGs = socialMSGs;
    }

    public List<SocialMSG> getSocialMSGs() {
        return socialMSGs;
    }

    public void setSocialMSGs(List<SocialMSG> socialMSGs) {
        this.socialMSGs = socialMSGs;
    }
}
