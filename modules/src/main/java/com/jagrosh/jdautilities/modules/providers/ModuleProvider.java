package com.jagrosh.jdautilities.modules.providers;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public interface ModuleProvider
{
    @Nonnull
    String FILE_EXTENSION = ".jar";

    @Nonnull
    String getId();

    @Nonnull
    Path[] resolve(@Nonnull String id, @Nonnull String... locations) throws ModuleNotFoundException;
}
