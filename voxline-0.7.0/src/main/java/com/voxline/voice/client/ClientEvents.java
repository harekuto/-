package com.voxline.voice.client;

import com.voxline.voice.client.gui.GroupScreen;
import com.voxline.voice.client.gui.PlayerMixerScreen;
import com.voxline.voice.client.gui.VoiceSettingsScreen;
import com.voxline.voice.client.hud.VoiceHud;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientEvents {
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;Minecraft mc=Minecraft.getInstance();ClientVoiceController c=ClientVoiceController.get();if(mc.player!=null&&mc.getConnection()!=null)c.ensureStarted();else return;while(KeyBindings.MUTE.consumeClick())c.toggleMute();while(KeyBindings.DEAFEN.consumeClick())c.toggleDeafen();while(KeyBindings.SETTINGS.consumeClick())mc.setScreen(new VoiceSettingsScreen(mc.screen));while(KeyBindings.GROUPS.consumeClick())mc.setScreen(new GroupScreen(mc.screen));while(KeyBindings.MIXER.consumeClick())mc.setScreen(new PlayerMixerScreen(mc.screen));c.tick();}
    @SubscribeEvent public void render(RenderGuiEvent.Post e){VoiceHud.render(e.getGuiGraphics());}
    @SubscribeEvent public void logout(ClientPlayerNetworkEvent.LoggingOut e){ClientVoiceController.get().disconnect();}
}
