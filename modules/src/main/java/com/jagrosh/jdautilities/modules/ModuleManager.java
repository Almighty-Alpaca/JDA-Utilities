/*
 * Copyright 2016 John Grosh (jagrosh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.modules;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.modules.providers.ModuleNotFoundException;
import com.jagrosh.jdautilities.modules.providers.ModuleProvider;
import com.jagrosh.jdautilities.modules.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Kaidan Gustave
 */
public class ModuleManager
{
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(ModuleManager.class);
    //    private final Map<String, ModuleFactory<?, ?>> loadedFactories;
    @Nonnull
    private final CommandClient representedClient;
    @Nonnull
    private final Map<String, Module<?>> modules;

    public ModuleManager(@Nonnull CommandClient representedClient)
    {
        //        this.loadedFactories = new HashMap<>();
        this.representedClient = representedClient;
        this.modules = new HashMap<>();
    }

    public void loadModule(@Nonnull String providerId, @Nonnull String moduleId, @Nonnull String... locations) throws ModuleNotFoundException
    {
        Map<String, ModuleProvider> providers = getModuleProviders();

        ModuleProvider provider = providers.get(providerId);

        if (provider == null)
            throw new ModuleNotFoundException("Unknown ModuleProvider: " + providerId);
        else
            loadModule(provider, moduleId, locations);
    }

    public void loadModule(@Nonnull ModuleProvider provider, @Nonnull String moduleId, @Nonnull String... locations) throws ModuleNotFoundException
    {
        Path[] paths = provider.resolve(moduleId, locations);

        // @formatter:off
        URL[] urls = Arrays.stream(paths)
                .map(Path::toUri)
                .map(FileUtil.URI_TO_URL)
                .toArray(URL[]::new);

        ClassLoader classLoader = new URLClassLoader(urls);

        ServiceLoader<ModuleFactory> serviceLoader = ServiceLoader.load(ModuleFactory.class, classLoader);

        Map<String, ModuleFactory> moduleFactories = serviceLoader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toMap(ModuleFactory::getFileExtension, Function.identity()));

        List<ModuleException> exceptions = new ArrayList<>();
        Module<?> module = moduleFactories.values().stream()
                .map(mf -> {
                    try
                    {
                        return mf.create(classLoader);
                    }
                    catch (ModuleNotFoundException e)
                    {
                        return null;
                    }
                    catch (ModuleException e)
                    {
                        exceptions.add(e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> {
                    ModuleException e = new ModuleException();
                    for (ModuleException cause : exceptions)
                    {
                        e.initCause(cause);
                        e = cause;
                    }
                    return e;
                });

        if (exceptions.size() == 1)
            ModuleManager.LOG.warn("Caught an exception while constructing a plugin although it has still been loaded.\n{}", (Object) exceptions.get(0));
        else if (exceptions.size() > 1)
            ModuleManager.LOG.warn("Caught multiple exceptions while constructing a plugin although it has still been loaded.\n{}",
                     exceptions.stream()
                             .map(e -> {
                                 StringWriter sw = new StringWriter();
                                 e.printStackTrace(new PrintWriter(sw));
                                 return sw.toString();
                             })
            .collect(Collectors.joining("\n")));
        // @formatter:on

        this.modules.put(module.getName().toLowerCase(), module);

    }

    private Map<String, ModuleProvider> getModuleProviders()
    {
        // @formatter:off
        return ServiceLoader.load(ModuleProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toMap(m -> m.getId().toLowerCase(), Function.identity()));
        // @formatter:on
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> Module<T> getModule(@Nonnull String name)
    {
        return (Module<T>) this.modules.get(name.toLowerCase());
    }

    public void unloadModule(@Nonnull String name)
    {
        Module<?> module = getModule(name);

        if (module != null)
        {
            // Remove it from the loaded
            this.modules.remove(module.getName().toLowerCase());

            // @formatter:off
            Collection<Class<? extends Command>> moduleContents = module.getCommands().stream()
                    .map(Module.Entry::getCommandClass)
                    .collect(Collectors.toList());

            this.representedClient.getCommands().stream()
                    .filter(c -> moduleContents.contains(c.getClass()))
                    .forEach(c -> this.representedClient.removeCommand(c.getName()));
            // @formatter:on

            ClassLoader classLoader = module.getClassloader();

            if (classLoader instanceof Closeable)
                try
                {
                    ((Closeable) classLoader).close();
                }
                catch (IOException ignored) {}
        }
    }
}
