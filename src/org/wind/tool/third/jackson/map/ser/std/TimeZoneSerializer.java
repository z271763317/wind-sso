package org.wind.tool.third.jackson.map.ser.std;

import java.io.IOException;
import java.util.TimeZone;

import org.wind.tool.third.jackson.JsonGenerationException;
import org.wind.tool.third.jackson.JsonGenerator;
import org.wind.tool.third.jackson.map.SerializerProvider;
import org.wind.tool.third.jackson.map.TypeSerializer;

/**
 * @since 1.8
 */
public class TimeZoneSerializer
    extends ScalarSerializerBase<TimeZone>
{
    public final static TimeZoneSerializer instance = new TimeZoneSerializer();
    
    public TimeZoneSerializer() { super(TimeZone.class); }

    @Override
    public void serialize(TimeZone value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        jgen.writeString(value.getID());
    }

    @Override
    public void serializeWithType(TimeZone value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        // Better ensure we don't use specific sub-classes:
        typeSer.writeTypePrefixForScalar(value, jgen, TimeZone.class);
        serialize(value, jgen, provider);
        typeSer.writeTypeSuffixForScalar(value, jgen);
    }
}
