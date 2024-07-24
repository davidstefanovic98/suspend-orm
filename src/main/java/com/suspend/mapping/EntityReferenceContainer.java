package com.suspend.mapping;

import java.util.HashMap;
import java.util.Map;

public class EntityReferenceContainer {
    private final Map<EntityKey, EntityReference> entityMap;

    public EntityReferenceContainer() {
        this.entityMap = new HashMap<>();
    }

    public void addProcessedEntity(Class<?> entityClass, Object entityId, EntityReference entity) {
        EntityKey key = new EntityKey(entityId, entityClass);
        entityMap.put(key, entity);
    }

    public EntityReference getProcessedEntity(Class<?> entityClass, Object entityId) {
        EntityKey key = new EntityKey(entityId, entityClass);
        return entityMap.get(key);
    }
}
