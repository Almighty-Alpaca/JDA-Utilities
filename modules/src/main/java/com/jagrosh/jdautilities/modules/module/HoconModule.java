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
package com.jagrosh.jdautilities.modules.module;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.modules.Module;
import com.jagrosh.jdautilities.modules.ModuleException;
import com.jagrosh.jdautilities.modules.ModuleFactory;
import com.jagrosh.jdautilities.modules.providers.ModuleNotFoundException;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * @author Kaidan Gustave
 */
public class HoconModule extends Module<CommentedConfigurationNode>
{
    private static final String NO_NODE_FOUND_FORMAT = "Module configuration node '%s' was not found!";

    HoconModule(ClassLoader classLoader) throws ModuleNotFoundException, ModuleException
    {
        super(classLoader, loader -> {
            HoconConfigurationLoader.Builder builder = HoconConfigurationLoader.builder();

            builder.setRenderOptions(ConfigRenderOptions.defaults().setComments(true));

            // We should allow missing so we can handle it ourselves.
            builder.setParseOptions(ConfigParseOptions.defaults().setAllowMissing(true));

            URL url = loader.getResource("/module.conf");

            if (url == null)
                throw new ModuleNotFoundException("Could not find module.conf!");

            builder.setSource(() -> new BufferedReader(new InputStreamReader(url.openStream())));

            HoconConfigurationLoader hoconLoader = builder.build();

            if (!hoconLoader.canLoad()) // If we can't load, we throw an exception explaining ourselves
                throw new ModuleException("Could not create module because module.conf could not be loaded!");

            try
            {
                return hoconLoader.load();
            }
            catch (Exception e)
            {
                throw new ModuleException(e);
            }
        });
    }

    @Override
    protected void init(ClassLoader classLoader)
    {
        CommentedConfigurationNode moduleNode = moduleConfig.getNode("module");

        if (moduleNode.isVirtual()) // The base node does not exist
            throw new ModuleException(String.format(NO_NODE_FOUND_FORMAT, "module"));

        CommentedConfigurationNode nameNode = moduleNode.getNode("name");

        // If there is no name this will throw an exception later.
        if (!nameNode.isVirtual())
        {
            this.name = nameNode.getValue().toString();
        }

        // TODO Extras

        CommentedConfigurationNode commandsNode = moduleNode.getNode("commands");

        if (commandsNode.isVirtual()) // Commands node doesn't exist
            throw new ModuleException(String.format(NO_NODE_FOUND_FORMAT, "module.commands"));

        List<String> classNameStrings = commandsNode.getList(Object::toString);

        for (String classNameString : classNameStrings)
        {
            try
            {
                Class<? extends Command> clazz = classLoader.loadClass(classNameString).asSubclass(Command.class);

                createEntry(clazz);
            }
            catch (ClassNotFoundException ex)
            {
                throw new ModuleException(String.format("Could not find class specified in module.conf: %s", classNameString), ex);
            }
            catch (ClassCastException ex)
            {
                throw new ModuleException(String.format("Could not cast class '%s' to Command", classNameString), ex);
            }
        }
    }

    public static class Factory implements ModuleFactory<CommentedConfigurationNode, HoconModule>
    {
        @Override
        public String getFileExtension()
        {
            return "conf";
        }

        @Override
        public HoconModule create(ClassLoader classLoader) throws ModuleNotFoundException, ModuleException
        {
            return new HoconModule(classLoader);
        }
    }
}
