package org.wind.tool.third.jackson.map.ser.impl;

import java.io.IOException;
import java.lang.reflect.Type;

import org.wind.tool.third.jackson.JsonGenerationException;
import org.wind.tool.third.jackson.JsonGenerator;
import org.wind.tool.third.jackson.JsonNode;
import org.wind.tool.third.jackson.map.JsonMappingException;
import org.wind.tool.third.jackson.map.SerializerProvider;
import org.wind.tool.third.jackson.map.ser.std.SerializerBase;

/**
 * Special bogus "serializer" that will throw
 * {@link JsonGenerationException} if its {@link #serialize}
 * gets invoked. Most commonly registered as handler for unknown types,
 * as well as for catching unintended usage (like trying to use null
 * as Map/Object key).
 */
public final class FailingSerializer
    extends SerializerBase<Object>
{
    final String _msg;
    
    public FailingSerializer(String msg) {
        super(Object.class);
        _msg = msg;
    }
    
    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        throw new JsonGenerationException(_msg);
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint)
        throws JsonMappingException
    {
        return null;
    }
}
