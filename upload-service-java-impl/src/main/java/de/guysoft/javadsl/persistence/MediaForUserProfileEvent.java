package de.guysoft.javadsl.persistence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

public interface MediaForUserProfileEvent extends Jsonable, AggregateEvent<MediaForUserProfileEvent>  {

    default AggregateEventTag<MediaForUserProfileEvent> aggregateTag() {
        return MediaForUserProfileEventTag.MEDIA_FOR_USER_EVENT_AGGREGATE_EVENT_TAG;
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = MediaForUserProfileSaved.class)
    interface AbstractMediaForUserProfileSaved extends MediaForUserProfileEvent {
        @Value.Parameter
        String getUserProfileId();

        @Value.Parameter
        String getMediaId();

        @Value.Parameter
        AbstractFileSystemMediaPath getFileSystemMediaPath();

        @Value.Parameter
        boolean isMain();
    }

}
