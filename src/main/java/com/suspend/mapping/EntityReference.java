package com.suspend.mapping;

import java.util.List;
import java.util.Map;

public class EntityReference {
    private Class<?> clazz;
    private Object entity;
    private Object entityId;
    private List<ForeignKey> foreignKeys;
    private boolean fullyProcessed;
    private boolean mapped;

    public EntityReference(Class<?> clazz, Object entity, Object entityId, boolean fullyProcessed, boolean mapped, List<ForeignKey> foreignKeys) {
        this.clazz = clazz;
        this.entity = entity;
        this.entityId = entityId;
        this.fullyProcessed = fullyProcessed;
        this.mapped = mapped;
        this.foreignKeys = foreignKeys;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public Object getEntityId() {
        return entityId;
    }

    public void setEntityId(Object entityId) {
        this.entityId = entityId;
    }

    public boolean isFullyProcessed() {
        return fullyProcessed;
    }

    public void setFullyProcessed(boolean fullyProcessed) {
        this.fullyProcessed = fullyProcessed;
    }

    public boolean isMapped() {
        return mapped;
    }

    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public ForeignKey getForeignKeyByFieldName(String fieldName) {
        return foreignKeys.stream()
                .filter(fk -> fk.getName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

    public void addForeignKey(ForeignKey foreignKey) {
        this.foreignKeys.add(foreignKey);
    }
}
