package de.guysoft.javadsl.service;

import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import de.guysoft.javadsl.persistence.AbstractMediaId;

import java.util.StringJoiner;

final class ResponseHeaders {

    private ResponseHeaders() {
    }

    static ResponseHeader responseHeader(String userProfileId, AbstractMediaId mediaId) {
        StringJoiner joiner = new StringJoiner("/").add("/java/service/media/users")
                .add(userProfileId).add("medias").add(mediaId.getId());
        return ResponseHeader.OK.withHeader("Location", joiner.toString());
    }

    static ResponseHeader responseHeaderWithoutLocation(String userProfileId) {
        return ResponseHeader.OK.withHeader("UserId", userProfileId);
    }

}
