package org.wind.tool.third.jackson.map.deser.std;

import java.io.IOException;

import org.wind.tool.third.jackson.JsonParser;
import org.wind.tool.third.jackson.JsonProcessingException;
import org.wind.tool.third.jackson.JsonToken;
import org.wind.tool.third.jackson.map.DeserializationContext;
import org.wind.tool.third.jackson.type.JavaType;

/**
 * @since 1.9
 */
public class JavaTypeDeserializer
    extends StdScalarDeserializer<JavaType>
{
    public JavaTypeDeserializer() { super(JavaType.class); }
    
    @Override
    public JavaType deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonToken curr = jp.getCurrentToken();
        // Usually should just get string value:
        if (curr == JsonToken.VALUE_STRING) {
            String str = jp.getText().trim();
            if (str.length() == 0) {
                return getEmptyValue();
            }
            return ctxt.getTypeFactory().constructFromCanonical(str);
        }
        // or occasionally just embedded object maybe
        if (curr == JsonToken.VALUE_EMBEDDED_OBJECT) {
            return (JavaType) jp.getEmbeddedObject();
        }
        throw ctxt.mappingException(_valueClass);
    }
}
