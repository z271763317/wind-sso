package org.wind.tool.third.jackson.map.jsontype.impl;

import org.wind.tool.third.jackson.annotate.JsonTypeInfo.As;
import org.wind.tool.third.jackson.map.BeanProperty;
import org.wind.tool.third.jackson.map.jsontype.TypeIdResolver;
import org.wind.tool.third.jackson.type.JavaType;

/**
 * Type deserializer used with {@link As#EXTERNAL_PROPERTY} inclusion mechanism.
 * Actual implementation may look bit strange since it depends on comprehensive
 * pre-processing done by {@link org.wind.tool.third.jackson.map.deser.BeanDeserializer}
 * to basically transform external type id into structure that looks more like
 * "wrapper-array" style inclusion. This intermediate form is chosen to allow
 * supporting all possible JSON structures.
 * 
 * @since 1.9
 */
public class AsExternalTypeDeserializer extends AsArrayTypeDeserializer
{
    protected final String _typePropertyName;
    
    public AsExternalTypeDeserializer(JavaType bt, TypeIdResolver idRes, BeanProperty property,
            Class<?> defaultImpl,
            String typePropName)
    {
        super(bt, idRes, property, defaultImpl);
        _typePropertyName = typePropName;
    }

    @Override
    public As getTypeInclusion() {
        return As.EXTERNAL_PROPERTY;
    }

    @Override
    public String getPropertyName() { return _typePropertyName; }
}
