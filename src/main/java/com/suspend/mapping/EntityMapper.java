package com.suspend.mapping;

import com.suspend.annotation.*;
import com.suspend.core.exception.SuspendException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.*;

public class EntityMapper {
    private final Connection connection;
    private final EntityReferenceContainer entityReferenceContainer;

    public EntityMapper(Connection connection) {
        this.connection = connection;
        entityReferenceContainer = new EntityReferenceContainer();
    }

    public <T> List<T> mapResultSet(ResultSet resultSet, Class<T> entityClass, Set<EntityKey> processedEntities) {
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
                mapRelationships(entity, entityClass, fieldValues, processedEntities);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }

        return results;
    }

    private <T> T createEntityInstance(Class<T> entityClass) {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
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

    private <T> void mapRelationships(Object entity, Class<T> entityClass, Map<String, Object> fieldValues, Set<EntityKey> processedEntities) {
        Field idField = findIdField(entityClass);
        idField.setAccessible(true);
        Object entityId;
        try {
            entityId = idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access entity ID field", e);
        }

        EntityKey entityKey = new EntityKey(entityId, entityClass);

        if (processedEntities.contains(entityKey)) {
            EntityReference entityReference = entityReferenceContainer.getProcessedEntity(entityClass, entityId);
            if (entityReference != null && !entityReference.isProcessing()) {
                return;
            }
        } else {
            processedEntities.add(entityKey);
            entityReferenceContainer.addProcessedEntity(entityClass, entityId, new EntityReference(entity, entityId));
        }

        EntityReference currentEntityReference = entityReferenceContainer.getProcessedEntity(entityClass, entityId);
        currentEntityReference.setProcessing(true);

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                String mappedBy = oneToMany.mappedBy();
                Class<?> targetEntity = getTargetEntityType(field);
                List<Object> relatedEntities = fetchRelatedEntities(targetEntity, mappedBy, processedEntities, currentEntityReference);
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
                Object relatedEntity = fetchRelatedEntity(targetEntity, joinColumn, fieldValues, processedEntities);
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

    private List<Object> fetchRelatedEntities(Class<?> targetEntityClass, String mappedBy, Set<EntityKey> processedEntities, EntityReference entityReference) {
        try {
            String targetTableName = getTableName(targetEntityClass);
            Field mappedByField = targetEntityClass.getDeclaredField(mappedBy);
            mappedByField.setAccessible(true);

            List<Object> relatedEntities = new ArrayList<>();

            if (mappedByField.isAnnotationPresent(ManyToOne.class)) {
                String mappedByColumnName = getColumnName(targetEntityClass, mappedByField.getName());
                String sql = "SELECT * FROM " + targetTableName + " WHERE " + mappedByColumnName + " = ?";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    Object entityId = entityReference.getEntityId();
                    statement.setObject(1, entityId);
                    ResultSet resultSet = statement.executeQuery();
                    List<?> results = mapResultSet(resultSet, targetEntityClass, processedEntities);
                    for (Object result : results) {
                        Object resultEntityId = getEntityId(result);
                        EntityReference cachedEntityReference = entityReferenceContainer.getProcessedEntity(targetEntityClass, resultEntityId);
                        if (cachedEntityReference != null) {
                            relatedEntities.add(cachedEntityReference.getEntity());
                        } else {
                            EntityReference e = new EntityReference(result, resultEntityId);
                            relatedEntities.add(result);
                            entityReferenceContainer.addProcessedEntity(targetEntityClass, resultEntityId, e);
                        }
                    }
                }
            }
            return relatedEntities;
        } catch (SQLException | NoSuchFieldException e) {
            throw new SuspendException("Failed to fetch related entities", e);
        }
    }

    private Object fetchRelatedEntity(Class<?> targetEntityClass, JoinColumn joinColumn, Map<String, Object> fieldValues, Set<EntityKey> processedEntities) {
        try {
            Object id = fieldValues.get(joinColumn.name());
            EntityReference relatedEntityReference = entityReferenceContainer.getProcessedEntity(targetEntityClass, id);

            if (relatedEntityReference != null) {
                return relatedEntityReference.getEntity();
            }

            String targetTableName = getTableName(targetEntityClass);
            String sql = "SELECT * FROM " + targetTableName + " WHERE " + joinColumn.referencedColumnName() + " = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, id);
                ResultSet resultSet = statement.executeQuery();
                List<?> results = mapResultSet(resultSet, targetEntityClass, processedEntities);
                if (results.isEmpty()) {
                    return null;
                }
                relatedEntityReference = new EntityReference(results.get(0), id);
                entityReferenceContainer.addProcessedEntity(targetEntityClass, fieldValues.get(joinColumn.name()), relatedEntityReference);
                return relatedEntityReference.getEntity();
            }
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

    //    private List<Object> removeDuplicates(List<Object> relatedEntities) {
//        Map<Object, Object> uniqueEntities = new LinkedHashMap<>(); // LinkedHashMap maintains insertion order
//
//        for (Object entity : relatedEntities) {
//            Object entityId = getEntityId(entity); // Assuming getEntityId method retrieves the ID of the entity
//            uniqueEntities.putIfAbsent(entityId, entity); // Add only if the ID is not already present
//        }
//
//        return new ArrayList<>(uniqueEntities.values());
//    }
//
    private Object getEntityId(Object entity) {
        try {
            // Assuming the ID field is annotated with @Id
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    return field.get(entity);
                }
            }
            throw new RuntimeException("No ID field found on entity " + entity.getClass().getName());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access entity ID field", e);
        }
    }
}
