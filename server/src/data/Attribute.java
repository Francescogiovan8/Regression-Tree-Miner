package data;

import java.io.Serializable;

public abstract class Attribute implements Serializable {
    private String name;
    private int index;

    protected Attribute(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return name;
    }
}
