package com.suspend.core.internal;

import com.suspend.annotation.JoinColumn;
import com.suspend.annotation.JoinTable;
import com.suspend.mapping.EntityMetadata;
import com.suspend.mapping.FetchType;
import com.suspend.mapping.Relationship;
import com.suspend.util.ReflectionUtil;

import java.lang.reflect.Field;

public class QueryGenerator {
    private final StringBuilder queryBuilder;
    private final Class<?> entityClass;

    public QueryGenerator(String baseQuery, Class<?> entityClass) {
        this.queryBuilder = new StringBuilder(baseQuery);
        this.entityClass = entityClass;
    }

    public void appendEagerJoins() {
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) SessionFactoryImpl.getInstance();
        EntityMetadata entityMetadata = sessionFactory.getEntityMetadata(entityClass);

        if (entityMetadata != null) {
            for (Relationship relationship : entityMetadata.getRelationships()) {
                if (FetchType.EAGER.equals(relationship.getFetchingType())) {
                    String joinClause = generateJoinClause(relationship);
                    queryBuilder.append(" ").append(joinClause);
                }
            }
        }
    }

    private String generateJoinClause(Relationship relationship) {
        Class<?> relatedEntityClass = relationship.getRelatedEntity();
        Field field = relationship.getField();
        String relatedTableName = ReflectionUtil.getTableName(relatedEntityClass);
        String tableName = ReflectionUtil.getTableName(entityClass);

        String joinClause = null;

        if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            joinClause = String.format("JOIN %s ON %s.%s = %s.%s",
                    relatedTableName,
                    tableName,
                    joinColumn.referencedColumnName(),
                    relatedTableName,
                    joinColumn.name());
        } else if (field.isAnnotationPresent(JoinTable.class)) {
            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            String joinTableName = joinTable.name();

            String joinColumnName = joinTable.joinColumns()[0].name();
            String inverseJoinColumnName = joinTable.inverseJoinColumns()[0].name();

            joinClause = String.format("JOIN %s ON %s.%s = %s.%s JOIN %s ON %s.%s = %s.id",
                    joinTableName,
                    tableName,
                    joinColumnName,
                    joinTableName,
                    joinColumnName,
                    relatedTableName,
                    joinTableName,
                    inverseJoinColumnName,
                    relatedTableName);
        }
        return joinClause;
    }

    public String getFinalQuery() {
        return queryBuilder.toString();
    }
}

