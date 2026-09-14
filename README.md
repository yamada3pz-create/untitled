# Factorio Top-Down Game

2D Factorio-style game built with pure Java (Swing/Swing2D). Hybrid of Factorio and Minecraft top-down.

## Architecture

- **Data-driven registration** — `DataLoader` reads JSON from `data/vanilla/block`, `data/vanilla/item`, `data/vanilla/biome`, `data/vanilla/model/block`: blocks/items/biomes are not hardcoded
- **Block System** — `Block` (immutable properties), `Blocks` registry (`int[]` + by-name map), `BlockModels`, `BlockState`
- **Block Entities** — `ChestBlockEntity`, `PipeBlockEntity`, `MachineEntity` (data only) + **Systems** (`GameSystems`, `FactorySystem`, `ConveyorSystem`) with scan sync of loaded chunks each tick
- **Inventory (MC-style)** — `Item`, `ItemStack`, `Inventory`/`Slot`, containers (`PlayerContainer`, `ChestContainer`), hotbar
- **World** — `Region` → `Chunk` (16×16, `int[]` layers + `oreAmount`), save/load to `saves/<world>/regions/r.X.Y.dat`, seed in `level.dat`, Perlin-noise generation with biomes
- **UI** — `GuiWindow`, `ContainerWindow`, widgets (`ButtonWidget`, `HotbarWidget`, `IconWidget`, `LabelWidget`, `TextFieldWidget`), menus (`MenuState`, `WorldSelectState`, `CreateWorldState`, `SettingsState`, `GraphicsSettingsState`, `RenameWorldState`), world UI scale (1x/2x)
- **Game loop** — fixed 20 TPS ticks + variable-fps render with interpolation (`tickAlpha`)

## Running

```
gradlew.bat run
```