package com.suspend.mapping;

import java.util.Objects;

public record EntityKey(Object entityId, Class<?> entityClass) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityKey entityKey = (EntityKey) o;
        return Objects.equals(entityId, entityKey.entityId) &&
                Objects.equals(entityClass, entityKey.entityClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, entityClass);
    }
}
