package com.suspend.core;

import com.suspend.core.exception.SuspendException;
import com.suspend.mapping.EntityMetadata;

import java.util.List;

/**
 * SessionFactory will be created once in application lifecycle, through Configuration object.
 */
public interface SessionFactory {

    Session openSession() throws SuspendException;

    Session getCurrentSession() throws SuspendException;

    void closeSession() throws SuspendException;

    List<EntityMetadata> getAllEntityMetadata();
}
