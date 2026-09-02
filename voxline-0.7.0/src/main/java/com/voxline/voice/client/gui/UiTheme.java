package com.voxline.voice.client.gui;

import com.voxline.voice.client.ClientConfig;

public record UiTheme(int bg,int panel,int panel2,int border,int text,int muted,int accent,int green,int purple,int red,int amber){
    public static UiTheme current(){return switch(ClientConfig.INSTANCE.theme){case "Amethyst"->new UiTheme(0xF20E0F15,0xF51A1922,0xF523222D,0xFF4C435E,0xFFF3F0F8,0xFF9E99AA,0xFFB56CFF,0xFF73E05C,0xFF8E64FF,0xFFFF6773,0xFFFFB24A);case "Frost"->new UiTheme(0xF20D1217,0xF5172027,0xF5202A33,0xFF3E5665,0xFFF3FAFF,0xFF94A8B5,0xFF41D8FF,0xFF79E38B,0xFF9A8EFF,0xFFFF6B79,0xFFFFBC55);case "Crimson"->new UiTheme(0xF2150E11,0xF521171B,0xF52A1F23,0xFF5A3B44,0xFFFFF1F3,0xFFAF959B,0xFFFF5F76,0xFF7EE36A,0xFFC86BFF,0xFFFF536B,0xFFFFA845);default->new UiTheme(0xF2101417,0xF5191E22,0xF521272C,0xFF39454C,0xFFF2F5F6,0xFF94A0A6,0xFF20D6D2,0xFF69D33A,0xFF9562F2,0xFFFF6773,0xFFFFB24A);};}
    public static int alpha(int c,float a){return (c&0x00FFFFFF)|(Math.max(0,Math.min(255,Math.round(a*255)))<<24);}
}
