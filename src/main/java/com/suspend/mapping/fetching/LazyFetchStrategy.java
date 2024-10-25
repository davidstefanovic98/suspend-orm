package com.suspend.mapping.fetching;

import com.suspend.connection.ConnectionManager;
import com.suspend.core.internal.SessionFactoryImpl;
import com.suspend.core.internal.SessionImpl;
import com.suspend.mapping.*;
import com.suspend.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LazyFetchStrategy implements FetchStrategy {

    @Override
    public Object fetch(EntityReference entity, Relationship relationship, EntityMapper mapper, Method method) throws SQLException {
        if (method == null) {
            if (relationship.isManyToOne() || relationship.isOneToOne()) {
                return ProxyFactory.createProxy(entity, relationship.getRelatedEntity(), relationship, mapper);
            }
            return ProxyFactory.createBagProxy(entity, relationship, mapper);
        }
        return fetchLazily(relationship, entity, mapper);
    }

    @Override
    public boolean supports(FetchType fetchType) {
        return fetchType == FetchType.LAZY;
    }

    public Object fetchLazily(Relationship relationship, EntityReference entity, EntityMapper entityMapper) throws SQLException {
        Connection connection = ConnectionManager.getInstance().getConnection();

        String sql = createSelectQuery(relationship);

        PreparedStatement statement = connection.prepareStatement(sql);
        setQueryParameters(statement, relationship, entity);
        ResultSet resultSet = statement.executeQuery();

        Class<?> relatedEntityClass = relationship.getRelatedEntity();
        SessionImpl session = (SessionImpl) SessionFactoryImpl.getInstance().getCurrentSession();

        if (relationship.isManyToOne() || relationship.isOneToOne()) {
            if (resultSet.next()) {
                Object primaryKeyValue = resultSet.getObject(relationship.getPrimaryKeyField());
                Object existingEntity = session.get(relatedEntityClass, primaryKeyValue);

                if (existingEntity != null) {
                    return existingEntity;
                } else {
                    resultSet.previous();
                    Object mappedEntity = entityMapper.mapResultSet(resultSet, relatedEntityClass);
                    session.addEntityReference(new EntityReference(relatedEntityClass, mappedEntity, primaryKeyValue, false, false, null));
                    return mappedEntity;
                }
            }
        } else {
            List<Object> resultList = new ArrayList<>();

            while (resultSet.next()) {
                Object primaryKeyValue = resultSet.getObject(relationship.getPrimaryKeyField());
                Object existingEntity = session.get(relatedEntityClass, primaryKeyValue);

                if (existingEntity != null) {
                    resultList.add(existingEntity);
                } else {
                    resultSet.previous();
                    Object mappedEntity = entityMapper.mapResultSet(resultSet, relatedEntityClass);
                    session.addEntityReference(new EntityReference(relatedEntityClass, mappedEntity, primaryKeyValue, false, false, null));
                    return mappedEntity;
                }
            }
            return resultList;
        }
        return null;
    }

    private String createSelectQuery(Relationship relationship) {
        String relatedEntityName = ReflectionUtil.getTableName(relationship.getRelatedEntity());
        String foreignKeyField;
        if (relationship.isManyToOne() || relationship.isOneToOne()) {
            foreignKeyField = relationship.getPrimaryKeyField();
        } else {
            foreignKeyField = relationship.getForeignKeyField();
        }
        return "SELECT * FROM " + relatedEntityName + " WHERE " + foreignKeyField + " = ?";
    }

    private void setQueryParameters(PreparedStatement preparedStatement, Relationship relationship, EntityReference entity) throws SQLException {
        Object primaryKeyValue;
        if (relationship.isManyToOne() || relationship.isOneToOne()) {
            ForeignKey foreignKey = entity.getForeignKeyByFieldName(relationship.getForeignKeyField());
            primaryKeyValue = foreignKey.getValue();
        } else {
            primaryKeyValue = ReflectionUtil.getValueForIdField(entity.getEntity());
        }
        preparedStatement.setObject(1, primaryKeyValue);
    }
}
