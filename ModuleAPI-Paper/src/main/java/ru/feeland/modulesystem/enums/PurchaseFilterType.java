package ru.feeland.modulesystem.enums;

public enum PurchaseFilterType {
    BOUGHT("Куплено"),
    NOT_BOUGHT("Не куплено"),
    ALL("Все");

    private final String name;
    
    PurchaseFilterType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}