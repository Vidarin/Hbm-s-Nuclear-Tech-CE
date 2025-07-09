package com.hbm.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * No more boilerplate crap
 *   @author MrNorwood
 *   @parm String value(); // the ResourceLocation path, e.g. "tileentity_machine_press"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)

public @interface AutoRegisterTE {
    String value();

    public static String TE = "tileentity_";
    public static String TEM = "tileentity_machine_";
}
