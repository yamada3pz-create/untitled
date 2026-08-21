package org.example.block;

import java.util.*;

public class BlockState {
    // Ссылка на базовый тип блока (паттерн Легковес / Flyweight)
    private final Block block;

    //карта для хранения динамических свойств (например, "direction" -> "north".
    private final Map<String, String> properties = new HashMap<>();

    // Состояние с дефолтными свойствами блока
    public BlockState(Block block){
        this. block = block;
    }

    // Состояние с явными свойствами
    public BlockState(Block block, Map<String, String> properties){
        this.block = block;
        if(properties != null){
            this. properties.putAll(properties);
        }
    }

    // Проси методы для быстрого доступа к базовым свойствам блока
    public Block getBlock()   { return block;}
    public String getBlockId(){ return block.getId(); }
    public Boolean isSolid()  { return block.isSolid();}


    // Метод для работы с динамическими свойствами конкретного блока на карте
    public void setProperty(String key,String value){
        properties.put(key,value);
    }

    public String getProperty(String key, String defaultValue){
        return properties.getOrDefault(key, defaultValue);
    }
    public String getProperty(String key){
        return properties.get(key);
    }
    public Map<String, String> getProperties(){
        return Collections.unmodifiableMap(properties);
    }
    // --- Удобные методы ---

    /** Поворот: 0, 90, 180, 270 */
    public int getRotation(){
        String rot = properties.get("rotation");
        if(rot == null) return 0;
        try{
            return Integer.parseInt(rot);
        }catch (NumberFormatException e){
            return 0;
        }
    }

    public void setRotation (int rotation){
        properties.put("rotation", String.valueOf(rotation));
    }

    /** Парсит "north,south,west" → Set<Direction> */

    public Set<Direction> getConnections(){
        String conn = properties.get("connections");
        if(conn == null || conn.isEmpty()){
            return Collections.emptySet();
        }
        Set<Direction> dirs = new HashSet<>();
        for(String part : conn.split(",")){
            dirs.add(Direction.fromString(part.trim()));
        }
        return dirs;
    }

    /** Set<Direction> → строку в properties */
    public void setConnections(Set<Direction> connections){
        if(connections == null || connections.isEmpty()){
            properties.remove("connections");
        }else{
            List<String> names = new ArrayList<>();
            for(Direction d : connections){
                names.add(d.name().toLowerCase());
            }
            Collections.sort(names);
            properties.put("connections",String.join(",", names));
        }
    }
    /** Ключ для blockstate JSON: "rotation=90,connections=east,west" */

    public String getTextureKey(){
        StringBuilder sb = new StringBuilder();
        sb.append("rotation=").append(getRotation());
        Set<Direction> conns = getConnections();
        if(!conns.isEmpty()){
            List<String> connName = new ArrayList<>();
            for (Direction d : conns){
                connName.add(d.name().toLowerCase());
            }
            Collections.sort(connName);
            sb.append(",connections=").append(String.join(",", connName));
        }
        return sb.toString();
    }

    @Override
    public boolean equals (Object o){
        if(this == o) return true;
        if(!(o instanceof BlockState other)) return false;
        return block.equals(other.block) && properties.equals(other.properties);
    }

    @Override
    public int hashCode(){
        return 31 * block.hashCode() + properties.hashCode();
    }
    @Override
    public String toString(){
        return "BlockState{" + block.getId() + ", " + properties + "}";
    }
}
