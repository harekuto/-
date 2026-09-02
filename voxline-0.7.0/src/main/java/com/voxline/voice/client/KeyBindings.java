package com.voxline.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    public static final String CATEGORY="key.categories.voxline";
    public static final KeyMapping PTT=new KeyMapping("key.voxline.ptt",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,CATEGORY);
    public static final KeyMapping GROUP_PTT=new KeyMapping("key.voxline.group_ptt",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,CATEGORY);
    public static final KeyMapping MUTE=new KeyMapping("key.voxline.mute",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_M,CATEGORY);
    public static final KeyMapping DEAFEN=new KeyMapping("key.voxline.deafen",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_B,CATEGORY);
    public static final KeyMapping SETTINGS=new KeyMapping("key.voxline.settings",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_SHIFT,CATEGORY);
    public static final KeyMapping GROUPS=new KeyMapping("key.voxline.groups",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_P,CATEGORY);
    public static final KeyMapping MIXER=new KeyMapping("key.voxline.mixer",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_H,CATEGORY);
    private KeyBindings(){}
    public static void register(RegisterKeyMappingsEvent e){e.register(PTT);e.register(GROUP_PTT);e.register(MUTE);e.register(DEAFEN);e.register(SETTINGS);e.register(GROUPS);e.register(MIXER);}
}
