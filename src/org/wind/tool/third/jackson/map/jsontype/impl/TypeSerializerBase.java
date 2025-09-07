package org.wind.tool.third.jackson.map.jsontype.impl;

import org.wind.tool.third.jackson.annotate.JsonTypeInfo;
import org.wind.tool.third.jackson.map.BeanProperty;
import org.wind.tool.third.jackson.map.TypeSerializer;
import org.wind.tool.third.jackson.map.jsontype.TypeIdResolver;

/**
 * @since 1.5
 */
public abstract class TypeSerializerBase extends TypeSerializer
{
    protected final TypeIdResolver _idResolver;

    protected final BeanProperty _property;
    
    protected TypeSerializerBase(TypeIdResolver idRes, BeanProperty property)
    {
        _idResolver = idRes;
        _property = property;
    }

    @Override
    public abstract JsonTypeInfo.As getTypeInclusion();

    @Override
    public String getPropertyName() { return null; }
    
    @Override
    public TypeIdResolver getTypeIdResolver() { return _idResolver; }
}
