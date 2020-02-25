package org.turningme.theoretics.common.event;

import java.util.List;

import org.turningme.theoretics.common.beans.SocialMSG;

/**
 * Created by jpliu on 2020/2/24.
 */
public class MSGSET {
    List<SocialMSG> socialMSGs;

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
