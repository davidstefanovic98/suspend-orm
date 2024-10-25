package com.suspend.core.internal;

import com.suspend.core.Session;
import com.suspend.core.SessionFactory;
import com.suspend.core.exception.SuspendException;
import com.suspend.mapping.EntityMetadata;

import java.util.ArrayList;
import java.util.List;

public class SessionFactoryImpl implements SessionFactory {

    private final ThreadLocal<Session> sessions = new ThreadLocal<>();
    private static SessionFactory instance = null;
    private final List<EntityMetadata> entities = new ArrayList<>();

    private SessionFactoryImpl() {}

    public static synchronized SessionFactory getInstance() {
        if (instance == null) {
            instance = new SessionFactoryImpl();
        }
        return instance;
    }

    @Override
    public Session openSession() throws SuspendException {
        Session session = new SessionImpl();
        sessions.set(session);
        return session;
    }

    @Override
    public Session getCurrentSession() throws SuspendException {
        Session session = sessions.get();
        if (session == null) {
            throw new SuspendException("No current session found. Please ensure that the session is opened.");
        }
        return session;
    }

    @Override
    public void closeSession() throws SuspendException {
        Session session = sessions.get();
        if (session != null) {
            session.close();
        }
        sessions.remove();
    }

    @Override
    public List<EntityMetadata> getAllEntityMetadata() {
        return entities;
    }

    public void addEntityMetadata(EntityMetadata entityMetadata) {
        entities.add(entityMetadata);
    }

    public EntityMetadata getEntityMetadata(Class<?> entityClass) {
        for (EntityMetadata metadata : entities) {
            if (metadata.getEntityClass().equals(entityClass)) {
                return metadata;
            }
        }
        return null;
    }
}
