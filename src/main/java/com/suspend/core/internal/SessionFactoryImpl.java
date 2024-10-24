package com.suspend.core.internal;

import com.suspend.core.Session;
import com.suspend.core.SessionFactory;
import com.suspend.core.exception.SuspendException;
import com.suspend.mapping.EntityReferenceContainer;

public class SessionFactoryImpl implements SessionFactory {

    private final ThreadLocal<Session> sessions = new ThreadLocal<>();
    private static SessionFactory instance = null;
    private final EntityReferenceContainer entityReferenceContainer;

    public SessionFactoryImpl() {
        this.entityReferenceContainer = new EntityReferenceContainer();
    }

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
            throw new SuspendException("No current session found");
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
    public EntityReferenceContainer getEntityReferenceContainer() {
        return entityReferenceContainer;
    }
}
