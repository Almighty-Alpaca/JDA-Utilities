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

import com.jagrosh.jdautilities.modules.providers.ModuleNotFoundException;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings("RedundantThrows")
public interface ModuleFactory<T, M extends Module<T>>
{
    M create(ClassLoader classLoader) throws ModuleNotFoundException, ModuleException;

    String getFileExtension();

    default void check(M module) throws Exception {}

    default M load(ClassLoader classLoader) throws ModuleNotFoundException, ModuleException
    {
        M module = create(classLoader);

        try
        {
            check(module);
        }
        catch (Throwable t)
        {
            if (t instanceof ModuleException)
                throw (ModuleException) t;
            throw new ModuleException("Failed to load module!", t);
        }

        return module;
    }
}
