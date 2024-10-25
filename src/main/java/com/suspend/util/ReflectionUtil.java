package com.suspend.util;

import com.suspend.annotation.*;
import com.suspend.core.exception.SuspendException;
import com.suspend.mapping.FetchType;
import com.suspend.mapping.Relationship;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ReflectionUtil {

    private ReflectionUtil() {
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(name -> !name.isEmpty())
                .orElse(field.getName());
    }

    public static String getTableName(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(Table.class))
                .map(Table::name)
                .filter(name -> !name.isEmpty())
                .orElse(clazz.getSimpleName());
    }

    public static Set<Class<?>> getEntityClasses(String packageName) {
        Reflections reflections = new Reflections(packageName);

        return reflections.getTypesAnnotatedWith(Entity.class);
    }

    public static Class<?> getFieldType(Field field) {
        if (List.class.isAssignableFrom(field.getType())) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType paramType) {
                return (Class<?>) paramType.getActualTypeArguments()[0];
            } else {
                return field.getType(); // Return the raw List type if no generic type is available
            }
        } else if (Set.class.isAssignableFrom(field.getType())) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType paramType) {
                return (Class<?>) paramType.getActualTypeArguments()[0];
            } else {
                return field.getType();
            }
        } else if (Map.class.isAssignableFrom(field.getType())) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType paramType) {
                return (Class<?>) paramType.getActualTypeArguments()[1];
            } else {
                return field.getType();
            }
        }
        return field.getType();
    }

    public static Field getIdField(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    public static Method getGetterMethod(Class<?> entityClass, Field field) {
        String fieldName = field.getName();
        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        try {
            return entityClass.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No getter found for field: " + fieldName);
        }
    }

    public static Object getFieldValue(Object entity, Field field) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new SuspendException("Couldn't access field " + field.getName(), e);
        }
    }

    public static ElementMatcher<MethodDescription> isGetterForLazyLoadedFields(List<Relationship> relationships) {
        return methodDescription -> {
            for (Relationship relationship : relationships) {
                String getterName = ReflectionUtil.getGetterMethod(
                        relationship.getField().getDeclaringClass(),
                        relationship.getField()
                ).getName();
                if (methodDescription.getName().equals(getterName)) {
                    return relationship.getFetchingType().equals(FetchType.LAZY);
                }
            }
            return false;
        };
    }

    public static <T> void copyValues(T source, T target) {
        Field[] fields = source.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(source);
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to copy value for field: " + field.getName(), e);
            }
        }
    }

    public static Field getEntityField(Class<?> entityClass, String columnName) {
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


    public static Object getValueForIdField(Object entity) {
        Field idField = getIdField(entity.getClass());
        return getFieldValue(entity, idField);
    }

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SuspendException("Failed to set field: " + fieldName, e);
        }
    }
}
