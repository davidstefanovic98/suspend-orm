package com.suspend.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntityMapper {

    public <T> List<T> mapResultSet(ResultSet resultSet, Class<T> entityClass) {
        List<T> results = new ArrayList<>();

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                T entity = createEntityInstance(entityClass);
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    Field field = getEntityField(entityClass, columnName);
                    if (field != null) {
                        try {
                            field.setAccessible(true);
                            field.set(entity, value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Failed to set value to field " + field.getName(), e);
                        }
                    }
                }
                results.add(entity);
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
            if (field.getName().equals(columnName)) {
                return field;
            }
        }
        return null;
    }
}
