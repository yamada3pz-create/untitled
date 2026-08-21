package org.example.item;

public class Item {

    private final String id;
    private final String name;
    private final int maxStackSize;

    public Item (String id, String name, int maxStackSize){
        this.id = id;
        this.name = name;
        this.maxStackSize = maxStackSize;
    }

    public Item (String id, String name){
        this(id, name, 64);
    }

    public String getId() { return id; }
    public String fetName() { return name; }
    public int  getMaxStackSize() { return maxStackSize; }
}
