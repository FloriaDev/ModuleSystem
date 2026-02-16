package ru.feeland.modulesystem.service.impl;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.BaseConfig;
import ru.feeland.modulesystem.service.BaseService;

import java.util.*;

public class RecipeService extends BaseService {
    private final Set<Inventory> processingInventories = new HashSet<>();
    private final Set<NamespacedKey> registeredRecipes = new HashSet<>();
    private Map<Character, Boolean> lastTransferDataMap;
    private BaseConfig recipesConfig;

    public RecipeService(BaseModuleSystem plugin) {
        super(plugin);
    }

    public void loadRecipesFromConfig(BaseConfig recipesConfig) {
        this.recipesConfig = recipesConfig;
        if (recipesConfig.getSectionKeys("recipes").isEmpty()) {
            getPlugin().getLogger().info("Нет рецептов для загрузки.");
            return;
        }

        for (String group : recipesConfig.getSectionKeys("recipes")) {
            for (String recipeKey : recipesConfig.getSectionKeys("recipes." + group)) {
                String fullKey = group + "." + recipeKey;
                boolean useListener = recipesConfig.getBoolean("recipes." + fullKey + ".useListener");

                if (!useListener && isSimpleRecipe(fullKey)) {
                    registerShapedRecipe(fullKey);
                }
            }
        }
    }

    private boolean isSimpleRecipe(String fullKey) {
        String basePath = "recipes." + fullKey + ".ingredients";
        for (String key : recipesConfig.getSectionKeys(basePath)) {
            String type = recipesConfig.getString(basePath + "." + key + ".type");

            if ("enchanted_book".equals(type) || "potion".equals(type) || "splash_potion".equals(type)) {
                return false;
            }
            if (!"none".equals(type) && !type.isEmpty()) {
                String id = recipesConfig.getString(basePath + "." + key + ".id");
                if (id != null && !id.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void registerShapedRecipe(String fullKey) {
        ItemStack result = parseResultItem(fullKey);
        List<String> shape = validateShape(recipesConfig.getStringList("recipes." + fullKey + ".shape"));
        Map<Character, List<ItemStack>> ingredients = parseIngredients(fullKey);

        Map<Character, List<Material>> materialsBySymbol = new HashMap<>();
        for (Map.Entry<Character, List<ItemStack>> entry : ingredients.entrySet()) {
            List<Material> materials = entry.getValue().stream()
                    .map(ItemStack::getType)
                    .toList();
            materialsBySymbol.put(entry.getKey(), materials);
        }

        List<Map<Character, Material>> combinations = generateMaterialCombinations(materialsBySymbol);

        for (Map<Character, Material> combination : combinations) {
            NamespacedKey namespacedKey = createUniqueNamespacedKey(fullKey, combination);
            if (Bukkit.getRecipe(namespacedKey) != null) {
                Bukkit.removeRecipe(namespacedKey);
            }

            ShapedRecipe recipe = new ShapedRecipe(namespacedKey, result);
            recipe.shape(shape.toArray(new String[0]));

            for (Map.Entry<Character, Material> entry : combination.entrySet()) {
                recipe.setIngredient(entry.getKey(), entry.getValue());
            }

            Bukkit.addRecipe(recipe);
            registeredRecipes.add(namespacedKey);
        }
    }

    private List<Map<Character, Material>> generateMaterialCombinations(Map<Character, List<Material>> materialsBySymbol) {
        List<Map<Character, Material>> combinations = new ArrayList<>();
        generateCombinationsRecursive(materialsBySymbol, new HashMap<>(), new ArrayList<>(materialsBySymbol.keySet()), 0, combinations);
        return combinations;
    }

    private void generateCombinationsRecursive(
            Map<Character, List<Material>> materialsBySymbol,
            Map<Character, Material> currentCombination,
            List<Character> symbols,
            int index,
            List<Map<Character, Material>> combinations) {
        if (index >= symbols.size()) {
            combinations.add(new HashMap<>(currentCombination));
            return;
        }

        Character symbol = symbols.get(index);
        List<Material> materials = materialsBySymbol.get(symbol);

        for (Material material : materials) {
            currentCombination.put(symbol, material);
            generateCombinationsRecursive(materialsBySymbol, currentCombination, symbols, index + 1, combinations);
            currentCombination.remove(symbol);
        }
    }

    private NamespacedKey createUniqueNamespacedKey(String fullKey, Map<Character, Material> combination) {
        StringBuilder keyBuilder = new StringBuilder(fullKey.toLowerCase().replace(".", "_"));
        for (Character symbol : combination.keySet()) {
            keyBuilder.append("_").append(combination.get(symbol).name().toLowerCase());
        }
        return new NamespacedKey(getPlugin(), keyBuilder.toString());
    }

    public void unRegisterAllShapedRecipes() {
        for (NamespacedKey key : registeredRecipes) {
            Bukkit.removeRecipe(key);
        }
        registeredRecipes.clear();
    }

    public ItemStack parseResultItem(String recipeKey) {
        return parseItemFromPath("recipes." + recipeKey + ".result");
    }

    public ItemStack parseItemFromPath(String configPath) {
        return parseItemFromConfig(configPath);
    }

    public Map<Character, List<ItemStack>> parseIngredients(String recipeKey) {
        Map<Character, List<ItemStack>> ingredients = new HashMap<>();
        Map<Character, Boolean> transferDataMap = new HashMap<>();
        String basePath = "recipes." + recipeKey + ".ingredients";

        for (String keyStr : recipesConfig.getSectionKeys(basePath)) {
            if (keyStr.length() != 1) continue;
            char key = keyStr.charAt(0);
            String itemPath = basePath + "." + keyStr;
            String type = recipesConfig.getString(itemPath + ".type");
            boolean transferData = false;

            if (type != null && !type.isEmpty() && !"none".equals(type)) {
                transferData = recipesConfig.getBoolean(itemPath + ".transferData");
            }
            transferDataMap.put(key, transferData);

            List<ItemStack> itemList = new ArrayList<>();
            if (recipesConfig.contains(itemPath + ".materials")) {
                List<String> materials = recipesConfig.getStringList(itemPath + ".materials");
                for (String material : materials) {
                    ItemStack item = parseItemFromConfig(itemPath, Material.valueOf(material));
                    itemList.add(item);
                }
            } else if (recipesConfig.contains(itemPath + ".material")) {
                ItemStack item = parseItemFromConfig(itemPath);
                itemList.add(item);
            } else {
                ItemStack item = parseItemFromConfig(itemPath);
                itemList.add(item);
            }

            ingredients.put(key, itemList);
        }
        this.lastTransferDataMap = transferDataMap;
        return ingredients;
    }

    private ItemStack parseItemFromConfig(String configPath, Material material) {
        String type = recipesConfig.getString(configPath + ".type");

        if (type == null || type.isEmpty() || "none".equals(type)) {
            return new ItemStack(material);
        }

        return switch (type) {
            case "enchanted_book" -> {
                String id = recipesConfig.getString(configPath + ".id");
                int level = recipesConfig.getInt(configPath + ".level");
                yield createVanillaEnchantedBook(id, level);
            }
            case "potion", "splash_potion" -> {
                String id = recipesConfig.getString(configPath + ".id");
                int level = recipesConfig.getInt(configPath + ".level");
                int duration = recipesConfig.getInt(configPath + ".duration");
                yield createVanillaPotion(type, id, level, duration);
            }

            default -> createCustomItem(configPath, material);
        };
    }

    private ItemStack parseItemFromConfig(String configPath) {
        String type = recipesConfig.getString(configPath + ".type");
        Material material = recipesConfig.getMaterial(configPath + ".material");

        if (type == null || type.isEmpty() || "none".equals(type)) {
            return new ItemStack(material);
        }

        return switch (type) {
            case "enchanted_book" -> {
                String id = recipesConfig.getString(configPath + ".id");
                int level = recipesConfig.getInt(configPath + ".level");
                yield createVanillaEnchantedBook(id, level);
            }
            case "potion", "splash_potion" -> {
                String id = recipesConfig.getString(configPath + ".id");
                int level = recipesConfig.getInt(configPath + ".level");
                int duration = recipesConfig.getInt(configPath + ".duration");
                yield createVanillaPotion(type, id, level, duration);
            }

            default -> createCustomItem(configPath, material);
        };
    }

    private ItemStack createCustomItem(String configPath, Material material) {
        CustomItemService customItemService = getPlugin().getService(CustomItemService.class);

        String id = recipesConfig.getString(configPath + ".id");
        Component name = recipesConfig.getComponent(configPath + ".name");
        List<Component> lore = recipesConfig.getComponentList(configPath + ".lore");
        boolean enchanted = recipesConfig.getBoolean(configPath + ".enchanted");
        boolean custom = recipesConfig.getBoolean(configPath + ".custom");

        return customItemService.createRecipeItem(id, material, name, lore, enchanted, custom);
    }

    public boolean matchesCraftingMatrix(ItemStack[] matrix, List<String> shape, Map<Character, List<ItemStack>> ingredients) {
        if (matrix.length != 9) {
            return false;
        }
        int index = 0;
        for (String row : shape) {
            for (char c : row.toCharArray()) {
                ItemStack provided = matrix[index];
                if (c == ' ' && (provided == null || provided.getType() == Material.AIR)) {
                    index++;
                    continue;
                }
                List<ItemStack> requiredList = ingredients.get(c);
                if (requiredList == null) {
                    return false;
                }
                boolean match = false;
                for (ItemStack required : requiredList) {
                    if (itemsMatch(required, provided)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    return false;
                }
                index++;
            }
        }
        return true;
    }

    private boolean itemsMatch(ItemStack required, ItemStack provided) {
        if (required == null && provided == null) return true;
        if (required == null || provided == null) return false;
        if (provided.getType() == Material.AIR) return false;
        if (required.getType() != provided.getType()) return false;

        if (!required.hasItemMeta() && !provided.hasItemMeta()) return true;

        ItemMeta metaReq = required.getItemMeta();
        ItemMeta metaProv = provided.getItemMeta();

        if (metaReq == null || metaProv == null) return true;

        if (required.getType() == Material.ENCHANTED_BOOK) {
            return compareEnchantments(metaReq, metaProv);
        }

        if (required.getType() == Material.POTION || required.getType() == Material.SPLASH_POTION) {
            return comparePotionEffects(metaReq, metaProv);
        }

        return comparePersistentData(metaReq, metaProv);
    }

    private boolean compareEnchantments(ItemMeta metaReq, ItemMeta metaProv) {
        Map<Enchantment, Integer> reqEnchants = getAllEnchants(metaReq);
        Map<Enchantment, Integer> provEnchants = getAllEnchants(metaProv);
        return reqEnchants.equals(provEnchants);
    }

    private boolean comparePotionEffects(ItemMeta metaReq, ItemMeta metaProv) {
        if (!(metaReq instanceof PotionMeta reqPotion) || !(metaProv instanceof PotionMeta provPotion)) {
            return false;
        }

        List<PotionEffect> reqEffects = reqPotion.getCustomEffects();
        List<PotionEffect> provEffects = provPotion.getCustomEffects();

        if (reqEffects.size() != provEffects.size()) {
            return false;
        }

        for (int i = 0; i < reqEffects.size(); i++) {
            PotionEffect req = reqEffects.get(i);
            PotionEffect prov = provEffects.get(i);

            if (req.getType() != prov.getType()) {
                return false;
            }
            if (req.getAmplifier() != prov.getAmplifier()) {
                return false;
            }
            if (req.getDuration() != prov.getDuration()) {
                return false;
            }
        }

        if (reqPotion.hasColor() && provPotion.hasColor()) {
            return Objects.equals(reqPotion.getColor(), provPotion.getColor());
        }

        return true;
    }

    private boolean comparePersistentData(ItemMeta metaReq, ItemMeta metaProv) {
        PersistentDataContainer reqPdc = metaReq.getPersistentDataContainer();
        PersistentDataContainer provPdc = metaProv.getPersistentDataContainer();

        for (NamespacedKey key : reqPdc.getKeys()) {
//            if (IGNORED_PDC_KEYS.contains(key.getKey())) continue;

            if (!provPdc.has(key, PersistentDataType.STRING)) return false;

            String reqVal = reqPdc.get(key, PersistentDataType.STRING);
            String provVal = provPdc.get(key, PersistentDataType.STRING);

            if (!Objects.equals(reqVal, provVal)) return false;
        }
        return true;
    }

    private Map<Enchantment, Integer> getAllEnchants(ItemMeta meta) {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        if (meta instanceof EnchantmentStorageMeta storage) {
            enchants.putAll(storage.getStoredEnchants());
        }
        enchants.putAll(meta.getEnchants());
        return enchants;
    }

    public void handlePrepareCraft(ItemStack result, List<String> shape,
                                   Map<Character, List<ItemStack>> ingredients,
                                   boolean transferDataFlag,
                                   CraftingInventory inventory) {
        if (matchesCraftingMatrix(inventory.getMatrix(), shape, ingredients)) {
            if (transferDataFlag) {
                ItemStack source = findTransferDataSource(inventory, shape);
                if (source != null) {
                    transferData(source, result);
                }
            }
            inventory.setResult(result);
        }
    }

    public void handlePrepareItemCraftEvent(PrepareItemCraftEvent event) {
        if (processingInventories.contains(event.getInventory())) return;
        if (!hasCustomItems(event.getInventory().getMatrix())) return;
        processingInventories.add(event.getInventory());

        for (String group : recipesConfig.getSectionKeys("recipes")) {
            for (String recipeKey : recipesConfig.getSectionKeys("recipes." + group)) {
                String fullKey = group + "." + recipeKey;
                if (!recipesConfig.getBoolean("recipes." + fullKey + ".useListener")) continue;

                ItemStack result = parseResultItem(fullKey);
                List<String> shape = recipesConfig.getStringList("recipes." + fullKey + ".shape");
                Map<Character, List<ItemStack>> ingredients = parseIngredients(fullKey);
                boolean transferDataFlag = "rod".equalsIgnoreCase(recipesConfig.getString("recipes." + fullKey + ".result.type"));

                handlePrepareCraft(result, shape, ingredients, transferDataFlag, event.getInventory());
            }
        }

        processingInventories.remove(event.getInventory());
    }

    public void handleInventoryClickEvent(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || clickedInventory.getType() != InventoryType.WORKBENCH || event.getSlot() != 0) return;
        if (!hasCustomItems(((CraftingInventory) clickedInventory).getMatrix())) return;
        if (processingInventories.contains(clickedInventory)) return;

        processingInventories.add(clickedInventory);

        boolean handled = false;
        for (String group : recipesConfig.getSectionKeys("recipes")) {
            for (String recipeKey : recipesConfig.getSectionKeys("recipes." + group)) {
                String fullKey = group + "." + recipeKey;
                if (!recipesConfig.getBoolean("recipes." + fullKey + ".useListener")) continue;

                ItemStack result = parseResultItem(fullKey);
                List<String> shape = recipesConfig.getStringList("recipes." + fullKey + ".shape");
                Map<Character, List<ItemStack>> ingredients = parseIngredients(fullKey);

                if (handleClickCraft(result, shape, ingredients, event)) {
                    handled = true;
                    break;
                }
            }
            if (handled) break;
        }

        processingInventories.remove(clickedInventory);
    }

    public boolean handleClickCraft(ItemStack result, List<String> shape,
                                    Map<Character, List<ItemStack>> ingredients,
                                    InventoryClickEvent event) {
        if (!(event.getInventory() instanceof CraftingInventory craftingInventory)) return false;
        if (event.getSlot() != 0) return false;
        if (!matchesCraftingMatrix(craftingInventory.getMatrix(), shape, ingredients)) return false;

        event.setCancelled(true);

        int craftAmount = calculateCraftAmount(event, craftingInventory, shape, ingredients);
        if (craftAmount <= 0) return false;

        ItemStack cursor = event.getWhoClicked().getItemOnCursor();
        if (!canAddToStack(cursor, result, craftAmount)) return false;

        ItemStack source = findTransferDataSource(craftingInventory, shape);
        ItemStack give = prepareResult(result, craftAmount, source);

        consumeIngredients(craftingInventory, shape, ingredients, craftAmount);
        giveResultToPlayer(event, cursor, give, source);

        craftingInventory.setResult(new ItemStack(Material.AIR));
        return true;
    }

    private int calculateCraftAmount(InventoryClickEvent event, CraftingInventory inventory,
                                     List<String> shape, Map<Character, List<ItemStack>> ingredients) {
        if (event.getClick() == ClickType.LEFT) {
            return 1;
        }

        if (event.getClick() != ClickType.SHIFT_LEFT) {
            return 0;
        }

        int maxAmount = Integer.MAX_VALUE;
        ItemStack[] matrix = inventory.getMatrix();

        for (int i = 0; i < 9; i++) {
            ItemStack item = matrix[i];
            if (item == null || item.getType() == Material.AIR) continue;

            char sym = getSymbolForSlot(i, shape);
            if (sym == ' ') continue;

            List<ItemStack> reqList = ingredients.get(sym);
            if (reqList == null || reqList.isEmpty()) continue;

            ItemStack req = reqList.getFirst();
            maxAmount = Math.min(maxAmount, item.getAmount() / req.getAmount());
        }

        return Math.max(1, maxAmount);
    }

    private boolean canAddToStack(ItemStack cursor, ItemStack result, int amount) {
        if (cursor.getType() == Material.AIR) return true;

        if (!cursor.isSimilar(result)) return false;

        int available = cursor.getMaxStackSize() - cursor.getAmount();
        return available >= amount;
    }

    private ItemStack findTransferDataSource(CraftingInventory inventory, List<String> shape) {
        if (lastTransferDataMap == null || lastTransferDataMap.isEmpty()) return null;

        boolean hasTransferData = lastTransferDataMap.values().stream().anyMatch(Boolean::booleanValue);
        if (!hasTransferData) return null;

        for (int i = 0; i < 9; i++) {
            char sym = getSymbolForSlot(i, shape);
            if (sym == ' ') continue;
            if (!lastTransferDataMap.getOrDefault(sym, false)) continue;

            ItemStack item = inventory.getMatrix()[i];
            if (item != null && item.hasItemMeta()) {
                return item;
            }
        }
        return null;
    }

    private ItemStack prepareResult(ItemStack result, int amount, ItemStack source) {
        ItemStack give = result.clone();
        give.setAmount(amount);

        if (source != null) {
            transferData(source, give);
        }
        return give;
    }

    private void consumeIngredients(CraftingInventory inventory, List<String> shape,
                                    Map<Character, List<ItemStack>> ingredients, int amount) {
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getMatrix()[i];
            if (item == null || item.getType() == Material.AIR) continue;

            char sym = getSymbolForSlot(i, shape);
            if (sym == ' ') continue;

            List<ItemStack> reqList = ingredients.get(sym);
            if (reqList == null || reqList.isEmpty()) continue;

            ItemStack req = reqList.getFirst();
            int reduce = req.getAmount() * amount;
            item.setAmount(item.getAmount() - reduce);
            inventory.setItem(i + 1, item.getAmount() > 0 ? item : null);
        }
    }

    private void giveResultToPlayer(InventoryClickEvent event, ItemStack cursor,
                                    ItemStack give, ItemStack source) {
        if (cursor.getType() == Material.AIR) {
            event.getWhoClicked().setItemOnCursor(give);
        } else {
            cursor.setAmount(cursor.getAmount() + give.getAmount());

            if (source != null && cursor.hasItemMeta()) {
                transferData(source, cursor);
            }
        }
    }

    public boolean hasCustomItems(ItemStack[] matrix) {
        for (ItemStack item : matrix) {
            if (item != null && item.hasItemMeta()) {
                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                if (!pdc.getKeys().isEmpty()) {
                    for (NamespacedKey key : pdc.getKeys()) {
//                        if (!IGNORED_PDC_KEYS.contains(key.getKey())) {
//                            return true;
//                        }
                    }
                }
            }
        }
        return false;
    }

    private List<String> validateShape(List<String> shape) {
        List<String> validated = new ArrayList<>();
        for (String row : shape) {
            if (row.length() > 3) {
                row = row.substring(0, 3);
            }
            StringBuilder rowBuilder = new StringBuilder(row);
            while (rowBuilder.length() < 3) {
                rowBuilder.append(" ");
            }
            row = rowBuilder.toString();
            validated.add(row);
        }
        while (validated.size() < 3) {
            validated.add("   ");
        }
        return validated;
    }

    private char getSymbolForSlot(int slot, List<String> shape) {
        int row = slot / 3;
        int col = slot % 3;
        if (row >= shape.size()) return ' ';

        String line = shape.get(row);
        if (col >= line.length()) return ' ';

        return line.charAt(col);
    }

    public void transferData(ItemStack source, ItemStack target) {
        if (source == null || !source.hasItemMeta()) return;

        ItemMeta srcMeta = source.getItemMeta();
        ItemMeta tgtMeta = target.getItemMeta();
        if (tgtMeta == null) return;

        PersistentDataContainer src = srcMeta.getPersistentDataContainer();
        PersistentDataContainer tgt = tgtMeta.getPersistentDataContainer();

        for (NamespacedKey key : src.getKeys()) {
            String keyName = key.getKey();

            if ("rod_type".equals(keyName) || "rod_uid".equals(keyName)) continue;

            if (src.has(key, PersistentDataType.STRING)) {
                tgt.set(key, PersistentDataType.STRING, src.get(key, PersistentDataType.STRING));
            } else if (src.has(key, PersistentDataType.INTEGER)) {
                tgt.set(key, PersistentDataType.INTEGER, src.get(key, PersistentDataType.INTEGER));
            } else if (src.has(key, PersistentDataType.DOUBLE)) {
                tgt.set(key, PersistentDataType.DOUBLE, src.get(key, PersistentDataType.DOUBLE));
            }
        }

        target.setItemMeta(tgtMeta);
    }

    public ItemStack createVanillaEnchantedBook(String enchantmentId, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        if (meta == null) return book;

        NamespacedKey key = NamespacedKey.minecraft(enchantmentId.toLowerCase(Locale.ROOT));
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Enchantment enchantment = registry.get(key);

        if (enchantment != null) {
            int actualLevel = level <= 0 ? 1 : Math.min(level, enchantment.getMaxLevel());
            meta.addStoredEnchant(enchantment, actualLevel, true);
        } else {
            Enchantment fallback = registry.get(NamespacedKey.minecraft("unbreaking"));
            if (fallback != null) {
                meta.addStoredEnchant(fallback, 1, true);
            }
        }

        book.setItemMeta(meta);
        return book;
    }

    public ItemStack createVanillaPotion(String type, String potionId, int level, int duration) {
        Material mat = "splash_potion".equals(type) ? Material.SPLASH_POTION : Material.POTION;
        ItemStack potion = new ItemStack(mat);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta == null) return potion;

        try {
            String potionTypeName = potionId.toUpperCase();
            PotionType potionType = null;

            if (level >= 1) {
                try {
                    potionType = PotionType.valueOf("STRONG_" + potionTypeName);
                } catch (IllegalArgumentException ignored) {}
            }

            if (potionType == null) {
                if (duration > 1800) {
                    try {
                        potionType = PotionType.valueOf("LONG_" + potionTypeName);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            if (potionType == null) {
                potionType = PotionType.valueOf(potionTypeName);
            }
            meta.setBasePotionType(potionType);

        } catch (IllegalArgumentException e) {
            meta.setBasePotionType(PotionType.AWKWARD);
        }

        potion.setItemMeta(meta);
        return potion;
    }
}