# ModuleSystem / ModuleAPI – актуальное руководство

## 1. Общая идея

`ModuleAPI` — это библиотека для создания модулей к основному плагину `ModuleSystem`.
Модуль — это отдельный JAR, который:

- регистрирует **команды**, **сервисы**, **слушатели**, **конфиги**, **меню**;
- может использовать общую инфраструктуру (`BaseModule`, `BaseService`, `BaseConfig`,
  `BaseMenu`, фильтры, SQLite‑слой, HTTP‑клиент и т.д.), не дублируя код;
- загружается и выгружается во время работы сервера через `ModuleService`;
- включается/отключается через конфиг основного плагина (`modules.<name>.enabled`).

📄 Подробнее о доступных Gradle задачах см. в [TASKS.md](./TASKS.md)

## 2. Как устроен модуль

Модуль реализует интерфейс `ru.feeland.modulesystem.module.Module` и, как правило,
наследуется от `ru.feeland.modulesystem.module.BaseModule`.

Пример структуры проекта модуля:

```text
IdeaProjects/
├── ModuleSystem/
│   └── ModuleAPI/
└── YourModule/
    ├── build.gradle
    └── src/
        └── main/
            └── java/
                └── ru/
                    └── feeland/
                            └── module/
                                └── YourModule.java
```

Базовый модуль:

```java
package ru.feeland.modulesystem.module;

import org.bukkit.event.Listener;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.command.Command;
import ru.feeland.modulesystem.config.Config;
import ru.feeland.modulesystem.module.BaseModule;
import ru.feeland.modulesystem.service.Service;

import java.util.stream.Stream;

public class YourModule extends BaseModule {

    public YourModule(BaseModuleSystem plugin) {
        super(plugin);
    }

    @Override
    public Stream<Command> getCommands() {
        return Stream.empty();
    }

    @Override
    public Stream<Listener> getListeners() {
        return Stream.empty();
    }

    @Override
    public Stream<Config> getConfigs() {
        return Stream.empty();
    }

    @Override
    public Stream<Service> getServices() {
        return Stream.empty();
    }
}
```

Модуль загружается/выгружается через `ModuleService` основного плагина и
управляется через `MainConfig` (`modules.<name>.enabled`).

## 3. build.gradle модуля

Модуль — это обычный Gradle‑проект, который подключает `ModuleAPI` как локальную зависимость
из папки `../libs` (туда вы предварительно кладёте JAR `ModuleAPI`):

```gradle
plugins {
    id 'java'
    id "io.github.goooler.shadow" version "8.1.8"
}

group = 'ru.feeland'
version = ''

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = "https://repo.papermc.io/repository/maven-public/"
    }
}

dependencies {
    compileOnly "io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT"
    implementation(fileTree(dir: '../libs', include: ['ModuleAPI-1.0-SNAPSHOT.jar']))
}

shadowJar {
    if (System.getProperty('os.name').toLowerCase().contains('win')) {
        destinationDirectory = file("C:/Users/${System.getProperty('user.name')}/Desktop/folia8/plugins/ModuleSystem/modules")
    }
    archiveClassifier.set("")
}
```

Сборка:

```bash
./gradlew shadowJar
```

На Windows JAR модуля автоматически копируется в папку модулей
основного плагина.

## 4. Сервисы

Bазовая абстракция:

- `ru.feeland.modulesystem.service.Service` — маркерный интерфейс слоя сервисов;
- `ru.feeland.modulesystem.service.BaseService` — базовый класс, который хранит ссылку на `BaseModuleSystem`.

Свои сервисы вы реализуете как наследников `BaseService` и возвращаете их из `getServices()` модуля.
Простой пример:

```java
public class MyService extends BaseService {

    public MyService(BaseModuleSystem plugin) {
        super(plugin);
    }

    public void doSomething() {
        Logger.info().log("Hello from {}", getPlugin().getName());
    }
}
```

Регистрация в модуле:

```java
@Override
public Stream<Service> getServices() {
    return Stream.of(
        new MyService(getPlugin())
    );
}
```

Получение сервиса из другого кода:

```java
MyService myService = getPlugin().getService(MyService.class);
myService.doSomething();
```

Примеры готовых сервисов в `ModuleAPI`:

- `HttpService` — HTTP‑клиент с типизированными ответами;
- `ModuleService` — загрузка/выгрузка модулей из JAR;
- `CommandSystemService` — регистрация/разрегистрация Bukkit‑команд.

## 5. Работа с SQLite

Для работы с локальной SQLite предусмотрены:

- интерфейс `SQLiteService` описывает базовые операции: `connect()`, `close()`,
  `createTables()`, `loadCaches()`, CRUD через DTO;
- абстрактный класс `BaseSQLiteService` реализует типовую логику:
  создание файла БД, подключение, вызовы `createTables()/loadCaches()`,
  методы `saveToDatabase/updateInDatabase/removeFromDatabase`.

Таблицы описываются через:

- `Table` + `TableAware`;
- дополнительные интерфейсы `CreateTableAware`, `LoadCacheTableAware` для
  создания схемы и загрузки кэшей.

Типичный сервис базы данных в модуле:

```java
public class MySQLiteService extends BaseSQLiteService {

    public MySQLiteService(BaseModuleSystem plugin) {
        super(plugin);
    }

    @Override
    public String getDatabaseName() {
        return "my_module";
    }
}
```

Регистрация в модуле:

```java
@Override
public Stream<Service> getServices() {
    return Stream.of(
        new MySQLiteService(getPlugin())
    );
}
```

Дальше вы можете вызывать, например:

```java
mySQLiteService.connect();
mySQLiteService.saveToDatabase(dto);
```

## 6. Menu (`Menu` / `BaseMenu`)

Интерфейс:

- `Menu` — простое меню с методами `open()` и `updateGui()`;
- `BaseMenu` — базовый класс на Triumph‑GUI:
  - `createGui(...)` / `createPaginatedGui(...)`;
  - `addMenuItems(...)` — заполнение обычных меню из `MenusConfig`;
  - `addPaginatedMenuItems(...)` — пагинируемые меню с фильтрами.

Меню завязаны на:

- `CustomItemService` — создание `ItemStack` по конфигу;
- `BaseFilterService` — переключение фильтров (`previous`, `next`, `filter`‑кнопки).

Пример простого меню:

```java
public class ExampleMenu extends BaseMenu {

    public ExampleMenu(BaseModuleSystem plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        Gui gui = createGui(Component.text("Пример"), 3, false);
        // заполняем элементы через addMenuItems(...)
        gui.open(getPlayer());
    }

    @Override
    public void updateGui() {
        // перечитать конфиг/данные и заново заполнить слоты
    }
}
```

## 7. Фильтры (`BaseFilterService<T>`)

`BaseFilterService<T extends Enum<T>>` — универсальный базовый класс для
переключения enum‑фильтра:

- хранит массив значений `values`;
- предоставляет `switchFilter()`, `resetToDefault()`, `getCurrentFilter()`,
  `getAllValues()`;
- конкретные реализации определяют:
  - `getEnumClass()` — какой enum;
  - `getDefaultValue()` — значение по умолчанию.

Пример: `PurchaseFilterService` для `PurchaseFilterType` (фильтр покупок),
который удобно использовать вместе с пагинируемыми меню (`BaseMenu`).

## 8. HTTP‑клиент (`HttpService`)

`HttpService` — клиент для внешнего API:

- использует `java.net.http.HttpClient` и `ObjectMapper`;
- берёт `apiUrl` и `apiToken` из `MainConfig`;
- методы `get/post/put/delete` возвращают `HttpResponseContent<T>`:
  - `body` — типизированное тело;
  - `apiExceptionResponse` — ошибка API;
  - `exception` — техническое исключение;
  - `code` и `isSuccess()` — HTTP‑код/успех.

Метод `failure(Player, HttpResponseContent)` логирует ошибку и
отправляет игроку человекочитаемое сообщение.

## 9. Логирование

Система модулей предоставляет фасад над SLF4J — `ru.feeland.modulesystem.logger.Logger`:

- уровни: `trace()`, `debug()`, `info()`, `warn()`, `error()`;
- включение/отключение уровней — через основную конфигурацию плагина
  (`logger.trace`, `logger.debug`, `logger.info`, `logger.warn`, `logger.error`).

Пример:

```java
import ru.feeland.modulesystem.logger.Logger;

public class YourService {

    public void doSomething(String moduleName) {
        Logger.info().log("Модуль {} успешно загрузился", moduleName);
        Logger.debug().log("Отладочная информация по модулю {}", moduleName);
    }

    public void handleError(Exception e) {
        Logger.error().log("Произошла ошибка в модуле", e);
    }
}
```

## 10. Команды и SubCommand

API поддерживает вложенные команды:

- базовая команда — наследник `ru.feeland.modulesystem.command.BaseCommand`,
  возвращает список подкоманд;
- подкоманда — наследник `ru.feeland.modulesystem.command.subcommand.BaseSubCommand`.

Для валидации используется `CommandValidationBuilder`, DTO — `CommandDTO`,
конфиги сообщений — `MessagesConfig`.

Подкоманды регистрируются через `getSubCommands()` в наследнике `BaseCommand`,
а сами команды — через `CommandInitializer` / `CommandSystemService`.

## 11. Конфиги модулей

`BaseConfig` автоматически выгружает `config.yml` из ресурсов модуля в:

```text
plugins/ModuleSystem/modules/<ИмяМодуля>/config.yml
```

Дальнейшая работа идёт только с внешним файлом.

Пример:

```java
public class MainConfig extends BaseConfig {

    public MainConfig(BaseModuleSystem plugin, String moduleName) {
        super(plugin, moduleName);
    }
}
```

Конфиги регистрируются в модуле через `getConfigs()` и
