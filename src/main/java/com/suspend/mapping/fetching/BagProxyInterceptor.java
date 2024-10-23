package com.suspend.mapping.fetching;

import com.suspend.mapping.EntityMapper;
import com.suspend.mapping.Relationship;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class BagProxyInterceptor<E> implements InvocationHandler {
    private final Object parentEntity;
    private final Relationship relationship;
    private final EntityMapper entityMapper;
    private final FetchStrategy fetchStrategy;
    private Bag<E> loadedData = new Bag<>();

    public BagProxyInterceptor(Object parentEntity, Relationship relationship, EntityMapper entityMapper, FetchStrategy fetchStrategy) {
        this.parentEntity = parentEntity;
        this.relationship = relationship;
        this.entityMapper = entityMapper;
        this.fetchStrategy = fetchStrategy;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        List<E> fetchedData = (List<E>) fetchStrategy.fetch(parentEntity, relationship, entityMapper, method);

        if (loadedData.isEmpty())
            loadedData.addAll(fetchedData);

        return method.invoke(loadedData, objects);
    }
}
