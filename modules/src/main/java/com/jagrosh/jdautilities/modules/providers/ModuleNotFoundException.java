package com.jagrosh.jdautilities.modules.providers;

public class ModuleNotFoundException extends Exception
{
    public ModuleNotFoundException() {}

    public ModuleNotFoundException(String message)
    {
        super(message);
    }

    public ModuleNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModuleNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
