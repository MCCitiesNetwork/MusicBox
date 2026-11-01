package com.minecraftcitiesnetwork.musicbox.utils;

import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import com.minecraftcitiesnetwork.musicbox.MusicBoxConfig;

@UtilityClass
public class YamlSupportUtils {
    public CustomClassLoaderConstructor createCustomClassLoaderConstructor() {
        return new CustomClassLoaderConstructor(MusicBoxConfig.class, MusicBoxConfig.class.getClassLoader(), new LoaderOptions());
    }
}
