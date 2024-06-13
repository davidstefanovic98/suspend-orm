package com.suspend.core;

import java.sql.Connection;

public interface Session {

    void persist(Object object);

    <T> T merge(T object);

    <T> Query<T> createQuery(String sql, Class<T> entityClass);

    void clear();

    Connection close();
}

