package org.wind.tool.third.jackson.map.ser;

import org.wind.tool.third.jackson.JsonGenerator;
import org.wind.tool.third.jackson.map.SerializerProvider;

/**
 * Interface that defines API for filter objects use (as configured
 * using {@link org.wind.tool.third.jackson.map.annotate.JsonFilter})
 * for filtering bean properties to serialize.
 * 
 * @since 1.7
 */
public interface BeanPropertyFilter
{
    /**
     * Method called by {@link BeanSerializer} to let filter decide what to do with
     * given bean property value: the usual choices are to either filter out (i.e.
     * do nothing) or write using given {@link BeanPropertyWriter}, although filters
     * can choose other to do something different altogether.
     * 
     * @param bean Bean of which property value to serialize
     * @param jgen Generator use for serializing value
     * @param prov Provider that can be used for accessing dynamic aspects of serialization
     *    processing
     * @param writer Default bean property serializer to use
     */
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov,
            BeanPropertyWriter writer)
        throws Exception;
}
