package com.suspend.mapping;

import java.util.ArrayList;
import java.util.List;

/*
   TODO: This logic should be moved to SessionFactory.
 */

/**
 * The EntityMetadataContainer class holds the general information of entities, when packages are scanned.
 */
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
