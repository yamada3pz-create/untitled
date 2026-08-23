package org.example.item;

public class Items {

    public static Item GRASS;
    public static Item STONE;
    public static Item SAND;
    public static Item IRON_PLATE;
    public static Item COPPER_PLATE;
    public static Item GEAR;
    public static Item CIRCUIT;
    public static Item PIPE;
    public static Item CHEST;
    public static Item MACHINE;
    public static Item IRON_ORE;
    public static Item COPPER_ORE;
    public static Item WALL;

    private static int nextId = 0;

    public static void register() {
        GRASS = register("grass", "Трава");
        STONE        = register("stone",        "Камень");
        SAND         = register("sand",         "Песок");
        IRON_PLATE   = register("iron_plate",   "Железная пластина");
        COPPER_PLATE = register("copper_plate", "Медная пластина");
        GEAR         = register("gear",         "Шестерёнка");
        CIRCUIT      = register("circuit",      "Схема");
        PIPE    = register("pipe",    "Труба");
        CHEST   = register("chest",   "Сундук");
        MACHINE = register("machine", "Машина");
        IRON_ORE   = register("iron_ore",   "Железная руда");
        COPPER_ORE = register("copper_ore", "Медная руда");
        WALL       = register("wall",       "Стена");

    }

    private static Item register(String id, String name){
        return new Item(id, name);
    }

    public static Item get(String id){
        return switch (id){
            case "grass"        -> GRASS;
            case "stone"        -> STONE;
            case "sand"         -> SAND;
            case "iron_plate"   -> IRON_PLATE;
            case "copper_plate" -> COPPER_PLATE;
            case "gear"         -> GEAR;
            case "circuit"      -> CIRCUIT;
            case "pipe"         -> PIPE;
            case "chest"        -> CHEST;
            case "machine"      -> MACHINE;
            case "iron_ore"   -> IRON_ORE;
            case "copper_ore" -> COPPER_ORE;
            case "wall"       -> WALL;
            default             -> null;
        };
    }
}
