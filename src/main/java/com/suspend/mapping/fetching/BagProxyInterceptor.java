package com.suspend.mapping.fetching;

import com.suspend.mapping.EntityMapper;
import com.suspend.mapping.EntityReference;
import com.suspend.mapping.Relationship;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.List;

public class BagProxyInterceptor<E> {
    private final EntityReference entityReference;
    private final E parentEntity;
    private final Relationship relationship;
    private final EntityMapper entityMapper;
    private final FetchStrategy fetchStrategy;
    private final Bag<E> loadedData = new Bag<>();

    public BagProxyInterceptor(EntityReference entityReference, E parentEntity, Relationship relationship, EntityMapper entityMapper, FetchStrategy fetchStrategy) {
        this.entityReference = entityReference;
        this.parentEntity = parentEntity;
        this.relationship = relationship;
        this.entityMapper = entityMapper;
        this.fetchStrategy = fetchStrategy;
    }

    @RuntimeType
    public Object intercept(@Origin(cache = false) Method method, @RuntimeType @AllArguments Object[] arguments) throws Throwable {
        if (loadedData.isEmpty()) {
            List<E> data = (List<E>) fetchStrategy.fetch(entityReference, relationship, entityMapper, method);
            loadedData.addAll(data);
        }
        return method.invoke(loadedData, arguments);
    }
}
