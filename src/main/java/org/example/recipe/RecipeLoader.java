package org.example.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.item.Item;
import org.example.item.ItemStack;
import org.example.item.Items;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Загрузчик рецептов из data/vanilla/recipes/*.json.
 *
 * Формат файла:
 * {
 *   "input":  [ {"id":"iron_ore","amount":2} ],
 *   "output": { "id":"iron_plate","amount":1 },
 *   "craftTimeTicks": 40
 * }
 * id рецепта берётся из имени файла (без .json).
 */
public class RecipeLoader {

    public static void load(String folderPath){
        File dir = new File(folderPath);
        if(!dir.exists()){
            System.out.println("[Recipes] Папка не найдена: " + folderPath);
            return;
        }
        int count = 0;
        try(Stream<Path> paths = Files.list(dir.toPath())){
            List<Path> jsons = new ArrayList<>();
            paths.filter(p -> p.toString().endsWith(".json") && !p.getFileName().toString().startsWith("parent."))
                 .forEach(jsons::add);
            jsons.sort(Path::compareTo);

            for(Path p : jsons){
                try{
                    JsonObject obj = JsonParser.parseReader(new FileReader(p.toFile())).getAsJsonObject();
                    String id = p.getFileName().toString().replace(".json", "");

                    List<ItemStack> inputs = parseItems(obj, "input");
                    ItemStack output = parseSingle(obj, "output");
                    int craftTime = obj.has("craftTimeTicks") ? obj.get("craftTimeTicks").getAsInt() : 40;

                    if(inputs.isEmpty() || output == null || output.isEmpty()){
                        System.out.println("[Recipes] Пропущен " + id + ": нет input/output");
                        continue;
                    }

                    Recipes.register(new Recipe(id, inputs, output, craftTime));
                    count++;
                }catch(Exception e){
                    System.out.println("[Recipes] Ошибка чтения " + p.getFileName() + ": " + e.getMessage());
                }
            }
        }catch(Exception e){
            System.out.println("[Recipes] Ошибка чтения папки: " + e.getMessage());
        }
        System.out.println("[Recipes] Загружено: " + count);
    }

    private static List<ItemStack> parseItems(JsonObject obj, String key){
        List<ItemStack> result = new ArrayList<>();
        if(!obj.has(key)) return result;
        JsonElement el = obj.get(key);
        if(el.isJsonArray()){
            JsonArray arr = el.getAsJsonArray();
            for(JsonElement e : arr){
                ItemStack s = stackFrom(e.getAsJsonObject());
                if(s != null) result.add(s);
            }
        }else{
            ItemStack s = stackFrom(el.getAsJsonObject());
            if(s != null) result.add(s);
        }
        return result;
    }

    private static ItemStack parseSingle(JsonObject obj, String key){
        if(!obj.has(key)) return null;
        return stackFrom(obj.get(key).getAsJsonObject());
    }

    private static ItemStack stackFrom(JsonObject o){
        String itemId = o.has("id") ? o.get("id").getAsString() : "";
        int amount = o.has("amount") ? o.get("amount").getAsInt() : 1;
        Item item = Items.get(itemId);
        if(item == null){
            System.out.println("[Recipes] Неизвестный предмет: " + itemId);
            return null;
        }
        return new ItemStack(item, amount);
    }
}