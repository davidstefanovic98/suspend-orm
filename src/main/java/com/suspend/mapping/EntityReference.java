package com.suspend.mapping;

import java.util.HashMap;
import java.util.Map;

public class EntityReference {
    private final Map<Class<?>, Map<Object, Object>> entityMap;

    public EntityReference() {
        this.entityMap = new HashMap<>();
    }

    public void addProcessedEntity(Class<?> entityClass, Object entityId, Object entity) {
        entityMap.computeIfAbsent(entityClass, k -> new HashMap<>()).put(entityId, entity);
    }

    public Object getProcessedEntity(Class<?> entityClass, Object entityId) {
        return entityMap.getOrDefault(entityClass, new HashMap<>()).get(entityId);
    }
}
