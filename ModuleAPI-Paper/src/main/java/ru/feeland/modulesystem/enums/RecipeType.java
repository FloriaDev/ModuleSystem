package ru.feeland.modulesystem.enums;

public enum RecipeType {
    CRAFTING_TABLE("crafting_table", 3, 3),
    FURNACE("furnace", 1, 1),
    BLAST_FURNACE("blast_furnace", 1, 1),
    SMOKER("smoker", 1, 1),
    SMITHING_TABLE("smithing_table", 2, 1),
    ANVIL("anvil", 2, 1);

    private final String key;
    private final int inputWidth;
    private final int inputHeight;

    RecipeType(String key, int width, int height) {
        this.key = key;
        this.inputWidth = width;
        this.inputHeight = height;
    }

    public String getKey() {
        return key;
    }

    public int getInputWidth() {
        return inputWidth;
    }

    public int getInputHeight() {
        return inputHeight;
    }

    public static RecipeType fromKey(String key) {
        for (RecipeType type : values()) {
            if (type.key.equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null; // или throw IllegalArgumentException
    }
    }