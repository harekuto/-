package com.voxline.voice.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class UiKit {
    public record Rect(int x,int y,int w,int h){public boolean hit(double mx,double my){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}}
    private UiKit(){}
    public static Font font(){return Minecraft.getInstance().font;}
    public static void panel(GuiGraphics g,int x,int y,int w,int h,int fill,int border){g.fill(x,y,x+w,y+h,0xFF0A0D0F);g.fill(x+1,y+1,x+w-1,y+h-1,border);g.fill(x+2,y+2,x+w-2,y+h-2,fill);g.fill(x+3,y+3,x+w-3,y+4,0x553F4C52);}
    public static void text(GuiGraphics g,String s,int x,int y,int c){g.drawString(font(),s,x,y,c,false);}public static void centered(GuiGraphics g,String s,int cx,int y,int c){g.drawString(font(),s,cx-font().width(s)/2,y,c,false);}
    public static void button(GuiGraphics g,Rect r,String label,boolean selected,boolean hover,UiTheme t){int b=selected?t.accent():hover?0xFF647078:t.border();panel(g,r.x,r.y,r.w,r.h,t.panel2(),b);if(selected)g.fill(r.x+3,r.y+3,r.x+r.w-3,r.y+5,t.accent());centered(g,label,r.x+r.w/2,r.y+(r.h-8)/2,selected?t.text():hover?0xFFFFFFFF:t.text());}
    public static void toggle(GuiGraphics g,Rect r,boolean on,UiTheme t){panel(g,r.x,r.y,r.w,r.h,on?0xFF276E1D:t.panel2(),on?t.green():t.border());int knob=on?r.x+r.w-15:r.x+4;g.fill(knob,r.y+4,knob+11,r.y+r.h-4,0xFFE9EEF0);}
    public static void slider(GuiGraphics g,Rect r,float v,int color,UiTheme t){v=Math.max(0,Math.min(1,v));g.fill(r.x,r.y+5,r.x+r.w,r.y+9,0xFF080B0D);g.fill(r.x+1,r.y+6,r.x+r.w-1,r.y+8,t.border());int p=r.x+Math.round((r.w-6)*v);g.fill(r.x+2,r.y+6,p+2,r.y+8,color);g.fill(p,r.y+1,p+6,r.y+13,0xFFE2E6E8);g.fill(p+1,r.y+2,p+5,r.y+12,0xFF707A80);}
    public static String fit(String s,int max){if(s==null)return "";if(font().width(s)<=max)return s;String e="…";int target=max-font().width(e);if(target<=0)return "";StringBuilder b=new StringBuilder();for(char c:s.toCharArray()){if(font().width(b.toString()+c)>target)break;b.append(c);}return b+e;}
}
