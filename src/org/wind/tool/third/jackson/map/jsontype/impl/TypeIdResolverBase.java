package org.wind.tool.third.jackson.map.jsontype.impl;

import org.wind.tool.third.jackson.map.jsontype.TypeIdResolver;
import org.wind.tool.third.jackson.map.type.TypeFactory;
import org.wind.tool.third.jackson.type.JavaType;

public abstract class TypeIdResolverBase
    implements TypeIdResolver
{
    protected final TypeFactory _typeFactory;

    /**
     * Common base type for all polymorphic instances handled.
     */
    protected final JavaType _baseType;

    protected TypeIdResolverBase(JavaType baseType, TypeFactory typeFactory)
    {
        _baseType = baseType;
        _typeFactory = typeFactory;
    }

    @Override
    public void init(JavaType bt) {
        /* Standard type id resolvers do not need this;
         * only useful for custom ones.
         */
    }
}
