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
        try {
            Session session = SessionFactoryImpl.getInstance().getCurrentSession();
            if (loadedData.isEmpty())
                loadedData = (List<E>) fetchStrategy.fetch(entityReference, relationship, entityMapper, method);

            E fetchedEntity = loadedData.isEmpty() ? null : loadedData.get(0);

            if (fetchedEntity != null) {
                updateEntityReferenceDynamically(parentEntity, fetchedEntity);
            }

            updateEntityReferenceInContainer((SessionImpl) session, fetchedEntity);

        } catch (SuspendException e) {
            throw new LazyInitializationException("Failed to lazily initialize entity", e);
        }

        return method.invoke(loadedData.isEmpty() ? null : loadedData.get(0), objects);
    }

    private void updateEntityReferenceDynamically(Object parentEntity, E fetchedEntity) {
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) SessionFactoryImpl.getInstance();
        EntityMetadata parentMetadata = sessionFactory.getEntityMetadata(parentEntity.getClass());

        for (Relationship relationship : parentMetadata.getRelationships()) {
            if (relationship.getRelatedEntity().equals(fetchedEntity.getClass())) {
                ReflectionUtil.setField(parentEntity, relationship.getField().getName(), fetchedEntity);
            }
        }
    }

    private void updateEntityReferenceInContainer(SessionImpl session, E fetchedEntity) {
        Object fetchedEntityId = ReflectionUtil.getValueForIdField(fetchedEntity);
        EntityReference fetchedEntityRef = session.getEntityReference(fetchedEntity.getClass(), fetchedEntityId);

        if (fetchedEntityRef == null) {
            fetchedEntityRef = new EntityReference(fetchedEntity.getClass(), fetchedEntity, fetchedEntityId, true, true, null);
            session.addEntityReference(fetchedEntityRef);
        }

        fetchedEntityRef.setFullyProcessed(true);
    }
}
