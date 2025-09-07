package org.wind.tool.third.jackson.map;

import java.io.IOException;

import org.wind.tool.third.jackson.JsonGenerator;
import org.wind.tool.third.jackson.JsonProcessingException;

/**
 * Interface that is to replace {@link JsonSerializable} to
 * allow for dynamic type information embedding.
 * 
 * @since 1.5
 * @author tatu
 */
@SuppressWarnings("deprecation")
public interface JsonSerializableWithType
    extends JsonSerializable
{
    public void serializeWithType(JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonProcessingException;
}
