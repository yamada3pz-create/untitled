package org.example.block;

import org.example.core.ResourceManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Block {

    private final int globalId;                          // Глобальное id блока
    private final String id;                             // "vanilla:pipe"
    private final String name;                           // "Pipe"
    private final boolean solid;                         // Можно ли пройти сквозь него
    private final float destroyTime;                     // Время добычи
    private final Color color;                           // Цвет для карт (мб)
    private final boolean hasRotation;                   // Поддерживание поворотов
    private final Set<String> connectToTags;             // Тэги для автосоединения ["pipe", "pump"]
    private final boolean hasBlockEntity;


    public Block(int globalId, String name, BlockProperties properties){
        this.globalId = globalId;
        this.id = "vanilla:" + name;
        this.name = name;
        this.solid = properties.solid;
        this.destroyTime = properties.destroyTime;
        this.color = properties.color;
        this.hasRotation = properties.hasRotation;
        this.connectToTags = new HashSet<String>(Arrays.asList(properties.connectsToTags));
        this.hasBlockEntity = properties.hasBlockEntity;
    }
    // Геттеры
    public int getGlobalId()                            { return globalId; }
    public String getId()                               { return id;}
    public String getName()                             { return name; }
    public boolean isSolid()                            { return  solid; }
    public float getDestroyTime()                       { return destroyTime; }
    public Color getColor()                             { return color; }
    public boolean hasRotation()                        { return hasRotation; }
    public Set<String> getConnectsToTags()              { return connectToTags; }
    public boolean hasBlockEntity()                     { return hasBlockEntity; }

    /** Соединяется ли этот блок с другими (пересечение тэгов) */
    public boolean connectsTo(Block other){
        for(String tag : connectToTags){
            if(other.connectToTags.contains(tag)){
                return true;
            }
        }
        return false;
    }
}
