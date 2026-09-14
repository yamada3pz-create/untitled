package org.example.block;

import java.util.HashMap;

public class BlockModel {

    public static class Animation {
        public final int frames;
        public final int speed;
        public final boolean loop;
        public Animation(int frames, int speed, boolean loop){
            this.frames = frames;
            this.speed = speed;
            this.loop = loop;
        }
    }

    private final int globalId;
    private final HashMap<String, String> textures = new HashMap<>();
    private int variantCount = 0;
    private Animation animation;

    public BlockModel(int globalId){
        this.globalId = globalId;
    }

    public int getGlobalId(){ return globalId; }

    public void setTexture(String variant, String textureName){
        textures.put(variant, textureName);
    }

    public String getTexture(String variant){
        String tex = textures.get(variant);
        if(tex != null) return tex;
        return textures.get("base");
    }

    public void setVariantCount(int v){ this.variantCount = v; }
    public int getVariantCount(){ return variantCount; }
    public boolean hasVariants(){ return variantCount > 0; }

    public void setAnimation(Animation anim){ this.animation = anim; }
    public Animation getAnimation(){ return animation; }
    public boolean isAnimated(){ return animation != null; }
}