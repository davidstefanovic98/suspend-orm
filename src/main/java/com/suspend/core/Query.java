package com.suspend.core;

import java.sql.SQLException;
import java.util.List;

public interface Query<T> {

    List<T> getResultList() throws SQLException;

    T uniqueResult() throws SQLException;

    void setParameter(String name, Object value) throws SQLException;
}
