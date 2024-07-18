package com.suspend.mapping;

import com.suspend.annotation.*;
import com.suspend.core.exception.SuspendException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.sql.Connection;

public class EntityMapper {
    private final Connection connection;
    private final EntityReference entityReference;

    public EntityMapper(Connection connection) {
        this.connection = connection;
        entityReference = new EntityReference();
    }

    public <T> List<T> mapResultSet(ResultSet resultSet, Class<T> entityClass, Set<Object> processedEntityIds) {
        List<T> results = new ArrayList<>();

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> fieldValues = new HashMap<>();
                T entity = createEntityInstance(entityClass);

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    Field field = getEntityField(entityClass, columnName);

                    if (field != null) {
                        if (field.isAnnotationPresent(ManyToOne.class)) {
                            fieldValues.put(columnName, value);
                        } else {
                            try {
                                field.setAccessible(true);
                                field.set(entity, value);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("Failed to set value to field " + field.getName(), e);
                            }
                        }
                    }
                }
                results.add(entity);
                mapRelationships(entity, entityClass, fieldValues, processedEntityIds);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }

        return results;
    }

    private <T> T createEntityInstance(Class<T> entityClass) {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException  | NoSuchMethodException |InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create entity instance for class: " + entityClass.getName(), e);
        }
    }

    private Field getEntityField(Class<?> entityClass, String columnName) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.name().equals(columnName)) {
                    return field;
                }
            } else if (field.getName().equals(columnName)) {
                return field;
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
                if (joinColumnAnnotation != null && joinColumnAnnotation.name().equals(columnName)) {
                    return field;
                }
            }
        }
        return null;
    }

    private <T> void mapRelationships(Object entity, Class<T> entityClass, Map<String, Object> fieldValues, Set<Object> processedEntityIds) {
        Field idField = findIdField(entityClass);
        idField.setAccessible(true);
        Object entityId;
        try {
            entityId = idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access entity ID field", e);
        }

        if (processedEntityIds.contains(entityId)) {
            return;
        }
        processedEntityIds.add(entityId);

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                String mappedBy = oneToMany.mappedBy();
                Class<?> targetEntity = getTargetEntityType(field);
                List<?> relatedEntities = fetchRelatedEntities(targetEntity, entity, field, mappedBy, processedEntityIds);

                try {
                    field.setAccessible(true);
                    field.set(entity, relatedEntities);
                } catch (IllegalAccessException e) {
                    throw new SuspendException("Failed to set value to field " + field.getName(), e);
                }
            }

            if (field.isAnnotationPresent(ManyToOne.class)) {
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                Class<?> targetEntity = getTargetEntityType(field);
                Object relatedEntity = fetchRelatedEntity(targetEntity, joinColumn, fieldValues, processedEntityIds);
                try {
                    field.setAccessible(true);
                    field.set(entity, relatedEntity);
                } catch (IllegalAccessException e) {
                    throw new SuspendException("Failed to set value to field " + field.getName(), e);
                }
            }
        }
    }

    private Class<?> getTargetEntityType(Field field) {
        if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            return (Class<?>) genericType.getActualTypeArguments()[0];
        } else if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
            return field.getType();
        } else {
            throw new IllegalArgumentException("Field does not have a valid relationship annotation");
        }
    }

    private List<?> fetchRelatedEntities(Class<?> targetEntityClass, Object parentEntity, Field relationField, String mappedBy, Set<Object> processedEntityIds) {
        try {
            String targetTableName = getTableName(targetEntityClass);
            Field mappedByField = targetEntityClass.getDeclaredField(mappedBy);
            mappedByField.setAccessible(true);

            List<Object> relatedEntities = new ArrayList<>();
            Set<Object> processedIdsCopy = new HashSet<>(processedEntityIds);

            for (Object processedId : processedIdsCopy) {
                Object cachedEntity = entityReference.getProcessedEntity(targetEntityClass, processedId);
                if (cachedEntity != null) {
                    relatedEntities.add(cachedEntity);
                    processedEntityIds.remove(processedId);
                }
            }

            if (!processedEntityIds.isEmpty()) {
                if (mappedByField.isAnnotationPresent(ManyToOne.class)) {
                    String mappedByColumnName = getColumnName(targetEntityClass, mappedByField.getName());
                    String sql = "SELECT * FROM " + targetTableName + " WHERE " + mappedByColumnName + " = ?";

                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        for (Object processedId : processedEntityIds) {
                            statement.setObject(1, processedId);
                            ResultSet resultSet = statement.executeQuery();
                            List<?> results = mapResultSet(resultSet, targetEntityClass, processedEntityIds);
                            relatedEntities.addAll(results);
                            for (Object result : results) {
                                entityReference.addProcessedEntity(targetEntityClass, processedId, result);
                            }
                        }
                    }
                }
            }
            return relatedEntities;
        } catch (SQLException | NoSuchFieldException e) {
            throw new SuspendException("Failed to fetch related entities", e);
        }
    }

    private Object fetchRelatedEntity(Class<?> targetEntityClass, JoinColumn joinColumn, Map<String, Object> fieldValues, Set<Object> processedEntityIds) {
        try {
            Object relatedEntity = entityReference.getProcessedEntity(targetEntityClass, fieldValues.get(joinColumn.name()));

            if (relatedEntity == null) {
                String targetTableName = getTableName(targetEntityClass);
                String sql = "SELECT * FROM " + targetTableName + " WHERE " + joinColumn.referencedColumnName() + " = ?";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setObject(1, fieldValues.get(joinColumn.name()));
                    ResultSet resultSet = statement.executeQuery();
                    List<?> results = mapResultSet(resultSet, targetEntityClass, processedEntityIds);
                    relatedEntity = results.isEmpty() ? null : results.get(0);
                    entityReference.addProcessedEntity(targetEntityClass, fieldValues.get(joinColumn.name()), relatedEntity);
                }
            }

            return relatedEntity;
        } catch (SQLException e) {
            throw new SuspendException("Failed to fetch related entity", e);
        }
    }

    private Field findIdField(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new SuspendException("No @Id annotation found for class: " + entityClass.getName());
    }

    private String getTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            return tableAnnotation.name();
        } else {
            return entityClass.getSimpleName().toLowerCase();
        }
    }

    private String getColumnName(Class<?> entityClass, String fieldName) throws NoSuchFieldException {
        Field field = entityClass.getDeclaredField(fieldName);
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.name();
        } else if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            return joinColumn.name();
        } else {
            return fieldName;
        }
    }

    private Object findProcessedEntity(Object entityId, Class<?> entityClass, Map<Class<?>, Set<Object>> processedEntities) {
        Set<Object> processedIds = processedEntities.get(entityClass);
        for (Object processedEntity : processedIds) {
            Field idField = findIdField(entityClass);
            idField.setAccessible(true);
            try {
                Object id = idField.get(processedEntity);
                if (id != null && id.equals(entityId)) {
                    return processedEntity;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access entity ID field", e);
            }
        }
        return null;
    }
}
