package com.voxline.voice.client.gui;

import com.voxline.voice.client.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class AvatarRenderer {
    private AvatarRenderer(){}
    public static void draw(GuiGraphics g,UUID id,int x,int y,int size){draw(g,id,x,y,size,ClientConfig.INSTANCE.showHatLayer,1f);}
    public static void draw(GuiGraphics g,UUID id,int x,int y,int size,boolean hat,float alpha){ResourceLocation skin=skin(id);g.setColor(1,1,1,Math.max(0,Math.min(1,alpha)));g.blit(skin,x,y,size,size,8,8,8,8,64,64);if(hat)g.blit(skin,x,y,size,size,40,8,8,8,64,64);g.setColor(1,1,1,1);}
    private static ResourceLocation skin(UUID id){Minecraft mc=Minecraft.getInstance();if(mc.level!=null){Player p=mc.level.getPlayerByUUID(id);if(p instanceof AbstractClientPlayer acp)return acp.getSkinTextureLocation();}if(mc.getConnection()!=null){PlayerInfo info=mc.getConnection().getPlayerInfo(id);if(info!=null)return info.getSkinLocation();}return DefaultPlayerSkin.getDefaultSkin(id);}
}
