package com.suspend.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * The EntityReferenceContainer holds references of mapped objects, so entity mapping
 * isn't being invoked every single time we need an object.
 */

//TODO: This logic should be moved to Session, since the Session should be the one holding
// the entity references, and handle the lifecycle of an entity throughout one session.
// For now, this will do.
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

    public void clear() {
        referenceMap.clear();
    }
}
