package org.example.block;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class BlockEntity {
    private int x; // Мировые координаты
    private int y;

    public BlockEntity(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){ return x; }
    public int getY(){ return y; }

    // Тип сущностей - строка "chest", "pipe" и т.д.
    public abstract String getType();

    // Сохранить данные из файла
    public abstract void save(DataOutputStream out) throws Exception;

    // Загружать данные из файла
    public abstract  void load(DataInputStream in) throws Exception;

    // Вызыватся каждый кадр (по умолчанию ничего не делает)
    public void tick(){}
}
