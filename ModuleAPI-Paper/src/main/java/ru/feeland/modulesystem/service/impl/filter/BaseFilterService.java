package ru.feeland.modulesystem.service.impl.filter;

import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.service.BaseService;

/**
 * Универсальный базовый сервис переключения enum‑фильтров.
 * <p>
 * Хранит текущее значение фильтра, умеет переключать его по кругу,
 * сбрасывать к значению по умолчанию и отдавать полный список
 * возможных значений. Конкретные реализации указывают тип enum и
 * дефолтное значение через абстрактные методы.
 * </p>
 *
 * @param <T> тип enum‑фильтра
 */
public abstract class BaseFilterService<T extends Enum<T>> extends BaseService {
    protected final T[] values;
    protected final T defaultValue;
    protected int currentIndex;
    protected T currentValue;

    /**
     * Конструктор, принимающий только плагин.
     * <p>
     * Тип enum и дефолтное значение инициализируются через
     * {@link #getEnumClass()} и {@link #getDefaultValue()}, после чего
     * фильтр сбрасывается к значению по умолчанию.
     * </p>
     */
    public BaseFilterService(BaseModuleSystem plugin) {
        super(plugin);
        this.values = getEnumClass().getEnumConstants();
        this.defaultValue = getDefaultValue();
        resetToDefault();
    }

    /**
     * Какой enum используется в конкретной реализации сервиса.
     */
    protected abstract Class<T> getEnumClass();

    /**
     * Значение фильтра по умолчанию для конкретной реализации.
     */
    protected abstract T getDefaultValue();

    /**
     * Устанавливает фильтр в произвольное значение и синхронизирует индекс.
     */
    public void setFilter(T value) {
        this.currentValue = value;
        this.currentIndex = indexOf(value);
    }

    private void move(int step) {
        currentIndex = (currentIndex + step + values.length) % values.length;
        currentValue = values[currentIndex];
    }

    public void switchFilterNext() {
        move(1);
    }

    public void switchFilterPrevious() {
        move(-1);
    }

    /**
     * Возвращает текущее значение фильтра.
     */
    public T getCurrentFilter() {
        return currentValue;
    }

    /**
     * Сбрасывает фильтр к значению по умолчанию.
     */
    public void resetToDefault() {
        setFilter(defaultValue);
    }

    /**
     * Возвращает копию массива всех возможных значений фильтра.
     */
    public T[] getAllValues() {
        return values.clone();
    }

    private int indexOf(T value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) return i;
        }
        return 0;
    }
}