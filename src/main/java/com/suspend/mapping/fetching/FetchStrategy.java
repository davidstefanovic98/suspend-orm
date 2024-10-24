package com.suspend.mapping.fetching;

import com.suspend.mapping.EntityMapper;
import com.suspend.mapping.EntityReference;
import com.suspend.mapping.FetchType;
import com.suspend.mapping.Relationship;

import java.lang.reflect.Method;
import java.sql.SQLException;

public interface FetchStrategy {

    Object fetch(EntityReference entity, Relationship relationship, EntityMapper mapper, Method method) throws SQLException;

    boolean supports(FetchType fetchType);
}
