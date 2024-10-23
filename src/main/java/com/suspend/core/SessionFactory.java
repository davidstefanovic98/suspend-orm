package com.suspend.core;

import com.suspend.core.exception.SuspendException;
import com.suspend.mapping.EntityReferenceContainer;

/**
 * SessionFactory will be created once in application lifecycle, through Configuration object.
 */
public interface SessionFactory {

    Session openSession() throws SuspendException;

    Session getCurrentSession() throws SuspendException;

    EntityReferenceContainer getEntityReferenceContainer();
}
