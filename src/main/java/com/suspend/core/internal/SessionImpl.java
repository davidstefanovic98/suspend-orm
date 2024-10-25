package com.suspend.core.internal;

import com.suspend.connection.ConnectionManager;
import com.suspend.core.Query;
import com.suspend.core.Session;
import com.suspend.core.exception.SuspendException;
import com.suspend.mapping.EntityKey;
import com.suspend.mapping.EntityReference;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SessionImpl implements Session {

    private final Connection connection;
    private final Map<EntityKey, EntityReference> referenceMap = new HashMap<>();

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
    public <T> T get(Class<T> clazz, Object entityId) {
        EntityKey key = new EntityKey(entityId, clazz);
        EntityReference e = referenceMap.get(key);
        if (e == null)
            return null;
        return (T) e.getEntity();
    }

    @Override
    public void clear() {
        referenceMap.clear();
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

    public void addEntityReference(EntityReference reference) {
        EntityKey key = new EntityKey(reference.getEntityId(), reference.getClazz());
        referenceMap.put(key, reference);
    }

    public EntityReference getEntityReference(Class<?> clazz, Object entityId) {
        EntityKey key = new EntityKey(entityId, clazz);
        return referenceMap.get(key);
    }
}
