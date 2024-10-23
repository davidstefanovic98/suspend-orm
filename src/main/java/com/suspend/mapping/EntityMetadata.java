package com.suspend.mapping;

import java.util.List;

public class EntityMetadata {
    private Class<?> entityClass;
    private Object entityId;
    private List<Relationship> relationships;

    public EntityMetadata(Class<?> entityClass, Object entityId, List<Relationship> relationships) {
        this.entityClass = entityClass;
        this.entityId = entityId;
        this.relationships = relationships;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Object getEntityId() {
        return entityId;
    }

    public void setEntityClass(Class<?> entity) {
        this.entityClass = entity;
    }

    public void setEntityId(Object entityId) {
        this.entityId = entityId;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }
}
