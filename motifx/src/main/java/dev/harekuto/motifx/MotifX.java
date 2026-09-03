package dev.harekuto.motifx;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraftforge.fml.common.Mod;

@Mod(MotifX.MOD_ID)
public final class MotifX {
    public static final String MOD_ID = "motifx";
    public static final String VERSION = "0.2.0-dev.3";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MotifX() {
        LOGGER.info("MotifX {} initialized", VERSION);
    }
}
