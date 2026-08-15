package org.example.block;

public class Block {

    private final String id;
    private String name = "Unnamed block";
    private boolean isSolid = true; // Можно ли пройти сквозь него
    private transient String textureName;

    public Block(String id){
        this.id = id;
    }

    // Геттеры
    public String getId()   { return id;}
    public boolean ifSolid(){ return  isSolid;}
    public String getName() { return name;}
}
