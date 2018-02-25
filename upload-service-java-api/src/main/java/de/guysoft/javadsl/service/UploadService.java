package de.guysoft.javadsl.service;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;
import play.api.mvc.Result;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface UploadService extends Service {

    ServiceCall<NotUsed, Result> uploadMedia(String userId);
    ServiceCall<NotUsed, Result> uploadMainMedia(String userId);
    ServiceCall<NotUsed, Result> mainMedia(String userId);
    ServiceCall<NotUsed, Result> media(String userProfileId, String uploadId);

    default Descriptor descriptor() {
        return named("JAVA_UPLOAD_SERVICE").withCalls(
            restCall(Method.POST, "/java/service/media/users/:userId/upload", this::uploadMedia),
            restCall(Method.POST, "/java/service/media/users/:userId/upload/main", this::uploadMainMedia),

            restCall(Method.GET, "/java/service/media/users/:userId/medias/main", this::mainMedia),
            restCall(Method.GET, "/java/service/media/users/:userId/medias/:uploadId", this::media)
        )
        .withAutoAcl(true)
        .withPathParamSerializer(UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString));
    }
}
