package com.jagrosh.jdautilities.modules.providers.impl;

import com.jagrosh.jdautilities.modules.providers.ModuleNotFoundException;
import com.jagrosh.jdautilities.modules.providers.ModuleProvider;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class FileModuleProvider implements ModuleProvider
{
    @Override
    @Nonnull
    public String getId()
    {
        return "file";
    }

    @Override
    @Nonnull
    public Path[] resolve(@Nonnull String id, @Nonnull String... locations) throws ModuleNotFoundException
    {
        // @formatter:off
        return Arrays.stream(locations)
                .filter(Objects::nonNull)
                .map(s -> Paths.get(s, id + FILE_EXTENSION))
                .filter(Files::exists)
                .filter(Files::isReadable)
                .findAny()
                .map(p -> new Path[] {p})
                .orElseThrow(ModuleNotFoundException::new);
        // @formatter:on
    }
}
