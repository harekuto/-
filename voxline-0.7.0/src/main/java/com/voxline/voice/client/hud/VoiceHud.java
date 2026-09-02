package com.voxline.voice.client.hud;

import com.voxline.voice.client.*;
import com.voxline.voice.client.gui.AvatarRenderer;
import com.voxline.voice.client.gui.UiKit;
import com.voxline.voice.client.gui.UiTheme;
import com.voxline.voice.network.VoiceChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class VoiceHud {
    private VoiceHud(){}
    public static void render(GuiGraphics g){Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||mc.screen!=null)return;ClientConfig c=ClientConfig.INSTANCE;ClientVoiceController ctl=ClientVoiceController.get();UiTheme t=UiTheme.current();int sw=mc.getWindow().getGuiScaledWidth(),sh=mc.getWindow().getGuiScaledHeight();List<ClientVoiceController.SpeakingInfo> active=ctl.activeSpeakers();int rows=Math.min(c.maxHudSpeakers,active.size());int w=Math.min(c.hudWidth,Math.max(130,sw-8));int h=Math.max(24,c.hudRowHeight);int block=rows*(h+3);int x=4+Math.round((Math.max(4,sw-w-8)-4)*(c.hudAnchorX/100f));int y=4+Math.round((Math.max(4,sh-Math.max(block,26)-8)-4)*(c.hudAnchorY/100f));
        int statusY=y;if(ctl.deafened()||ctl.muted()||!ctl.policy().voiceEnabled()){int color=!ctl.policy().voiceEnabled()?t.amber():ctl.deafened()?t.purple():t.red();String label=!ctl.policy().voiceEnabled()?"VOICE DISABLED":ctl.deafened()?"DEAFENED":"MIC MUTED";int pw=Math.min(126,w);UiKit.panel(g,x+w-pw,statusY,pw,22,UiTheme.alpha(t.panel(),.96f),color);UiKit.text(g,label,x+w-pw+8,statusY+7,color);statusY+=25;y=statusY;}
        int shown=0;for(ClientVoiceController.SpeakingInfo s:active){if(shown++>=c.maxHudSpeakers)break;int border=s.channel()==VoiceChannel.GROUP?t.purple():t.green();if(c.showBackground)UiKit.panel(g,x,y,w,h,UiTheme.alpha(t.panel(),c.uiOpacity),border);if(c.showSpeakingGlow)g.fill(x,y,x+3,y+h,UiTheme.alpha(border,.9f));int tx=x+8;if(c.showPlayerHead){int size=Math.max(14,Math.min(h-6,Math.round(18*c.headScale)));AvatarRenderer.draw(g,s.id(),x+7,y+(h-size)/2,size,c.showHatLayer,c.headOpacity);tx=x+13+size;}if(c.showStatusDot){g.fill(tx,y+9,tx+5,y+14,border);tx+=10;}if(c.showNameplate)UiKit.text(g,UiKit.fit(c.streamerMode?"Player":s.name(),Math.max(25,w-95-(tx-x))),tx,y+6,t.text());if(c.showChannelBadge){String badge=s.channel()==VoiceChannel.GROUP?"GROUP":"NEAR";UiKit.text(g,badge,x+w-42,y+6,s.channel()==VoiceChannel.GROUP?t.purple():t.accent());}if(c.showDistance&&s.channel()!=VoiceChannel.GROUP&&!c.streamerMode)UiKit.text(g,s.distance()<0?"":String.format("%.0fm",s.distance()),x+w-31,y+h-11,t.muted());if(c.showWaveform){float v=Math.max(0,Math.min(1,(s.levelDb()+60f)/50f));int bx=x+w-76,by=y+h-8;for(int i=0;i<6;i++){int bh=3+(i%3)*2;g.fill(bx+i*6,by-bh,bx+i*6+4,by,i<Math.max(1,Math.round(v*6))?border:0xFF3B4348);}}y+=h+3;}
        if(ctl.groupState().inGroup()&&active.isEmpty()){int gw=88,gy=Math.max(4,sh-34);UiKit.panel(g,sw-gw-6,gy,gw,24,UiTheme.alpha(t.panel(),.9f),t.purple());UiKit.text(g,"GROUP READY",sw-gw+2,gy+8,t.purple());}
    }
}
