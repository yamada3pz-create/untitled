package org.example.block;

import java.awt.*;

public class BlockProperties {

    boolean solid = true;
    float destroyTime = 1.0f;
    Color color = Color.WHITE;
    boolean hasRotation = false;
    String[] connectsToTags = new String[0];
    boolean hasBlockEntity = false;

    private BlockProperties(){

    }

    public static BlockProperties create(){
        return new BlockProperties();
    }

    public BlockProperties solid(boolean v){
        this.solid = v;
        return  this;
    }

    public BlockProperties destroyTime(float v){
        this.destroyTime = v;
        return this;
    }

    public BlockProperties color(Color v){
        this.color = v;
        return this;
    }

    public BlockProperties hasRotation(boolean v){
        this.hasRotation = v;
        return this;
    }

    public BlockProperties connectsToTags(String... tags){
        this.connectsToTags = tags;
        return this;
    }

    public BlockProperties hasBlockEntity(boolean v){
        this.hasBlockEntity = v;
        return this;
    }
}
