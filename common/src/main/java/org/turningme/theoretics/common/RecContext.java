package org.turningme.theoretics.common;

import org.turningme.theoretics.common.event.EventMigration;
import org.turningme.theoretics.common.event.EventRecomOpti;
import org.turningme.theoretics.common.event.EventRecommendation;
import org.turningme.theoretics.common.lsb.LSB;

/**
 * Created by jpliu on 2020/2/24.
 */
public class RecContext {
    EventMigration eventMigration;
    EventRecomOpti eventRecomOpti;
    EventRecommendation recommendation;
    LSB lsb;


    public EventMigration getEventMigration() {
        return eventMigration;
    }

    public void setEventMigration(EventMigration eventMigration) {
        this.eventMigration = eventMigration;
    }

    public LSB getLsb() {
        return lsb;
    }

    public void setLsb(LSB lsb) {
        this.lsb = lsb;
    }

    public EventRecomOpti getEventRecomOpti() {
        return eventRecomOpti;
    }

    public void setEventRecomOpti(EventRecomOpti eventRecomOpti) {
        this.eventRecomOpti = eventRecomOpti;
    }

    public EventRecommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(EventRecommendation recommendation) {
        this.recommendation = recommendation;
    }
}
