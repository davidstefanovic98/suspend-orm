package com.suspend.mapping;

import com.suspend.annotation.*;
import com.suspend.core.exception.SuspendException;
import com.suspend.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RelationshipResolver {

    private RelationshipResolver() {}

    public static List<Relationship> getRelationships(Class<?> entityClass) {
        List<Relationship> relationships = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                relationships.add(createRelationship(field, RelationshipType.ONE_TO_MANY));
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                relationships.add(createRelationship(field, RelationshipType.MANY_TO_ONE));
            } else if (field.isAnnotationPresent(ManyToMany.class)) {
                relationships.add(createRelationship(field, RelationshipType.MANY_TO_MANY));
            } else if (field.isAnnotationPresent(OneToOne.class)) {
                relationships.add(createRelationship(field, RelationshipType.ONE_TO_ONE));
            }
        }
        return relationships;
    }

    private static Relationship createRelationship(Field field, RelationshipType type) {
        Class<?> relatedEntity = ReflectionUtil.getFieldType(field);
        FetchType fetchType = getFetchType(field);

        return new Relationship(relatedEntity, type, fetchType, field, getForeignKeyField(field), getPrimaryKeyField(field));
    }

    private static FetchType getFetchType(Field field) {
        if (field.isAnnotationPresent(OneToMany.class)) {
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            return oneToMany.fetch();
        } else if (field.isAnnotationPresent(ManyToOne.class)) {
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            return manyToOne.fetch();
        } else if (field.isAnnotationPresent(OneToOne.class)) {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            return oneToOne.fetch();
        } else if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            return manyToMany.fetch();
        }
        return FetchType.LAZY;
    }

    private static String getForeignKeyField(Field field) {
        if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            return joinColumn.name();
        }

        if (field.isAnnotationPresent(JoinTable.class)) {
            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            return joinTable.joinColumns()[0].name();
        }

        if (field.isAnnotationPresent(OneToMany.class) && !field.isAnnotationPresent(JoinColumn.class)) {
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (!oneToMany.mappedBy().isEmpty()) {
                return getPrimaryKeyFieldFromOwningSide(ReflectionUtil.getFieldType(field), oneToMany.mappedBy());
            }
        }

        return "";
    }

    private static String getPrimaryKeyField(Field field) {
        if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            return joinColumn.referencedColumnName();
        }

        if (field.isAnnotationPresent(JoinTable.class)) {
            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            return joinTable.joinColumns()[0].referencedColumnName();
        }

        if (field.isAnnotationPresent(OneToMany.class) && !field.isAnnotationPresent(JoinColumn.class)) {
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (!oneToMany.mappedBy().isEmpty()) {
                return getForeignKeyFieldFromOwningSide(ReflectionUtil.getFieldType(field), oneToMany.mappedBy());
            }
            return "";
        }

        return "";
    }

    private static String getPrimaryKeyFieldFromOwningSide(Class<?> entityClass, String mappedByFieldName) {
        try {
            Field mappedByField = entityClass.getDeclaredField(mappedByFieldName);
            if (mappedByField.isAnnotationPresent(JoinColumn.class)) {
                JoinColumn joinColumn = mappedByField.getAnnotation(JoinColumn.class);
                return joinColumn.name();
            }
        } catch (NoSuchFieldException e) {
            throw new SuspendException(String.format("Field with name %s not found", mappedByFieldName));
        }

        return null;
    }

    private static String getForeignKeyFieldFromOwningSide(Class<?> entityClass, String mappedByFieldName) {
        try {
            Field mappedByField = entityClass.getDeclaredField(mappedByFieldName);
            if (mappedByField.isAnnotationPresent(JoinColumn.class)) {
                JoinColumn joinColumn = mappedByField.getAnnotation(JoinColumn.class);
                return joinColumn.referencedColumnName();
            }
        } catch (NoSuchFieldException e) {
            throw new SuspendException(String.format("Field with name %s not found", mappedByFieldName));
        }

        return null;
    }
}
