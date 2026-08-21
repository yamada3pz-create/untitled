# Factorio Top-Down Game

2D Factorio-style game built with Java Swing.

## Architecture

- **Block System** — Block, BlockState, Blocks registry, BlockProperties
- **Block Entities** — ChestBlockEntity, PipeBlockEntity, MachineEntity
- **Inventory System (MC-style)** — Item (singleton), ItemStack, Container (interface), SimpleContainer
- **UI** — GuiWindow, SlotWidget, ButtonWidget, PlayerInventoryWindow, ChestInventoryWindow
- **World** — Chunk (int[] palette + HashMap BlockEntity), WorldGenerator (Perlin noise)