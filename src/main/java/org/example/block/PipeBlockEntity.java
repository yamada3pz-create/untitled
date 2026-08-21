package org.example.block;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class PipeBlockEntity extends BlockEntity{

    private float amount;       // Сколько жидкости сейчас
    private float maxAmount;    // максимум (1.0)
    private String liquidType;  // тип жидкости ("water","lava","")

    public PipeBlockEntity(int x, int y){
        super(x,y);
        this.amount = 0;
        this.maxAmount = 1.0f;
        this.liquidType = "";
    }



    public float getAmount(){ return amount; }
    public float getMaxAmount(){ return maxAmount; }
    public String getLiquidType(){ return liquidType; }

    public void setAmount(float amount){
        this.amount = amount;
        if(this.amount > maxAmount) this.amount = maxAmount;
        if(this.amount < 0) this.amount = 0;
    }

    public void setLiquidType(String liquidType){
        this.liquidType = liquidType;
    }

    public boolean isFull(){ return amount >= maxAmount; }
    public boolean isEmpty(){ return amount <= 0; }

    @Override
    public String getType() {
        return "pipe";
    }

    @Override
    public void save(DataOutputStream out) throws Exception{
        out.writeFloat(amount);
        out.writeFloat(maxAmount);
        out.writeUTF(liquidType);

    }

    @Override
    public void load(DataInputStream in) throws Exception{
        amount = in.readFloat();
        maxAmount = in.readFloat();
        liquidType = in.readUTF();

    }
}
