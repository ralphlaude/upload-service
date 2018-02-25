package de.guysoft.javadsl.persistence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.util.Optional;

public interface MediaForUserProfileCommand extends Jsonable {

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = UploadMediaForUserProfile.class)
    interface AbstractUploadMediaForUserProfile extends MediaForUserProfileCommand, PersistentEntity.ReplyType<AbstractMediaId> {
        @Value.Parameter
        String getUserProfileId();

        @Value.Parameter
        String getMediaId();

        @Value.Parameter
        String getFileSystemMediaPath();

        @Value.Parameter
        boolean isMain();
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = GetMainMediaForUserProfile.class)
    interface AbstractGetMainMediaForUserProfile extends MediaForUserProfileCommand, PersistentEntity.ReplyType<Optional<AbstractFileSystemMediaPath>> {

    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = GetMediaForUserProfile.class)
    interface AbstractGetMediaForUserProfile extends MediaForUserProfileCommand, PersistentEntity.ReplyType<Optional<AbstractFileSystemMediaPath>> {

        @Value.Parameter
        String getMediaId();
    }

}
