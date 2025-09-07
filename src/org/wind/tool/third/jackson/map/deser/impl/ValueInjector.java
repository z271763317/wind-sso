package org.wind.tool.third.jackson.map.deser.impl;

import java.io.IOException;

import org.wind.tool.third.jackson.map.BeanProperty;
import org.wind.tool.third.jackson.map.DeserializationContext;
import org.wind.tool.third.jackson.map.introspect.AnnotatedMember;
import org.wind.tool.third.jackson.map.util.Annotations;
import org.wind.tool.third.jackson.type.JavaType;

/**
 * Class that encapsulates details of value injection that occurs before
 * deserialization of a POJO. Details include information needed to find
 * injectable value (logical id) as well as method used for assigning
 * value (setter or field)
 * 
 * @since 1.9
 */
public class ValueInjector
    extends BeanProperty.Std
{
    /**
     * Identifier used for looking up value to inject
     */
    protected final Object _valueId;

    public ValueInjector(String propertyName, JavaType type,
            Annotations contextAnnotations, AnnotatedMember mutator,
            Object valueId)
    {
        super(propertyName, type, contextAnnotations, mutator);
        _valueId = valueId;
    }

    public Object findValue(DeserializationContext context, Object beanInstance)
    {
        return context.findInjectableValue(_valueId, this, beanInstance);
    }
    
    public void inject(DeserializationContext context, Object beanInstance)
        throws IOException
    {
        _member.setValue(beanInstance, findValue(context, beanInstance));
    }
}