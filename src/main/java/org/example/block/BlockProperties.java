package org.example.block;

import java.awt.*;

public class BlockProperties {

    String name = "";
    boolean solid = true;
    float destroyTime = 1.0f;
    Color color = Color.WHITE;
    boolean hasRotation = false;
    String[] connectsToTags = new String[0];
    boolean hasBlockEntity = false;

    String layer = "object";
    float moveSpeed = 1.0f;
    int drillTier = 0;
    boolean infinite = false;
    int defaultAmount = 0;
    String drops = "";

    private BlockProperties(){}

    public static BlockProperties create(){
        return new BlockProperties();
    }

    public BlockProperties name(String v){ this.name = v; return this; }
    public BlockProperties solid(boolean v){ this.solid = v; return this; }
    public BlockProperties destroyTime(float v){ this.destroyTime = v; return this; }
    public BlockProperties color(Color v){ this.color = v; return this; }
    public BlockProperties hasRotation(boolean v){ this.hasRotation = v; return this; }
    public BlockProperties connectsToTags(String... tags){ this.connectsToTags = tags; return this; }
    public BlockProperties hasBlockEntity(boolean v){ this.hasBlockEntity = v; return this; }
    public BlockProperties layer(String v){ this.layer = v; return this; }
    public BlockProperties moveSpeed(float v){ this.moveSpeed = v; return this; }
    public BlockProperties drillTier(int v){ this.drillTier = v; return this; }
    public BlockProperties infinite(boolean v){ this.infinite = v; return this; }
    public BlockProperties defaultAmount(int v){ this.defaultAmount = v; return this; }
    public BlockProperties drops(String v){ this.drops = v; return this; }
}
