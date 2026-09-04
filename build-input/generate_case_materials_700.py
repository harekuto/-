from pathlib import Path
import struct, zlib, binascii

OUT = Path('musor-drop/src/main/resources/assets/musordrop/textures/item/cases')
OUT.mkdir(parents=True, exist_ok=True)

PALETTE = {
    'survival': (180, 100, 255), 'mining': (95, 155, 255), 'farming': (112, 226, 101),
    'building': (255, 173, 60), 'combat': (255, 82, 82), 'magic': (176, 74, 255),
    'redstone': (245, 77, 142), 'exploration': (115, 139, 255), 'village': (91, 157, 255),
    'ocean': (67, 219, 240), 'nether': (255, 73, 55), 'end': (163, 88, 255),
    'deep_dark': (55, 197, 212), 'structures': (195, 201, 220), 'mobs': (125, 202, 104),
    'tools': (159, 195, 255), 'armor': (190, 151, 255), 'weapons': (255, 101, 74),
    'food': (255, 183, 70), 'decor': (220, 121, 222), 'collections': (207, 76, 255),
    'pro': (168, 54, 250), 'mythic': (247, 78, 181), 'master': (255, 204, 65),
    'modded': (79, 220, 243), 'random': (222, 186, 82), 'biomes': (104, 187, 88),
    'treasure': (255, 202, 58),
}

GLYPHS = {
 'survival':["..###..",".#...#.","#..#..#","#.#.#.#","#..#..#",".#...#.","..###.."],
 'mining':[".....##","....##.","...##..","..##...",".###...","##.#...","...#..."],
 'farming':["...#...","..###..",".#.#.#.","...#...","..###..",".#####.","...#..."],
 'building':[".#####.",".#...#.",".#.#.#.",".#...#.",".#####.","..###..","..###.."],
 'combat':["#.....#",".#...#.","..#.#..","...#...","..#.#..",".#...#.","#.....#"],
 'magic':["...#...","#..#..#",".#####.","..###..",".#####.","#..#..#","...#..."],
 'redstone':["...#...","..###..","...#...",".#####.","..#.#..",".#...#.","#.....#"],
 'exploration':["...#...","..###..",".#.#.#.","###.###",".#.#.#.","..###..","...#..."],
 'village':["...#...","..###..",".#####.","##...##","##.#.##","##...##","#######"],
 'ocean':["...#...","...#...",".#.#.#.",".#.#.#.","#######","..###..","...#..."],
 'nether':["...#...","..##...",".####..","##.###.","#######",".#####.","..###.."],
 'end':["..###..",".#...#.","#..#..#","#.#.#.#","#..#..#",".#...#.","..###.."],
 'deep_dark':["#.....#",".#...#.","..###..",".#####.","##.#.##",".#...#.","#.....#"],
 'structures':[".#####.","...#...","...#...",".#####.","...#...","...#...",".#####."],
 'mobs':[".#####.","#.#.#.#","#######","##...##","#######","#.#.#.#","#.....#"],
 'tools':["##.....",".##....","..##...","...##..","....##.",".....##",".....##"],
 'armor':["##...##","#######",".#####.",".#####.",".#####.","..###..","..###.."],
 'weapons':["...##..","...##..","..###..",".####..","..##...",".##....","##....."],
 'food':["..###..",".#####.","#######","#######",".#####.","..###..","...#..."],
 'decor':["...#...",".#.#.#.","..###..","#######","..###..",".#.#.#.","...#..."],
 'collections':["...#...",".#####.","..###..","#######","..###..",".#####.","...#..."],
 'pro':["#.....#","##...##",".##.##.","..###..",".##.##.","##...##","#.....#"],
 'mythic':["#..#..#",".#####.","..###..","#######","..###..",".#####.","#..#..#"],
 'master':["#.....#","##...##","#.#.#.#","#######",".#####.",".#####.","..###.."],
 'modded':[".#####.","##...##","#.#.#.#","##...##","#.#.#.#","##...##",".#####."],
 'random':[".#####.","#.....#",".....#.","...##..","...#...",".......","...#..."],
 'biomes':["...#...","..###..",".#####.","#######","..###..","..###..",".#####."],
 'treasure':[".#####.","#.....#","#######","#..#..#","#.....#","#######",".#####."],
}

def clamp(v): return max(0, min(255, int(v)))
def mix(a,b,t): return tuple(clamp(a[i]*(1-t)+b[i]*t) for i in range(3))

def chunk(tag, data):
    return struct.pack('>I',len(data))+tag+data+struct.pack('>I',binascii.crc32(tag+data)&0xffffffff)

def save_png(path, pixels, w=32, h=32):
    raw=b''.join(b'\x00'+bytes(sum((list(pixels[y*w+x]) for x in range(w)),[])) for y in range(h))
    out=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b'')
    path.write_bytes(out)

def put(px,x,y,c):
    if 0<=x<32 and 0<=y<32: px[y*32+x]=(*c,255)
def rect(px,x1,y1,x2,y2,c):
    for y in range(y1,y2+1):
        for x in range(x1,x2+1): put(px,x,y,c)
def line_h(px,x1,x2,y,c):
    for x in range(x1,x2+1): put(px,x,y,c)
def line_v(px,x,y1,y2,c):
    for yy in range(y1,y2+1): put(px,x,yy,c)

def case_texture(name, accent):
    light=mix(accent,(255,255,255),.45); hi=mix(accent,(255,255,255),.22)
    dark=mix(accent,(4,5,10),.72); deep=mix(accent,(2,3,7),.88)
    px=[]
    for y in range(32):
        for x in range(32):
            base=mix((5,7,12),deep,.42+.25*y/31)
            edge=abs(x-15.5)/15.5
            base=mix(base,(1,2,5),edge*.18)
            px.append((*base,255))
    line_h(px,2,29,2,light); line_h(px,3,28,3,hi); line_h(px,2,29,29,dark)
    line_v(px,2,3,28,light); line_v(px,29,3,28,dark)
    line_h(px,5,26,5,mix(hi,(255,255,255),.10)); line_h(px,5,26,26,dark)
    line_v(px,5,5,26,hi); line_v(px,26,5,26,dark)
    for x,y in [(1,1),(26,1),(1,26),(26,26)]:
        rect(px,x,y,x+4,y+4,dark); rect(px,x+1,y+1,x+3,y+3,hi); put(px,x+2,y+2,light)
    rect(px,12,0,19,2,hi); rect(px,13,1,18,3,light)
    line_h(px,8,23,27,hi)
    rect(px,8,8,23,23,mix((4,5,9),accent,.08))
    line_h(px,8,23,8,light); line_v(px,8,8,23,light); line_h(px,8,23,23,dark); line_v(px,23,8,23,dark)
    for yy in range(10,22,3): put(px,4,yy,hi); put(px,27,yy,accent)
    glyph=GLYPHS[name]
    ox,oy=9,9
    for gy,row in enumerate(glyph):
        for gx,ch in enumerate(row):
            if ch=='#':
                col=light if (gx+gy)%3 else (255,255,255)
                rect(px,ox+gx*2,oy+gy*2,ox+gx*2+1,oy+gy*2+1,col)
    for x,y in [(7,6),(24,6),(6,24),(25,24)]: put(px,x,y,light)
    save_png(OUT/f'{name}.png',px)

def metal_texture():
    px=[]
    for y in range(32):
        for x in range(32):
            base=mix((10,13,21),(48,56,78),.18+.42*y/31)
            base=mix(base,(2,4,8),abs(x-15.5)/15.5*.25)
            px.append((*base,255))
    light=(205,214,239); hi=(126,139,178); dark=(34,41,61); purple=(135,72,190)
    line_h(px,2,29,2,light); line_v(px,2,2,29,hi); line_h(px,2,29,29,dark); line_v(px,29,2,29,dark)
    line_h(px,5,26,5,hi); line_h(px,5,26,26,dark)
    for x,y in [(3,3),(26,3),(3,26),(26,26)]: rect(px,x,y,x+2,y+2,light)
    rect(px,13,9,18,22,dark); rect(px,14,10,17,21,purple); rect(px,15,11,16,20,light)
    line_h(px,8,23,15,hi)
    save_png(OUT/'metal.png',px)

for name,accent in PALETTE.items(): case_texture(name,accent)
metal_texture()
print('GENERATED_CASE_TEXTURES_700', len(PALETTE)+1)
