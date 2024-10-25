package com.suspend.mapping;

import com.suspend.annotation.*;
import com.suspend.configuration.Configuration;
import com.suspend.core.SessionFactory;
import com.suspend.core.exception.SuspendException;
import com.suspend.core.internal.SessionFactoryImpl;
import com.suspend.core.internal.SessionImpl;
import com.suspend.mapping.fetching.FetchStrategy;
import com.suspend.mapping.fetching.FetchingStrategyFactory;
import com.suspend.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class EntityMapper {
    private final SessionFactoryImpl sessionFactory;
    private final SessionImpl session;

    public EntityMapper() {
        this.sessionFactory = (SessionFactoryImpl) SessionFactoryImpl.getInstance();
        this.session = (SessionImpl) sessionFactory.getCurrentSession();
    }

    public <T> List<T> mapResultSet(ResultSet resultSet, Class<T> entityClass) {
        List<T> results = new ArrayList<>();
        List<ForeignKey> foreignKeys = new ArrayList<>();

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

                    for (Field f : entityClass.getDeclaredFields()) {
                        if (f.isAnnotationPresent(ManyToOne.class)) {
                            JoinColumn joinColumn = f.getAnnotation(JoinColumn.class);
                            if (joinColumn != null && columnName.equals(joinColumn.name())) {
                                ForeignKey foreignKey = new ForeignKey(value, joinColumn.name(), entityClass, f.getType());
                                foreignKeys.add(foreignKey);
                            }
                        }
                    }
                }
                EntityReference entityReference = new EntityReference(entityClass, entity, ReflectionUtil.getValueForIdField(entity), false, false, foreignKeys);
                session.addEntityReference(entityReference);

                mapRelationships(entity, foreignKeys);
                results.add(entity);
            }
        } catch (SQLException e) {
            throw new SuspendException("Failed to execute query", e);
        }

        return results;
    }

    private void mapRelationships(Object entity, List<ForeignKey> foreignKeys) {
        Class<?> entityClass = entity.getClass();
        Object entityId = ReflectionUtil.getValueForIdField(entity);

        EntityReference entityReference = session.getEntityReference(entityClass, entityId);

        if (entityReference != null && entityReference.isFullyProcessed()) {
            if (entityReference.getEntity() != null) {
                entityReference.setEntity(entity);
                return;
            }
        }

        EntityMetadata entityMetadata = sessionFactory.getEntityMetadata(entity.getClass());

        for (Relationship relationship : entityMetadata.getRelationships()) {
            FetchStrategy fetchStrategy = FetchingStrategyFactory.getFetchStrategy(relationship.getFetchingType());

            Field field = relationship.getField();
            try {
                Object value;
                ForeignKey foreignKey = getForeignKeyForRelationship(foreignKeys, relationship);
                if (foreignKey != null) {
                    EntityReference relatedReference = session.getEntityReference(relationship.getRelatedEntity(), foreignKey.getValue());
                    if (relatedReference != null && relatedReference.isFullyProcessed()) {
                        value = relatedReference.getEntity();
                    } else {
                        value = fetchStrategy.fetch(entityReference, relationship, this, null);
                    }
                } else {
                    value = fetchStrategy.fetch(entityReference, relationship, this, null);
                }
                field.setAccessible(true);
                field.set(entity, value);
            } catch (SQLException | IllegalAccessException e) {
                throw new SuspendException(e);
            }
        }
        if (entityReference != null) {
            entityReference.setEntity(entity);
            entityReference.setFullyProcessed(true);
        } else {
            entityReference = new EntityReference(entityClass, entity, entityId, true, false, foreignKeys);
            session.addEntityReference(entityReference);
        }
    }

    private boolean isRelationshipField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)
               || field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class);
    }

    private ForeignKey getForeignKeyForRelationship(List<ForeignKey> foreignKeys, Relationship relationship) {
        for (ForeignKey foreignKey : foreignKeys) {
            if (foreignKey.getName().equals(relationship.getForeignKeyField())) {
                return foreignKey;
            }
        }
        return null;
    }
}
