package com.suspend.core.internal;

import com.suspend.core.Session;
import com.suspend.core.SessionFactory;
import com.suspend.core.exception.SuspendException;

import java.sql.SQLException;

public class SessionFactoryImpl implements SessionFactory {

    private final ThreadLocal<Session> sessions = new ThreadLocal<>();

    private static SessionFactory instance = null;

    public SessionFactoryImpl() {}

    public static synchronized SessionFactory getInstance() throws SQLException {
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
}
