package com.suspend.mapping;

import com.suspend.annotation.*;
import com.suspend.configuration.Configuration;
import com.suspend.core.SessionFactory;
import com.suspend.core.exception.SuspendException;
import com.suspend.core.internal.SessionFactoryImpl;
import com.suspend.mapping.fetching.Bag;
import com.suspend.mapping.fetching.FetchStrategy;
import com.suspend.mapping.fetching.FetchingStrategyFactory;
import com.suspend.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class EntityMapper {
    private final EntityReferenceContainer entityReferenceContainer;
    private final EntityMetadataContainer entityMetadataContainer;

    public EntityMapper() {
        this.entityReferenceContainer = SessionFactoryImpl.getInstance().getEntityReferenceContainer();
        this.entityMetadataContainer = Configuration.getInstance().getEntityMetadataContainer();
    }

    public <T> List<T> mapResultSet(ResultSet resultSet, Class<T> entityClass) {
        List<T> results = new ArrayList<>();

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                T entity = ReflectionUtil.newInstance(entityClass);

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    Field field = ReflectionUtil.getEntityField(entityClass, columnName);

                    if (field != null && !isRelationshipField(field)) {
                        try {
                            field.setAccessible(true);
                            field.set(entity, value);
                        } catch (IllegalAccessException e) {
                            throw new SuspendException("Failed to set value to field " + field.getName(), e);
                        }
                    }
                }
                EntityReference entityReference = new EntityReference(entityClass, entity, ReflectionUtil.getValueForIdField(entity), false, false);
                entityReferenceContainer.addEntityReference(entityReference);

                mapRelationships(entity);
                results.add(entity);
            }
        } catch (SQLException e) {
            throw new SuspendException("Failed to execute query", e);
        }

        return results;
    }

    private void mapRelationships(Object entity) {
        Class<?> entityClass = entity.getClass();
        Object entityId = ReflectionUtil.getValueForIdField(entity);

        EntityReference entityReference = entityReferenceContainer.getEntityReference(entityClass, entityId);

        if (entityReference != null && entityReference.isFullyProcessed()) {
            return;
        }

        EntityMetadata entityMetadata = entityMetadataContainer.getEntityMetadata(entity.getClass());

        for (Relationship relationship : entityMetadata.getRelationships()) {
            FetchStrategy fetchStrategy = FetchingStrategyFactory.getFetchStrategy(relationship.getFetchingType());

            Field field = relationship.getField();
            try {
                Object value = fetchStrategy.fetch(entity, relationship, this, null);
                field.setAccessible(true);
                field.set(entity, value);
            } catch (SQLException | IllegalAccessException e) {
                throw new SuspendException(e);
            }
        }
        if (entityReference != null) {
            entityReference.setFullyProcessed(true);
        } else {
            entityReference = new EntityReference(entityClass, entity, entityId, true, false);
            entityReferenceContainer.addEntityReference(entityReference);
        }
    }

    private boolean isRelationshipField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)
               || field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class);
    }
}
