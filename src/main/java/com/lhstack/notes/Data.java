package com.lhstack.notes;

import java.util.List;
import java.util.Objects;

public class Data {

    private String name;

    private String text;

    private List<Data> children;

    public String getName() {
        return name;
    }

    public Data setName(String name) {
        this.name = name;
        return this;
    }

    public String getText() {
        return text;
    }

    public Data setText(String text) {
        this.text = text;
        return this;
    }

    public List<Data> getChildren() {
        return children;
    }

    public Data setChildren(List<Data> children) {
        this.children = children;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return Objects.equals(name, data.name) && Objects.equals(text, data.text) && Objects.equals(children, data.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, text, children);
    }

    @Override
    public String toString() {
        return name;
    }
}
