package org.example.core;

import java.util.*;

public class Registry<T> {

    private final HashMap<String, T> byId = new HashMap<>();
    private final ArrayList<T> byIndex = new ArrayList<>();

    public void register(String id, T entry){
        byId.put(id, entry);
        byIndex.add(entry);
    }

    public T get(String id){
        return byId.get(id);
    }

    public T getByIndex(int index){
        if(index < 0 || index >= byIndex.size()) return null;
        return byIndex.get(index);
    }

    public int indexOf(T entry){
        return byIndex.indexOf(entry);
    }

    public boolean exists(String id){
        return byId.containsKey(id);
    }

    public Collection<T> all(){
        return byId.values();
    }

    public int size(){
        return byIndex.size();
    }

    public void clear(){
        byId.clear();
        byIndex.clear();
    }
}