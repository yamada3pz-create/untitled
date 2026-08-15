package org.example.block;

import com.google.gson.Gson;
import org.example.core.ResourceManager;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class Blocks {

    private static final Gson gson = new Gson();

    // Главный реестр всех блоков игры
    private static final Map<String, Block> REGISTRY = new HashMap<>();

    // Статические ссылки на базовые блоки для быстрого доступа в коде
    public static Block AIR;
    public static Block GRASS;
    public static Block STONE;
    /**
     * Этот метод запускается один раз при старте игры
     */
    public static void registerAll(){
        AIR = register(new Block("vanilla:air"));
        GRASS = register(new Block("vanilla:grass"));
        STONE = register(new Block("vanilla:stone"));

        loadBlockProperties();
    }

    private static Block register(Block block){
        REGISTRY.put(block.getId(), block);
        return block;
    }

    private static void loadBlockProperties() {
        for(Block block : REGISTRY.values()){
            // Убирает префикс "vanilla:" для имени файла (получается "grass")
            String pureName = block.getId().replace("vanilla:","");
            String jsonPath = ResourceManager.getBlockJsonPath(pureName);
            File jsonFile = new File(jsonPath);

            if(jsonFile.exists()){
                try{
                    FileReader reader = new FileReader(jsonFile);
                        // Магия Gson: обновляем свойства созданного Java-объекта данными из JSON!
                        Block loaderDate = gson.fromJson(reader,Block.class);

                        // Переносим настройки из файла в наш зарегистрированный блок
                        // (Или можно переписать класс так, чтобы Gson сразу создавал объект)
                        // Для простоты примера:
                        // block.setProperties(loadedData);
                         System.out.println("[Registry] Блок " + block.getId() + " успешно настроен из JSON.");

                    }catch(Exception e){
                        System.out.println("[Registry] Ошибка чтения JSON для " + block.getId() + ": " + e.getMessage());
                    }
                }else {
                System.out.println("[Registry] Предупреждение: JSON файл не найден для " + block.getId() + ". Используются дефолтные настройки.");
            }
        }
    }

    // Получить блок по его id
    public static Block get(String id){
        return REGISTRY.getOrDefault(id, AIR);
    }
}
