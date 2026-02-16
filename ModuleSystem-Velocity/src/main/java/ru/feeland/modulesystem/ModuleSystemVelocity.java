package ru.feeland.modulesystem;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import ru.feeland.modulesystem.aware.DestroyAware;
import ru.feeland.modulesystem.initializer.impl.CoreInitializerImpl;
import ru.feeland.modulesystem.initializer.impl.ModuleInitializerImpl;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.logger.impl.BaseLoggerProcessor;

import java.nio.file.Path;

@Plugin(
    id = "modulesystem",
    name = "ModuleSystem",
    version = "1.0.0",
    description = "Модульная система для Velocity",
    authors = {"Floria"}
)
public final class ModuleSystemVelocity extends BaseModuleSystemVelocity {
    @Inject
    public ModuleSystemVelocity(ProxyServer server, org.slf4j.Logger logger, @DataDirectory Path dataFolder) {
        super(server, logger, dataFolder);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        setCoreInitializer(CoreInitializerImpl.of(this));
        getCoreInitializer().init();

        Logger.info().log(BaseLoggerProcessor.ConsoleColors.RED +
            """
            
            
                                    _._     _,-'""`-._                                        __
                                    (,-.`._,'(       |\\`-/|                                 <(o )___
                                        `-.-' \\ )-`( , o o)                                  ( ._> /
                                              `-    \\`_`"'-                                   `---'
            
            """ + BaseLoggerProcessor.ConsoleColors.PURPLE_BRIGHT + """
                        888b     d888               888          888          .d8888b.                    888
                        8888b   d8888               888          888         d88P  Y88b                   888
                        88888b.d88888               888          888         Y88b.                        888
                        888Y88888P888  .d88b.   .d88888 888  888 888  .d88b.  "Y888b.   888  888 .d8888b  888888 .d88b.  88888b.d88b.
                        888 Y888P 888 d88""88b d88" 888 888  888 888 d8P  Y8b    "Y88b. 888  888 88       d8P   d8P  Y8b 888 "88b "88b
                        888  Y8P  888 888  888 888  888 888  888 888 88888888      "888 888  888 "Y8888b. 888   88888888 888  888  888
                        888   "   888 Y88..88P Y88b 888 Y88b 888 888 Y8b.    Y88b  d88P Y88b 888      X88 Y88b. Y8b.     888  888  888
                        888       888  "Y88P"   "Y88888  "Y88888 888  "Y8888  "Y8888P"   "Y88888  88888P'  "Y888 "Y8888  888  888  888
                                                                                           888
                                                                                      Y8b d88P
                                                                                       "Y88P"
            """ + BaseLoggerProcessor.ConsoleColors.RESET);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        getInitializer(ModuleInitializerImpl.class).getComponents().forEach(DestroyAware::destroy);
    }
}

