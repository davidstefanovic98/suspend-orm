package com.suspend.core;

public interface Session {

    void persist(Object object);

    <T> T merge(T object);

    <T> Query<T> createQuery(String sql, Class<T> entityClass);

    <T> T get(Class<T> clazz, Object entityId);

    void clear();

    void close();
}

