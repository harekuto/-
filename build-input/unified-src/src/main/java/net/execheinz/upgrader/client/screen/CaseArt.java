package net.execheinz.upgrader.client.screen;

import java.util.HashMap;
import java.util.Map;
import net.execheinz.upgrader.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class CaseArt {
    private static final String[] CATEGORIES = {
        "SURVIVAL","MINING","FARMING","BUILDING","COMBAT","MAGIC","REDSTONE","EXPLORATION",
        "VILLAGE","OCEAN","NETHER","END","DEEP_DARK","STRUCTURES","MOBS","TOOLS","ARMOR",
        "WEAPONS","FOOD","DECOR","COLLECTIONS","PRO","MYTHIC","MASTER","MODDED","RANDOM",
        "BIOMES","TREASURE"
    };
    private static final Map<String, ItemStack> CACHE = new HashMap<>();

    public static int modelId(String category) {
        if (category == null) return 0;
        for (int i = 0; i < CATEGORIES.length; i++) if (CATEGORIES[i].equals(category)) return i;
        return 0;
    }

    public static ItemStack stack(String category) {
        String key = category == null ? "SURVIVAL" : category;
        return CACHE.computeIfAbsent(key, k -> {
            ItemStack s = new ItemStack(ModItems.CASE_DISPLAY.get());
            s.getOrCreateTag().putInt("CustomModelData", modelId(k));
            return s;
        });
    }

    public static void render3d(GuiGraphics g, String category, int x, int y, float scale) {
        g.pose().pushPose();
        g.pose().translate(x, y, 100.0F);
        g.pose().scale(scale, scale, 1.0F);
        g.renderItem(stack(category), 0, 0);
        g.pose().popPose();
    }

    private CaseArt() {}
}
