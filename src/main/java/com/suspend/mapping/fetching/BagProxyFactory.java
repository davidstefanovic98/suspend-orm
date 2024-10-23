package com.suspend.mapping.fetching;

import com.suspend.core.exception.SuspendException;
import com.suspend.mapping.EntityMapper;
import com.suspend.mapping.Relationship;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;

public class BagProxyFactory {

    public static <E> Bag<E> createBagProxy(Object parentEntity, Relationship relationship, EntityMapper entityMapper) {
        try {
            return new ByteBuddy()
                    .subclass(Bag.class)
                    .implement(List.class)
                    .method(ElementMatchers.any())
                    .intercept(InvocationHandlerAdapter.of(new BagProxyInterceptor<>(parentEntity, relationship, entityMapper, FetchingStrategyFactory.getFetchStrategy(relationship.getFetchingType()))))
                    .make()
                    .load(Bag.class.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new SuspendException("Cannot create a proxy", e);
        }
    }
}
