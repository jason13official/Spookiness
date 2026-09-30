package io.github.jason13official.spookiness;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(Spookiness.MODID)
public class Spookiness {
    public static final String MODID = "spookiness";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Spookiness(IEventBus modEventBus, ModContainer modContainer) {
    }
}
