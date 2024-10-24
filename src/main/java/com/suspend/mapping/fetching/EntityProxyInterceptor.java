package com.suspend.mapping.fetching;

import com.suspend.mapping.EntityMapper;
import com.suspend.mapping.EntityReference;
import com.suspend.mapping.Relationship;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EntityProxyInterceptor<E> implements InvocationHandler {
    private final EntityReference entityReference;
    private final E parentEntity;
    private final Relationship relationship;
    private final EntityMapper entityMapper;
    private final FetchStrategy fetchStrategy;
    private List<E> loadedData = new ArrayList<>();

    public EntityProxyInterceptor(EntityReference entityReference, E parentEntity, Relationship relationship, EntityMapper entityMapper, FetchStrategy fetchStrategy) {
        this.entityReference = entityReference;
        this.parentEntity = parentEntity;
        this.relationship = relationship;
        this.entityMapper = entityMapper;
        this.fetchStrategy = fetchStrategy;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (loadedData.isEmpty())
            loadedData = (List<E>) fetchStrategy.fetch(entityReference, relationship, entityMapper, method);

        return method.invoke(loadedData.isEmpty() ? null : loadedData.get(0), objects);
    }
}
