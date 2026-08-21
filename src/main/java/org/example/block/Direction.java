package org.example.block;

public enum Direction {
    NORTH, // вверх (-Y)
    EAST,  // вправо (+X)
    SOUTH, // вниз (-Y)
    WEST;  // влево (-X)

    public int getOffsetX(){
        return switch (this){
            case EAST -> 1;
            case WEST -> -1;
            default -> 0;
        };
    }

    public int getOffsetY(){
        return switch (this){
            case SOUTH -> 1;
            case NORTH -> -1;
            default -> 0;
        };
    }

    public Direction getOpposite(){
        return switch (this){
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    public static Direction fromString(String s){
        return switch (s.toLowerCase()){
            case "north" -> NORTH;
            case "east" -> EAST;
            case "south" -> SOUTH;
            case "west" -> WEST;
            default -> throw new IllegalArgumentException("Unknown direction: " + s);
        };
    }
}
