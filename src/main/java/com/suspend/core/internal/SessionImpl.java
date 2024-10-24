package com.suspend.core.internal;

import com.suspend.connection.ConnectionManager;
import com.suspend.core.Query;
import com.suspend.core.Session;
import com.suspend.core.exception.SuspendException;

import java.sql.Connection;
import java.sql.SQLException;

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
        SessionFactoryImpl.getInstance().getEntityReferenceContainer().clear();
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new SuspendException("Failed to close connection", e);
        } finally {
            clear();
        }
    }
}
