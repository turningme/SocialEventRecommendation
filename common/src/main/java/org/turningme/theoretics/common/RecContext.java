package org.turningme.theoretics.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.turningme.theoretics.common.beans.UPEventPartition;
import org.turningme.theoretics.common.event.EventMigration;
import org.turningme.theoretics.common.event.EventRecomOpti;
import org.turningme.theoretics.common.event.EventRecommendation;
import org.turningme.theoretics.common.event.SocialEvent;
import org.turningme.theoretics.common.event.SocialEventOperation;
import org.turningme.theoretics.common.lsb.LSB;

/**
 * Created by jpliu on 2020/2/24.
 */
public class RecContext implements Serializable{
    public EventMigration eventMigration;
    public EventRecomOpti eventRecomOpti;
    public EventRecommendation recommendation;
    public LSB lsb;
    public SocialEventOperation socialEventOperation;
    public List<SocialEvent> Eventclusters;


    public List<UPEventPartition> upEventPartitionList =new ArrayList<>(10);


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

    public SocialEventOperation getSocialEventOperation() {
        return socialEventOperation;
    }

    public void setSocialEventOperation(SocialEventOperation socialEventOperation) {
        this.socialEventOperation = socialEventOperation;
    }

    public List<UPEventPartition> getUpEventPartitionList() {
        return upEventPartitionList;
    }

    public void setUpEventPartitionList(List<UPEventPartition> upEventPartitionList) {
        this.upEventPartitionList = upEventPartitionList;
    }

    public List<SocialEvent> getEventclusters() {
        return Eventclusters;
    }

    public void setEventclusters(List<SocialEvent> eventclusters) {
        Eventclusters = eventclusters;
    }
}
