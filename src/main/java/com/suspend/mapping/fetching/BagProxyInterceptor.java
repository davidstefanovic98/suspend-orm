package com.suspend.mapping.fetching;

import com.suspend.core.Session;
import com.suspend.core.exception.SuspendException;
import com.suspend.core.internal.SessionFactoryImpl;
import com.suspend.core.internal.SessionImpl;
import com.suspend.mapping.EntityMapper;
import com.suspend.mapping.EntityMetadata;
import com.suspend.mapping.EntityReference;
import com.suspend.mapping.Relationship;
import com.suspend.mapping.fetching.exception.LazyInitializationException;
import com.suspend.util.ReflectionUtil;
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
        try {
            Session session = SessionFactoryImpl.getInstance().getCurrentSession();
            if (loadedData.isEmpty()) {
                List<E> data = (List<E>) fetchStrategy.fetch(entityReference, relationship, entityMapper, method);
                loadedData.addAll(data);

                updateEntityReference(parentEntity, data);

                updateEntityReferencesInContainer((SessionImpl) session, data);
            }
        } catch (SuspendException e) {
            throw new LazyInitializationException("Failed to lazily initialize collection of entities", e);
        }

        return method.invoke(loadedData, arguments);
    }

    private void updateEntityReference(Object parentEntity, List<E> fetchedEntities) {
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) SessionFactoryImpl.getInstance();
        EntityMetadata parentMetadata = sessionFactory.getEntityMetadata(parentEntity.getClass());

        for (E fetchedEntity : fetchedEntities) {
            for (Relationship relationship : parentMetadata.getRelationships()) {
                if (relationship.getRelatedEntity().equals(fetchedEntity.getClass())) {
                    ReflectionUtil.setField(parentEntity, relationship.getField().getName(), fetchedEntities);
                }
            }
        }
    }

    private void updateEntityReferencesInContainer(SessionImpl session, List<E> fetchedEntities) {
        for (E fetchedEntity : fetchedEntities) {
            Object fetchedEntityId = ReflectionUtil.getValueForIdField(fetchedEntity);
            EntityReference fetchedEntityRef = session.getEntityReference(fetchedEntity.getClass(), fetchedEntityId);

            if (fetchedEntityRef == null) {
                fetchedEntityRef = new EntityReference(fetchedEntity.getClass(), fetchedEntity, fetchedEntityId, true, true, null);
                session.addEntityReference(fetchedEntityRef);
            }
            fetchedEntityRef.setFullyProcessed(true);
        }
    }
}
