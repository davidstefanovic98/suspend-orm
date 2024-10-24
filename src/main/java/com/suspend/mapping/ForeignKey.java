package com.suspend.mapping;

public class ForeignKey {
    private Object value;
    private String name;
    private Class<?> sourceClass;
    private Class<?> targetClass;

    public ForeignKey(Object value, String name, Class<?> sourceClass, Class<?> targetClass) {
        this.value = value;
        this.name = name;
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getSourceClass() {
        return sourceClass;
    }

    public void setSourceClass(Class<?> sourceClass) {
        this.sourceClass = sourceClass;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }
}
