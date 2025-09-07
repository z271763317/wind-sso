package org.wind.tool.third.jackson.map.deser.std;

import java.io.IOException;
import java.util.Date;

import org.wind.tool.third.jackson.JsonParser;
import org.wind.tool.third.jackson.JsonProcessingException;
import org.wind.tool.third.jackson.map.DeserializationContext;

/**
 * Simple deserializer for handling {@link java.util.Date} values.
 *<p>
 * One way to customize Date formats accepted is to override method
 * {@link DeserializationContext#parseDate} that this basic
 * deserializer calls.
 * 
 * @since 1.9 (moved from higher-level package)
 */
public class DateDeserializer
    extends StdScalarDeserializer<Date>
{
    public DateDeserializer() { super(Date.class); }
    
    @Override
    public java.util.Date deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        return _parseDate(jp, ctxt);
    }
}
