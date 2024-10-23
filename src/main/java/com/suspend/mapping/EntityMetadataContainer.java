package com.suspend.mapping;

import java.util.ArrayList;
import java.util.List;

public class EntityMetadataContainer {
    private final List<EntityMetadata> entities;

    public EntityMetadataContainer() {
        entities = new ArrayList<>();
    }

    public List<EntityMetadata> getAllEntityMetadata() {
        return entities;
    }

    public void addEntityMetadata(EntityMetadata entityMetadata) {
        entities.add(entityMetadata);
    }

    public EntityMetadata getEntityMetadata(Class<?> entityClass) {
        for (EntityMetadata metadata : entities) {
            if (metadata.getEntityClass().equals(entityClass)) {
                return metadata;
            }
        }
        return null;
    }
}
