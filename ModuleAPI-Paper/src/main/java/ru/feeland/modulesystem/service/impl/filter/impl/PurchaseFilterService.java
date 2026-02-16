package ru.feeland.modulesystem.service.impl.filter.impl;

import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.enums.PurchaseFilterType;
import ru.feeland.modulesystem.service.impl.filter.BaseFilterService;

/**
 * Конкретная реализация {@link BaseFilterService} для покупок.
 * <p>
 * Управляет текущим фильтром списка покупок по типу {@link PurchaseFilterType}
 * (например, куплено/доступно и т.п.) и задаёт дефолтное значение.
 * </p>
 */
public class PurchaseFilterService extends BaseFilterService<PurchaseFilterType> {
    /**
     * Создаёт сервис фильтрации покупок, привязанный к плагину.
     *
     * @param plugin основной модуль, через который инициализируется базовый сервис
     */
    public PurchaseFilterService(BaseModuleSystem plugin) {
        super(plugin);
    }

    @Override
    protected Class<PurchaseFilterType> getEnumClass() {
        return PurchaseFilterType.class;
    }

    @Override
    protected PurchaseFilterType getDefaultValue() {
        return PurchaseFilterType.BOUGHT;
    }
}