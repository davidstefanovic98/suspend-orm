package com.suspend.mapping.fetching;

import com.suspend.mapping.EntityMapper;
import com.suspend.mapping.EntityReference;
import com.suspend.mapping.FetchType;
import com.suspend.mapping.Relationship;

import java.lang.reflect.Method;

public class EagerFetchStrategy implements FetchStrategy {

    @Override
    public Object fetch(EntityReference entity, Relationship relationship, EntityMapper mapper, Method method) {
        return fetchEagerly(entity, relationship);
    }

    @Override
    public boolean supports(FetchType fetchType) {
        return fetchType.name().equals("EAGER");
    }

    private Object fetchEagerly(Object entity, Relationship relationship) {
        return null;
//        Class<?> relatedEntityClass = relationship.getRelatedEntity();
//
//        Field field = relationship.getField();
//        String relatedTableName = ReflectionUtil.getTableName(relatedEntityClass);
//        String tableName = ReflectionUtil.getTableName(entity.getClass());
//
//        String joinClause = null;
//
//        if (field.isAnnotationPresent(JoinColumn.class)) {
//            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
//
//            joinClause = String.format("JOIN %s ON %s.%s = %s.%s",
//                    relatedTableName,
//                    tableName,
//                    joinColumn.referencedColumnName(),
//                    relatedTableName,
//                    joinColumn.name()
//            );
//        } else if (field.isAnnotationPresent(JoinTable.class)) {
//            JoinTable joinTable = field.getAnnotation(JoinTable.class);
//            String joinTableName = joinTable.name();
//
//            String joinColumnName = joinTable.joinColumns()[0].name();
//            String inverseJoinColumnName = joinTable.inverseJoinColumns()[0].name();
//
//            joinClause = String.format("JOIN %s ON %s.%s = %s.%s ",
//                    joinTableName,
//                    tableName,
//                    joinColumnName,
//                    joinTableName,
//                    joinColumnName
//            );
//
//            joinClause += String.format("JOIN %s ON %s.%s = %s.id",
//                    relatedTableName,
//                    joinTableName,
//                    inverseJoinColumnName,
//                    relatedTableName
//            );
//        }
//        return joinClause;
    }
}
