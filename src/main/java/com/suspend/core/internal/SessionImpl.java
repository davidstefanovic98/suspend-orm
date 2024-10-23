package com.suspend.core.internal;

import com.suspend.connection.ConnectionManager;
import com.suspend.core.Query;
import com.suspend.core.Session;

import java.sql.Connection;

public class SessionImpl implements Session {

    private final Connection connection;

    public SessionImpl() {
        this.connection = ConnectionManager.getInstance().getConnection();
    }

    @Override
    public void persist(Object object) {

    }

    @Override
    public <T> T merge(T object) {
        return null;
    }


    @Override
    public <T> Query<T> createQuery(String sql, Class<T> entityClass) {
        QueryGenerator queryGenerator = new QueryGenerator(sql, entityClass);
        queryGenerator.appendEagerJoins();
        String finalQuery = queryGenerator.getFinalQuery();
        return new QueryImpl<>(new QueryWrapper(finalQuery), connection, entityClass);
    }

    @Override
    public void clear() {

    }

    @Override
    public Connection close() {
        return null;
    }
}
