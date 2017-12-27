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

import com.jagrosh.jdautilities.modules.Module;
import com.jagrosh.jdautilities.modules.ModuleException;
import com.jagrosh.jdautilities.modules.ModuleFactory;
import com.jagrosh.jdautilities.modules.providers.ModuleNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kaidan Gustave
 */
public class JSONModule extends Module<JSONObject>
{
    JSONModule(ClassLoader classLoader) throws ModuleNotFoundException, ModuleException
    {
        super(classLoader, loader -> {
            URL url = loader.getResource("/module.json");

            if (url == null)
                throw new ModuleNotFoundException("Could not find module.json!");

            try
            {
                return new JSONObject(new JSONTokener(url.openStream()));
            }
            catch (IOException | JSONException ex)
            {
                throw new ModuleException("Encountered an error reading module.json", ex);
            }
        });
    }

    @Override
    protected void init(ClassLoader classLoader)
    {
        if (moduleConfig.has("name") && !moduleConfig.isNull("name"))
        {
            this.name = moduleConfig.get("name").toString();
        }
    }

    public static class Factory implements ModuleFactory<JSONObject, JSONModule>
    {
        @Override
        public JSONModule create(ClassLoader classLoader) throws ModuleNotFoundException, ModuleException
        {
            return new JSONModule(classLoader);
        }

        @Override
        public String getFileExtension()
        {
            return "json";
        }
    }
}
