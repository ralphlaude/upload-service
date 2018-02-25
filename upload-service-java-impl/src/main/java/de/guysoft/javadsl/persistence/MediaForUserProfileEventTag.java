package de.guysoft.javadsl.persistence;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class MediaForUserProfileEventTag {

    public static final AggregateEventTag<MediaForUserProfileEvent> MEDIA_FOR_USER_EVENT_AGGREGATE_EVENT_TAG =
            AggregateEventTag.of(MediaForUserProfileEvent.class);
}
