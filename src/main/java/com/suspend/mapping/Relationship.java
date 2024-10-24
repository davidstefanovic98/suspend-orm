package com.suspend.mapping;

import java.lang.reflect.Field;

public class Relationship {
    private Class<?> relatedEntity;
    private RelationshipType relationshipType;
    private FetchType fetchingType;
    private Field field;
    private String foreignKeyField;
    private String primaryKeyField;

    public Relationship(Class<?> relatedEntity, RelationshipType relationshipType, FetchType fetchingType, Field field, String foreignKeyField, String primaryKeyField) {
        this.relatedEntity = relatedEntity;
        this.relationshipType = relationshipType;
        this.fetchingType = fetchingType;
        this.field = field;
        this.foreignKeyField = foreignKeyField;
        this.primaryKeyField = primaryKeyField;
    }

    public Class<?> getRelatedEntity() {
        return relatedEntity;
    }

    public void setRelatedEntity(Class<?> relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public FetchType getFetchingType() {
        return fetchingType;
    }

    public void setFetchingType(FetchType fetchingType) {
        this.fetchingType = fetchingType;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getForeignKeyField() {
        return foreignKeyField;
    }

    public void setForeignKeyField(String foreignKeyField) {
        this.foreignKeyField = foreignKeyField;
    }

    public String getPrimaryKeyField() {
        return primaryKeyField;
    }

    public void setPrimaryKeyField(String primaryKeyField) {
        this.primaryKeyField = primaryKeyField;
    }

    public boolean isManyToOne() {
        return relationshipType.equals(RelationshipType.MANY_TO_ONE);
    }
}
