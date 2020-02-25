package org.turningme.theoretics.common.event;

import java.io.Serializable;

import org.turningme.theoretics.common.RecContext;

/**
 * Created by jpliu on 2020/2/25.
 */

public class EventRecommendation implements Serializable{

    RecContext recContext;
    float alpha = 0f; //the weight of ESim
    public EventRecommendation() {

    }

    public EventRecommendation(RecContext recContext) {
        this.recContext = recContext;
        this.recContext.setRecommendation(this);
    }

    public float GetESim(SocialEvent En, SocialEvent Eu) {
        EventMigration emig = recContext.getEventMigration();
        float probr=emig.EventMigrationProb(En, Eu);
        if (probr > 0)
            System.out.printf("probr=%f\t EventID=%d\t UserEventID=%d\n", probr, En.GetEventNo(), Eu.GetEventNo());
        float Sim = En.EventSimi(Eu);
        float overallsim = alpha * Sim + (1 - alpha)*probr;
        return overallsim;
    }
}
