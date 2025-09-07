package org.wind.tool.third.jackson.map.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.wind.tool.third.jackson.annotate.JacksonAnnotation;

/**
 * Annotation used to indicate which logical filter is to be used
 * for filtering out properties of type (class) annotated;
 * association made by this annotation declaring ids of filters,
 * and  {@link org.wind.tool.third.jackson.map.ObjectMapper} (or objects
 * it delegates to) providing matching filters by id.
 * Filters to use are of type
 * {@link org.wind.tool.third.jackson.map.ser.BeanPropertyFilter} and
 * are registered through {@link org.wind.tool.third.jackson.map.ObjectMapper}
 * 
 * @since 1.7
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonFilter
{
    /**
     * Id of filter to use; if empty String (""), no filter is to be used.
     */
    public String value();
}
