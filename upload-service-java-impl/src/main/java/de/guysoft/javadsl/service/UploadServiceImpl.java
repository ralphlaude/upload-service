package de.guysoft.javadsl.service;

import akka.NotUsed;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.server.PlayServiceCall;
import de.guysoft.javadsl.persistence.AbstractFileSystemMediaPath;
import de.guysoft.javadsl.persistence.AbstractMediaId;
import de.guysoft.javadsl.persistence.GetMainMediaForUserProfile;
import de.guysoft.javadsl.persistence.GetMediaForUserProfile;
import de.guysoft.javadsl.persistence.MediaForUserProfileCommand;
import de.guysoft.javadsl.persistence.MediaUserProfileEntity;
import de.guysoft.javadsl.persistence.UploadMediaForUserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.Result;
import play.libs.F;
import play.libs.streams.Accumulator;
import play.mvc.EssentialAction;
import play.mvc.Http;
import play.mvc.Results;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class UploadServiceImpl implements UploadService {

    private final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);

    private final PersistentEntityRegistry persistentEntities;
    private final MediaFileManager mediaFileManager;

    @Inject
    public UploadServiceImpl(PersistentEntityRegistry persistentEntities, MediaFileManager mediaFileManager) {
        this.persistentEntities = persistentEntities;
        this.mediaFileManager = mediaFileManager;

        this.persistentEntities.register(MediaUserProfileEntity.class);
    }

    @Override
    public PlayServiceCall<NotUsed, Result> uploadMedia(String userId) {
        return wt -> EssentialAction.of(requestHeader -> {

            //TODO: is it possible to do this better??
            Accumulator<ByteString, F.Either<play.mvc.Result, Http.MultipartFormData<File>>> requestAccumulator =
                    new play.mvc.BodyParser.MultipartFormData().apply(requestHeader);
            Sink<ByteString, CompletionStage<F.Either<play.mvc.Result, Http.MultipartFormData<File>>>> accumulatorSink =
                    requestAccumulator.toSink();

            return Accumulator.fromSink(accumulatorSink.mapMaterializedValue(uploadedFileStage ->
                    uploadPipelineStage(userId, uploadedFileStage, false)
            ));
        });
    }

    @Override
    public PlayServiceCall<NotUsed, Result> uploadMainMedia(String userId) {
        return wt -> EssentialAction.of(requestHeader -> {

            //TODO: is it possible to do this better??
            Accumulator<ByteString, F.Either<play.mvc.Result, Http.MultipartFormData<File>>> requestAccumulator =
                    new play.mvc.BodyParser.MultipartFormData().apply(requestHeader);
            Sink<ByteString, CompletionStage<F.Either<play.mvc.Result, Http.MultipartFormData<File>>>> accumulatorSink =
                    requestAccumulator.toSink();

            return Accumulator.fromSink(accumulatorSink.mapMaterializedValue(uploadedFileStage ->
                    uploadPipelineStage(userId, uploadedFileStage, true)
            ));
        });
    }

    @Override
    public PlayServiceCall<NotUsed, Result> mainMedia(String userId) {
        return wt -> EssentialAction.of(requestHeader -> {
            GetMainMediaForUserProfile mainMediaForUserProfileCommand = GetMainMediaForUserProfile.builder().build();
            PersistentEntityRef<MediaForUserProfileCommand> entityRef = persistentEntities.refFor(MediaUserProfileEntity.class, userId);
            CompletionStage<play.mvc.Result> resultCompletionStage = entityRef.ask(mainMediaForUserProfileCommand)
                    .thenComposeAsync(reply -> {
                        Optional<AbstractFileSystemMediaPath> mediaPath = (Optional<AbstractFileSystemMediaPath>) reply;
                        if(!mediaPath.isPresent()) return CompletableFuture.completedFuture(ByteString.empty());
                        return mediaFileManager.readMediaFile(mediaPath.get().getPath());
                    })
                    .thenApplyAsync(mediaFile -> {
                        if(mediaFile.isEmpty()) return Results.ok("No image available");
                        return Results.ok(mediaFile.toArray());
                    })
                    .exceptionally(throwable -> {
                        log.info("By asking the persistent entity with userId {} for reading main file some error occurred {}",
                                userId, throwable.getMessage());
                        return Results.ok("No image available");
                    });

            return Accumulator.done(resultCompletionStage);
        });
    }

    @Override
    public PlayServiceCall<NotUsed, Result> media(String userId, String uploadId) {
        return wt -> EssentialAction.of(requestHeader -> {
            GetMediaForUserProfile mediaForUserProfileCommand = GetMediaForUserProfile.builder().mediaId(uploadId).build();
            PersistentEntityRef<MediaForUserProfileCommand> entityRef = persistentEntities.refFor(MediaUserProfileEntity.class, userId);
            CompletionStage<play.mvc.Result> resultCompletionStage = entityRef.ask(mediaForUserProfileCommand)
                    .thenComposeAsync(reply -> {
                        Optional<AbstractFileSystemMediaPath> mediaPath = (Optional<AbstractFileSystemMediaPath>) reply;
                        if(!mediaPath.isPresent()) return CompletableFuture.completedFuture(ByteString.empty());
                        return mediaFileManager.readMediaFile(mediaPath.get().getPath());
                    })
                    .thenApplyAsync(mediaFile -> {
                        if(mediaFile.isEmpty()) return Results.ok("No image available");
                        return Results.ok(mediaFile.toArray());
                    })
                    .exceptionally(throwable -> {
                        log.info("By asking the persistent entity with userId {} for reading main file some error occurred {}",
                                userId, throwable.getMessage());
                        return Results.ok("No image available");
                    });

            return Accumulator.done(resultCompletionStage);
        });
    }

    private CompletionStage<play.mvc.Result> uploadPipelineStage(String userId,
            CompletionStage<F.Either<play.mvc.Result, Http.MultipartFormData<File>>> uploadedFileStage, Boolean isMain) {

        return uploadedFileStage.thenComposeAsync(eitherFile ->
            mediaFileManager.saveMediaFile(userId, UUID.randomUUID().toString(), eitherFile)
                    .thenComposeAsync(fileSystemMediaPath -> {
                        if (!fileSystemMediaPath.isPresent()) {
                            String logMessage = "No uploaded file for userId {}";
                            log.info(logMessage, userId);
                            return CompletableFuture.completedFuture(Results.ok("file could not be uploaded"));
                        }

                        return uploadMedia(userId, UUID.randomUUID().toString(), fileSystemMediaPath.get(), isMain)
                                .thenApplyAsync(this::handleUploadPersistentEntityResponseHeader);
            })
        );
    }

    private CompletionStage<ResponseHeader> uploadMedia(String userId, String entityMediaId, MediaFileManager.MediaPath fileSystemMediaPath, Boolean isMain) {
        UploadMediaForUserProfile uploadMedia = UploadMediaForUserProfile.builder().userProfileId(userId)
                .mediaId(entityMediaId).fileSystemMediaPath(fileSystemMediaPath.path).isMain(isMain).build();

        return persistentEntities.refFor(MediaUserProfileEntity.class, userId).ask(uploadMedia)
                .thenApplyAsync(reply -> ResponseHeaders.responseHeader(userId, (AbstractMediaId) reply))
                .exceptionally(throwable -> {
                    log.info("By asking the persistent entity with userId {} for uploading file some error occurred {}",
                            userId, throwable.getMessage());
                    return ResponseHeaders.responseHeaderWithoutLocation(userId);
                });
    }


    private play.mvc.Result handleUploadPersistentEntityResponseHeader(ResponseHeader responseHeader) {
        return responseHeader.getHeader("Location")
                .map(location -> Results.ok("file successfully uploaded").withHeader("Location", location))
                .orElse(Results.ok("file could not be uploaded"));
    }


}
