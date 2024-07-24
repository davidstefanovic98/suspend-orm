package com.suspend.mapping;

import java.util.Objects;

public class EntityReference {
    private Object entity;
    private Object entityId;
    private boolean processing;

    public EntityReference(Object entity, Object entityId) {
        this.entity = entity;
        this.entityId = entityId;
        this.processing = false;
    }

    public Object getEntity() {
        return entity;
    }

    public Object getEntityId() {
        return entityId;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public void setEntityId(Object entityId) {
        this.entityId = entityId;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EntityReference) obj;
        return Objects.equals(this.entity, that.entity) &&
                Objects.equals(this.entityId, that.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, entityId);
    }
}
