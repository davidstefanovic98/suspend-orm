package com.suspend.mapping.fetching;

import com.suspend.core.exception.SuspendException;
import com.suspend.core.internal.SessionFactoryImpl;
import com.suspend.mapping.EntityMapper;
import com.suspend.mapping.EntityReference;
import com.suspend.mapping.Relationship;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;

public class ProxyFactory {

    public static <E> E createProxy(EntityReference entityReference, Class<?> entity, Relationship relationship, EntityMapper entityMapper) {
        try {
            return (E) new ByteBuddy()
                    .with(new NamingStrategy.SuffixingRandom("SuspendProxy"))
                    .subclass(entity)
                    .method(ElementMatchers.any())
                    .intercept(InvocationHandlerAdapter.of(new EntityProxyInterceptor<>(entityReference, entityReference.getEntity(), relationship, entityMapper, FetchingStrategyFactory.getFetchStrategy(relationship.getFetchingType()))))
                    .make()
                    .load(entity.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new SuspendException("Cannot create a proxy", e);
        }
    }

    public static <E> Bag<E> createBagProxy(EntityReference parentEntity, Relationship relationship, EntityMapper entityMapper) {
        try {
            TypeDescription.Generic genericBagType = TypeDescription.Generic.Builder.parameterizedType(Bag.class, relationship.getRelatedEntity()).build();
            return (Bag<E>) new ByteBuddy()
                    .with(new NamingStrategy.SuffixingRandom("SuspendProxy"))
                    .subclass(genericBagType)
                    .implement(List.class)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new BagProxyInterceptor<>(parentEntity, parentEntity.getEntity(), relationship, entityMapper, FetchingStrategyFactory.getFetchStrategy(relationship.getFetchingType()))))
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
