package com.suspend.core.internal;

import com.suspend.core.Query;
import com.suspend.mapping.EntityMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueryImpl<T> implements Query<T> {

    private final QueryWrapper queryWrapper;
    private final List<Parameter> parameters;
    private final EntityMapper mapper;
    private final Connection connection;
    private final Class<T> entityClass;

    public QueryImpl(QueryWrapper queryWrapper, Connection connection, Class<T> entityClass) {
        this.queryWrapper = queryWrapper;
        parameters = new ArrayList<>();
        mapper = new EntityMapper();
        this.connection = connection;
        this.entityClass = entityClass;
    }

    @Override
    public List<T> getResultList() throws SQLException {
        PreparedStatement statement = connection.prepareStatement(queryWrapper.sql());
        for (Parameter parameter : parameters) {
            statement.setObject(parameter.getIndex(), parameter.getValue());
        }
        ResultSet resultSet = statement.executeQuery();

        return mapper.mapResultSet(resultSet, entityClass);
    }

    @Override
    public T uniqueResult() throws SQLException {
        return null;
    }

    @Override
    public void setParameter(String name, Object value) throws SQLException {
        parameters.add(new Parameter(name, value));
    }
}
