package org.example.block;

import org.example.block.Machine.MachineBlockEntity;

import java.util.HashMap;

public class BlockEntityType {

    // Реестр: имя типа -> способ создать BlockEntity
    private static final HashMap<String, BlockEntityType> REGISTRY = new HashMap<>();

    private final String name;

    private BlockEntityType(String name){
        this.name = name;
    }

    public String getName(){ return name;}

    // ---Регистрация ---

    public static void register(String name){
        REGISTRY.put(name, new BlockEntityType(name));
    }

    // --- Создание ---

    public static BlockEntity create(String type, int x, int y){
        if(type.equals("chest")){
            return new ChestBlockEntity(x, y);
        }
        if(type.equals("pipe")){
            return new PipeBlockEntity(x, y);
        }
        if(type.equals("machine")){
            return new MachineBlockEntity(x, y);
        }
        return null;
    }

    // Проверка: такой тип существует?
    public static boolean exists(String type){
        return REGISTRY.containsKey(type);
    }

    // Запускает один раз при старте
    public static void init(){
        register("chest");
        register("pipe");
        System.out.println("[BlockEntityType] Типы зарегистрированы.");
    }
}
