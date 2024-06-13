package com.suspend.core.internal;

public class Parameter {

    private final String name;
    private final Object value;
    private int index;

    public Parameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
