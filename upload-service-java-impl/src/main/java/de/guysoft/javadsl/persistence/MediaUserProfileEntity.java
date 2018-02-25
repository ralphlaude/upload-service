package de.guysoft.javadsl.persistence;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MediaUserProfileEntity extends PersistentEntity<MediaForUserProfileCommand, MediaForUserProfileEvent, MediaForUserProfile> {

    private final Logger log = LoggerFactory.getLogger(MediaUserProfileEntity.class);

    @Override
    public Behavior initialBehavior(Optional<MediaForUserProfile> snapshotState) {
        MediaForUserProfile initialState = MediaForUserProfile.builder().userProfileId("").mediaEntities(HashTreePMap.empty()).build();
        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(snapshotState.orElse(initialState));

        initCommandHandlers(behaviorBuilder);
        initEventHandlers(behaviorBuilder);

        return behaviorBuilder.build();
    }

    @Override
    public String entityTypeName() {
        return "MediaUserProfileEntity";
    }

    private void initCommandHandlers(BehaviorBuilder behaviorBuilder) {
        behaviorBuilder.setCommandHandler(UploadMediaForUserProfile.class, (cmd, ctx) -> {
            if (state().getMediaEntities().size() >= state().limit()) {
                log.info("At least 4 medias already saved for this entity. Media could not be saved");
                ctx.invalidCommand("At least 4 medias already saved for this entity. Media could not be saved");
                return ctx.done();
            }
            MediaForUserProfileSaved mediaSaved = MediaForUserProfileSaved.builder()
                        .userProfileId(cmd.getUserProfileId())
                        .mediaId(cmd.getMediaId())
                        .fileSystemMediaPath(FileSystemMediaPath.builder().path(cmd.getFileSystemMediaPath()).build())
                        .isMain(cmd.isMain())
                        .build();
            return ctx.thenPersist(mediaSaved, (t) -> ctx.reply(MediaId.of(cmd.getMediaId())));
        });

        behaviorBuilder.setReadOnlyCommandHandler(GetMainMediaForUserProfile.class, (cmd, context) ->
            context.reply(state().mainMediaPath())
        );

        behaviorBuilder.setReadOnlyCommandHandler(GetMediaForUserProfile.class, (cmd, context) ->
            context.reply(state().mediaPathById(cmd.getMediaId()))
        );

    }

    private void initEventHandlers(BehaviorBuilder behaviorBuilder) {
        behaviorBuilder.setEventHandler(MediaForUserProfileSaved.class, event -> {
            // A media file is main if it is the first uploaded file
            boolean isMain = state().getMediaEntities().isEmpty() || event.isMain();
            Optional<AbstractMediaEntity> currentMainMedia = isMain ?
                    state().getMediaEntities().values().stream().filter(AbstractMediaEntity::isMain).findFirst() :
                    Optional.empty();
            if (currentMainMedia.isPresent()) {
                PMap<String, AbstractMediaEntity> mediaEntityMap = mediasByReplacingMainMedia(currentMainMedia.get(), event, isMain);
                return state().withUserProfileId(event.getUserProfileId()).withMediaEntities(mediaEntityMap);
            } else {
                MediaEntity newMainMediaEntity = MediaEntity.builder().isMain(isMain).mediaId(event.getMediaId())
                        .mediaPath(event.getFileSystemMediaPath()).build();
                PMap<String, AbstractMediaEntity> mediaEntityMap = state().getMediaEntities().plus(event.getMediaId(), newMainMediaEntity);
                return state().withUserProfileId(event.getUserProfileId()).withMediaEntities(mediaEntityMap);
            }
        });
    }

    private PMap<String, AbstractMediaEntity> mediasByReplacingMainMedia(AbstractMediaEntity currentMainMedia,
                                                                         MediaForUserProfileSaved event, boolean isMain) {
        MediaEntity newMainMediaEntity = MediaEntity.builder().isMain(isMain).mediaId(event.getMediaId())
                .mediaPath(event.getFileSystemMediaPath()).build();
        AbstractMediaEntity changedMainMediaEntity = MediaEntity.builder().from(currentMainMedia).isMain(false).build();
        return state().getMediaEntities()
                .minus(currentMainMedia.getMediaId())
                .plus(changedMainMediaEntity.getMediaId(), changedMainMediaEntity)
                .plus(event.getMediaId(), newMainMediaEntity);
    }

}
