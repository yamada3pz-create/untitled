package org.example.block;

import java.util.*;

public class Block {

    private final int globalId;
    private final String id;
    private final String name;
    private final boolean solid;
    private final float destroyTime;
    private final boolean hasRotation;
    private final Set<String> connectToTags;
    private final boolean hasBlockEntity;

    private final String layer;
    private final float moveSpeed;
    private final int drillTier;
    private final boolean infinite;
    private final int defaultAmount;
    private final String drops;

    // По ТЗ: физика и дроп
    private final boolean collidable;
    private final int layerCode;
    private final double miningSpeedMultiplier;
    private final String recipe;

    public Block(int globalId, String id, BlockProperties p){
        this.globalId = globalId;
        this.id = id;
        this.name = p.name.isEmpty() ? id : p.name;
        this.solid = p.solid;
        this.destroyTime = p.destroyTime;
        this.hasRotation = p.hasRotation;
        this.connectToTags = new HashSet<String>(Arrays.asList(p.connectsToTags));
        this.hasBlockEntity = p.hasBlockEntity;
        this.layer = p.layer;
        this.moveSpeed = p.moveSpeed;
        this.drillTier = p.drillTier;
        this.infinite = p.infinite;
        this.defaultAmount = p.defaultAmount;
        this.drops = p.drops;
        this.collidable = p.collidable;
        this.layerCode = p.layerCode;
        this.miningSpeedMultiplier = p.miningSpeedMultiplier;
        this.recipe = p.recipe;
    }

    public int getGlobalId()        { return globalId; }
    public String getId()           { return id; }
    public String getName()         { return name; }
    public boolean isSolid()        { return solid; }
    public float getDestroyTime()   { return destroyTime; }
    public boolean hasRotation()    { return hasRotation; }
    public Set<String> getConnectsToTags() { return connectToTags; }
    public boolean hasBlockEntity() { return hasBlockEntity; }

    public String getLayer()        { return layer; }
    public float getMoveSpeed()     { return moveSpeed; }
    public int getDrillTier()       { return drillTier; }
    public boolean isInfinite()     { return infinite; }
    public int getDefaultAmount()   { return defaultAmount; }
    public String getDrops()        { return drops; }

    public boolean isCollidable()        { return collidable; }
    public int getLayerCode()            { return layerCode; }
    public double getMiningSpeedMultiplier() { return miningSpeedMultiplier; }
    public String getRecipe()            { return recipe; }

    public boolean connectsTo(Block other){
        for(String tag : connectToTags){
            if(other.connectToTags.contains(tag)) return true;
        }
        return false;
    }
}
