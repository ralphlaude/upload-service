package de.guysoft.javadsl.persistence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = MediaId.class)
public interface AbstractMediaId extends Jsonable {

    @Value.Parameter
    String getId();
}
