# Blockstate: варианты текстур по состоянию

Blockstate задаёт, какую текстуру показывать в зависимости от *состояния*
блока (поворот, соединения с соседями и т.д.). Хранятся в папке:

```
data/vanilla/blockstate/*.json
```

Имя файла совпадает с id блока (например `pipe.json` — для блока `pipe`).

---

## Формат

```json
{
  "variants": {
    "состояние1": { "texture": "имя_текстуры" },
    "состояние2": { "texture": "имя_текстуры" }
  }
}
```

Каждый ключ — перечень условий через запятую (вида `параметр=значение`),
значение — какая текстура рисуется.

---

## Реальный пример: труба

`data/vanilla/blockstate/pipe.json`:

```json
{
  "variants": {
    "rotation=0,connections=north,south":  { "texture": "pipe_ns" },
    "rotation=90,connections=east,west":    { "texture": "pipe_ew" },
    "rotation=0,connections=north,east,south,west": { "texture": "pipe_cross" }
  }
}
```

- `rotation` — угол поворота (в градусах).
- `connections` — с какими направлениями соединён (north/east/south/west).

Загружается автоматически при запуске. Применяется для блоков с `hasRotation`
(трубы, машины) в рендере.
