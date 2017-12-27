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
package com.jagrosh.jdautilities.modules.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * @author Kaidan Gustave
 */
public final class FileUtil
{
    public static final Function<? super URI, ? extends URL> URI_TO_URL = uri -> {
        try
        {
            return uri.toURL();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    };

    public static File getFile(String path)
    {
        return getFile(path, "/");
    }

    public static File getFile(String path, String separator)
    {
        return Paths.get(System.getProperty("user.dir"), path.split(separator)).toFile();
    }
}
