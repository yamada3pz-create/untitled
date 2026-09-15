package org.example.world.systems;

import org.example.block.BeltBlockEntity;
import org.example.block.Direction;
import org.example.block.InventoryBlockEntity;
import org.example.block.Machine.MachineEntity;
import org.example.item.ItemStack;
import org.example.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Пакетная логистика предметных конвейеров (BeltBlockEntity).
 * Работает двухфазно как ConveyorSystem (жидкости):
 *   фаза 1 — по состоянию ДО тика посчитать, какие предметы "уходят" с последней
 *            позиции каждой ленты (следующая лента / машина / инвентарь);
 *   фаза 2 — одновременный сдвиг всех лент и применение передач.
 *
 * За один тик предмет продвигается ровно на одну позицию (из слота i+1 в i),
 * а с последней позиции ленты переходит на следующую цель. Циклических/
 * редундантных перепрыгиваний за один тик не происходит.
 */
public class BeltSystem {

    private final List<BeltBlockEntity> belts = new ArrayList<>();

    // Внутреннее представление передачи "последний предмет ленты -> цель"
    private static class Transfer {
        final BeltBlockEntity src;
        final Object dst;       // BeltBlockEntity | MachineEntity | InventoryEntity
        final ItemStack stack;  // копия предмета
        Transfer(BeltBlockEntity src, Object dst, ItemStack stack){
            this.src = src;
            this.dst = dst;
            this.stack = stack;
        }
    }

    public void clear(){ belts.clear(); }

    public void add(BeltBlockEntity belt){ belts.add(belt); }

    // Автостыковка углов: перпендикулярный сосед, направленный в ленту,
    // делает её угловой (спрайт угла, предметы доворачивают на 90°).
    // Вызывается из GameSystems.sync после сбора всех лент.
    public void resolveCorners(World world){
        for(BeltBlockEntity belt : belts){
            belt.setCorner(isCorner(world, belt));
        }
    }

    private boolean isCorner(World world, BeltBlockEntity belt){
        for(Direction d : Direction.values()){
            int nx = belt.getX() + d.getOffsetX();
            int ny = belt.getY() + d.getOffsetY();
            if(!(world.getBlockEntityAt(nx, ny) instanceof BeltBlockEntity neighbor)) continue;
            // Сосед направлен в эту ленту и его ось перпендикулярна нашей -> угол
            if(neighbor.getDirection() == d.getOpposite()
                    && neighbor.getDirection() != belt.getDirection()
                    && neighbor.getDirection() != belt.getDirection().getOpposite()){
                return true;
            }
        }
        return false;
    }

    public void tick(World world){
        // Фаза 1: собираем передачи по текущему (старому) состоянию
        List<Transfer> transfers = computeTransfers(world);

        // Фаза 2: применяем
        applyTransfers(transfers);
    }

    // ── Фаза 1 ──────────────────────────────────────────────

    private List<Transfer> computeTransfers(World world){
        List<Transfer> transfers = new ArrayList<>();

        for(BeltBlockEntity src : belts){
            ItemStack last = src.getLastSlot();
            if(last.isEmpty()) continue; // нечего передавать

            int tx = src.getX() + src.getDirection().getOffsetX();
            int ty = src.getY() + src.getDirection().getOffsetY();

            Object target = classifyTarget(world, tx, ty, last);
            if(target == null) continue; // некуда сдавать

            transfers.add(new Transfer(src, target, last));
        }
        return transfers;
    }

    // Какую цель может принять предмет? null — никто.
    private Object classifyTarget(World world, int tx, int ty, ItemStack stack){
        org.example.block.BlockEntity e = world.getBlockEntityAt(tx, ty);

        if(e instanceof BeltBlockEntity dst){
            if(dst.isFull()) return null;
            return dst;
        }
        if(e instanceof MachineEntity machine){
            return machine.canAcceptInput(stack) ? machine : null;
        }
        // Сундук и прочие инвентарные энтити — простой приёмник (всегда место гарантируем
        // косвенно: если слот успел освободиться; принимаем любой предмет).
        if(e instanceof InventoryBlockEntity inv){
            if(inv.getInventory() == null) return null;
            return inv;
        }
        return null;
    }

    // ── Фаза 2 ──────────────────────────────────────────────

    private void applyTransfers(List<Transfer> transfers){
        // 1) карта "входящий предмет" для каждой ленты
        HashMap<BeltBlockEntity, ItemStack> incomingMap = new HashMap<>();
        for(Transfer t : transfers){
            if(t.dst instanceof BeltBlockEntity d){
                incomingMap.putIfAbsent(d, t.stack);
            }
        }

        // 2) применяем передачи в инвентари/машины (вне лент)
        for(Transfer t : transfers){
            if(t.dst instanceof MachineEntity machine){
                machine.receiveInput(t.stack);
            }else if(t.dst instanceof InventoryBlockEntity inv){
                inv.getInventory().addItem(t.stack.getItem(), t.stack.getCount());
            }
        }

        // 3) сдвиг всех лент
        for(BeltBlockEntity belt : belts){
            ItemStack[] s = belt.getSlots();
            boolean canShift = incomingMap.containsKey(belt) || s[s.length - 1].isEmpty();

            if(canShift){
                // Сдвиг вправо (towards exit)
                for(int i = s.length - 1; i > 0; i--){
                    s[i] = s[i-1];
                }
                s[0] = incomingMap.getOrDefault(belt, ItemStack.EMPTY);
            }
            // иначе лента забита — предметы стоят, ничего не меняется
        }
    }
}