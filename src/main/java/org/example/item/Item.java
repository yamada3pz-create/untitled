package org.example.item;

public class Item {

    private final String id;
    private final String name;
    private final int maxStackSize;
    private String placeBlock;

    public Item(String id, String name, int maxStackSize){
        this.id = id;
        this.name = name;
        this.maxStackSize = maxStackSize;
    }

    public Item(String id, String name){
        this(id, name, 64);
    }

    public String getId()           { return id; }
    public String getName()         { return name; }
    public int getMaxStackSize()    { return maxStackSize; }
    public String getPlaceBlock()   { return placeBlock; }
    public void setPlaceBlock(String id){ this.placeBlock = id; }
}