package de.guysoft.javadsl.persistence;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import org.pcollections.PMap;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = MediaForUserProfile.class)
public interface AbstractMediaForUserProfile extends Jsonable {

    @Value.Default
    default int limit() {
        return 4;
    }

    @Value.Parameter
    String getUserProfileId();

    @Value.Parameter
    PMap<String, AbstractMediaEntity> getMediaEntities();

    default Optional<AbstractFileSystemMediaPath> mainMediaPath() {
        return getMediaEntities().values().stream().filter(entity -> entity.isMain())
                .map(entity -> entity.getMediaPath()).findFirst();
    }

    default Optional<AbstractFileSystemMediaPath> mediaPathById(String mediaId) {
        return getMediaEntities().values().stream().filter(entity -> entity.getMediaId().equals(mediaId))
                .map(entity -> entity.getMediaPath()).findFirst();
    }

    default Optional<AbstractMediaEntity> mediaById(String mediaId) {
        return getMediaEntities().values().stream()
                .filter(entity -> entity.getMediaId().equals(mediaId))
                .findFirst();
    }

    default PVector<AbstractMediaEntity> mediasOrderedByStatus() {
        Predicate<AbstractMediaEntity> mediaIsMainPredicate = entity -> entity.isMain();
        Function<AbstractMediaEntity, String> mediaDataToId = entity -> entity.getMediaId();
       return getMediaEntities().values().stream().filter(mediaIsMainPredicate).map(mediaDataToId).findFirst()
               .map(me -> getMediaEntities().values().stream()
                           .filter(mediaIsMainPredicate.negate()).collect(collectingAndThen(toList(), TreePVector::from))
                           .plus(0, getMediaEntities().get(me))
               ).orElse(TreePVector.empty());
    }

    default boolean mediaExists(String mediaId) {
        return getMediaEntities().keySet().stream().anyMatch(mediaId::equals);
    }

    default boolean isMainMedia(String mediaId) {
        return getMediaEntities().values().stream().filter(entity -> entity.getMediaId().equals(mediaId))
                .map(entity -> entity.isMain()).findFirst().get();
    }
}
