package org.example.world.systems;

import org.example.block.Direction;
import org.example.block.PipeBlockEntity;
import org.example.world.World;

import java.util.ArrayList;
import java.util.List;

// Движение жидкости по трубам. Двухфазный перенос:
// фаза 1 — посчитать, куда и сколько течёт каждый участок (по состоянию ДО изменения);
// фаза 2 — применить все переносы. Это исключает "двойной улёт" за один тик,
// когда жидкость успевает перепрыгнуть по нескольким трубам и обогнать реальный поток.
public class ConveyorSystem {

    private static final float FLOW_RATE = 0.25f; // сколько труба передаёт за тик

    private final List<PipeBlockEntity> pipes = new ArrayList<>();

    // Перенос: из src в dst объём amount
    private static class Transfer {
        PipeBlockEntity src;
        PipeBlockEntity dst;
        float amount;
        Transfer(PipeBlockEntity src, PipeBlockEntity dst, float amount){
            this.src = src;
            this.dst = dst;
            this.amount = amount;
        }
    }

    public void clear(){
        pipes.clear();
    }

    public void add(PipeBlockEntity e){
        pipes.add(e);
    }

    public void tick(World world){
        List<Transfer> transfers = computeTransfers(world);
        applyTransfers(transfers);
    }

    // Фаза 1
    private List<Transfer> computeTransfers(World world){
        List<Transfer> transfers = new ArrayList<>();
        for(PipeBlockEntity pipe : pipes){
            if(pipe.isEmpty()) continue;

            int tx = pipe.getX() + pipe.getDirection().getOffsetX();
            int ty = pipe.getY() + pipe.getDirection().getOffsetY();

            if(!(world.getBlockEntityAt(tx, ty) instanceof PipeBlockEntity dst)) continue;
            if(!canAccept(dst, pipe.getLiquidType())) continue;

            float space = dst.getMaxAmount() - dst.getAmount();
            float amount = Math.min(FLOW_RATE, Math.min(pipe.getAmount(), space));
            if(amount <= 0) continue;

            transfers.add(new Transfer(pipe, dst, amount));
        }
        return transfers;
    }

    // Фаза 2
    private void applyTransfers(List<Transfer> transfers){
        for(Transfer t : transfers){
            t.dst.setAmount(t.dst.getAmount() + t.amount);
            if(t.dst.getAmount() > 0 && t.dst.getLiquidType().isEmpty()){
                t.dst.setLiquidType(t.src.getLiquidType());
            }
            t.src.setAmount(t.src.getAmount() - t.amount);
        }
    }

    // В пустую трубу можно лить любую жидкость, в непустую — только ту же
    private boolean canAccept(PipeBlockEntity dst, String liquidType){
        if(dst.isEmpty()) return true;
        return dst.getLiquidType().equals(liquidType);
    }
}