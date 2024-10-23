package com.suspend.mapping;

import java.util.HashMap;
import java.util.Map;

public class EntityReferenceContainer {
    private final Map<EntityKey, EntityReference> referenceMap = new HashMap<>();

    public void addEntityReference(EntityReference reference) {
        EntityKey key = new EntityKey(reference.getEntityId(), reference.getClazz());
        referenceMap.put(key, reference);
    }

    public EntityReference getEntityReference(Class<?> clazz, Object entityId) {
        EntityKey key = new EntityKey(entityId, clazz);
        return referenceMap.get(key);
    }

    public boolean containsEntityReference(Class<?> clazz, Object entityId) {
        EntityKey key = new EntityKey(entityId, clazz);
        return referenceMap.containsKey(key);
    }

    public void removeEntityReference(Class<?> clazz, Object entityId) {
        EntityKey key = new EntityKey(entityId, clazz);
        referenceMap.remove(key);
    }
}
