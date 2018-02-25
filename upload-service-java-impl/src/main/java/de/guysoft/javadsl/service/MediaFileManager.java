package de.guysoft.javadsl.service;

import akka.stream.Materializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.libs.F;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class MediaFileManager {

    private static final Logger log = LoggerFactory.getLogger(MediaFileManager.class);

    private final Materializer materializer;
    private final String mediaDirectoryPath;

    @Inject
    public MediaFileManager(Materializer materializer, Configuration configuration) {
        this.materializer = materializer;
        this.mediaDirectoryPath = configuration.getString("mediaservice.medias.directory");
    }

    public CompletionStage<Optional<MediaPath>> saveMediaFile(
            String userProfileId,
            String fileSystemMediaId,
            F.Either<play.mvc.Result, Http.MultipartFormData<File>> eitherFile) {

        if(!isUploadFileExist(eitherFile)) {
            log.info("No uploaded main file for userProfileId {}", userProfileId);
            return CompletableFuture.completedFuture(Optional.empty());
        }

        String userMediaDirectoryPath = mediaDirectoryPath + userProfileId;
        Http.MultipartFormData.FilePart<File> uploadedFilePart = eitherFile.right.get().getFiles().get(0);
        Path uploadedFilePath = uploadedFilePart.getFile().toPath();

        log.info("File name " + uploadedFilePath.getFileName());
        log.info("File path " + uploadedFilePath);
        log.info("number of files " + eitherFile.right.get().getFiles().size());

        return createMediaFile(userMediaDirectoryPath, fileSystemMediaId).thenCompose(mediaFilePath -> {
            if(!mediaFilePath.isPresent()) {
                log.info("mediaFilePath is empty");
                return CompletableFuture.completedFuture(Optional.empty());
            }
            log.info("mediaFilePath for saving the media file is - " + mediaFilePath.get());
            return FileIO.fromPath(uploadedFilePath).runWith(FileIO.toPath(mediaFilePath.get()), materializer)
                    .thenApply(ioResult -> {
                            log.info("ioResult for saving the media file is - " + ioResult);
                            if(ioResult.wasSuccessful()) {
                                log.info("Saving the media file was successful");
                                return Optional.of(new MediaPath(mediaFilePath.get().toString()));
                            }

                            log.info("By saving the media file {} some error occurred {}", uploadedFilePath.getFileName(), ioResult.getError().getMessage());
                            ioResult.getError().printStackTrace();
                            return Optional.empty();
                        });
                }
        );
    }

    public CompletionStage<ByteString> readMediaFile(String mediaPath) {
        log.info("read file mediaPath {}", mediaPath);
        Sink<ByteString, CompletionStage<ByteString>> fileFoldSink = Sink.fold(ByteString.empty(), (b1, b2) -> b1.concat(b2));
        return FileIO.fromPath(Paths.get(mediaPath)).runWith(fileFoldSink, materializer)
                .exceptionally(throwable -> {
                    log.info("By reading the media from path some error occurred {}", throwable.getMessage());
                    return ByteString.empty();
                });
    }

    private boolean isUploadFileExist(F.Either<play.mvc.Result, Http.MultipartFormData<File>> either) {
        return either.right.isPresent() && (either.right.get().getFiles() != null && !either.right.get().getFiles().isEmpty());
    }

    private CompletableFuture<Optional<Path>> createMediaFile(String userMediaDirectoryPath, String mediaId) {
        return CompletableFuture.supplyAsync(() -> createUserMediaDirectory(userMediaDirectoryPath))
                .thenApply(directoryCreated -> {
                    log.info("Media directory created is - " + userMediaDirectoryPath);
                    if(!directoryCreated) {
                        log.info("Could no create the media directory");
                        return Optional.<Path>empty();
                    }
                    return internCreateMediaFile(userMediaDirectoryPath + "/" +  mediaId);
                });
    }

    private boolean createUserMediaDirectory(String userMediaDirectoryPath) {
        if(Files.exists(Paths.get(userMediaDirectoryPath))) {
            return true;
        } else {
            try {
                Files.createDirectory(Paths.get(userMediaDirectoryPath));
                return true;
            } catch (IOException e) {
                log.error("By creating the user media directory {} some error occurred {}", userMediaDirectoryPath, e.getMessage());
                return false;
            }
        }
    }

    private Optional<Path> internCreateMediaFile(String mediaFilePath) {
        try {
            return Optional.of(Files.createFile(Paths.get(mediaFilePath)));
        } catch (IOException e) {
            log.error("By creating the media file {} some error occurred {}", mediaFilePath, e.getMessage());
            return Optional.empty();
        }
    }

    final class MediaPath {
        final String path;

        MediaPath(String path) {
            this.path = path;
        }
    }

}
