package com.suspend.mapping.fetching;

import com.suspend.connection.ConnectionManager;
import com.suspend.mapping.EntityMapper;
import com.suspend.mapping.FetchType;
import com.suspend.mapping.Relationship;
import com.suspend.mapping.RelationshipType;
import com.suspend.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.*;

public class LazyFetchStrategy implements FetchStrategy {

    private static final Logger logger = LoggerFactory.getLogger(LazyFetchStrategy.class);

    @Override
    public Object fetch(Object entity, Relationship relationship, EntityMapper mapper, Method method) throws SQLException {
        if (method == null) {
            if (relationship.getRelationshipType().equals(RelationshipType.MANY_TO_ONE) || relationship.getRelationshipType().equals(RelationshipType.ONE_TO_ONE)) {
                return ProxyFactory.createProxy(relationship.getRelatedEntity(), relationship, mapper);
            }
            return ProxyFactory.createBagProxy(entity, relationship, mapper);
        }
        return fetchLazily(relationship, entity, mapper);
    }

    @Override
    public boolean supports(FetchType fetchType) {
        return fetchType == FetchType.LAZY;
    }

    public Object fetchLazily(Relationship relationship, Object entity, EntityMapper entityMapper) throws SQLException {
        Connection connection = ConnectionManager.getInstance().getConnection();

        PreparedStatement statement = connection.prepareStatement(createSelectQuery(relationship));
        setQueryParameters(statement, entity);
        ResultSet resultSet = statement.executeQuery();

        return entityMapper.mapResultSet(resultSet, relationship.getRelatedEntity());
    }

    private String createSelectQuery(Relationship relationship) {
        String relatedEntityName = ReflectionUtil.getTableName(relationship.getRelatedEntity());
        String foreignKeyField;
        if (relationship.getRelationshipType().equals(RelationshipType.MANY_TO_ONE) || relationship.getRelationshipType().equals(RelationshipType.ONE_TO_ONE)) {
            foreignKeyField = relationship.getPrimaryKeyField();
        } else {
            foreignKeyField = relationship.getForeignKeyField();
        }
        return "SELECT * FROM " + relatedEntityName + " WHERE " + foreignKeyField + " = ?";
    }

    private void setQueryParameters(PreparedStatement preparedStatement, Object entity) throws SQLException {
        String primaryKeyValue = ReflectionUtil.getValueForIdField(entity).toString();
        preparedStatement.setString(1, primaryKeyValue);
    }
}
