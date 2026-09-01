from pathlib import Path
import base64, json, struct, zlib

ROOT = Path('musor-drop/src/main/resources/assets/musordrop')
MODELS = ROOT / 'models/item/case_models'
TEX = ROOT / 'textures/item/cases'
ITEM = ROOT / 'textures/item'
MODELS.mkdir(parents=True, exist_ok=True)
TEX.mkdir(parents=True, exist_ok=True)
ITEM.mkdir(parents=True, exist_ok=True)

STATION_B64 = 'iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAEnUlEQVR4nM2WW2wUVRzGf2dmd7u90O2iBArYcCkrRQtSQpbLA5CqERIJL/hgTIBGRQQTTXwwGDWRVMKTRBIReDExEQENiFBuFgvFtkNRWiNYBmoDtNCG2DJ7K9udmePDbmOp23anJdH/y5yZnPm+7389B/5jE2MFCPiC+1PLCgDd0HrHiumIvL6qNVJX1Ropm/Ls4Xderpwa8AWznWAoYyBfDRTYUipSyoE4IuALZhxZ12jJ39j81oauzrtPlcyekQ0QiYRnHD9+tHtlojwOKPiwdUOTj0zAAK+aATo77/rTbIvXuxpzU+twJrgZCQj4gmJlolw56T57uflqfSlAKAVvJZJOqqoqps18omBWccnjLlW1j/1wzAYij0RAhiZutrV/qOA5JZKx6gn4gqZuaA+G/SlT9MEp8Pv9KsDuvV/NAmj781pUVdW+Q4cONhcWTvn58i8Xz88tfableNX3PcOJyLgLdEOTqaIqA1bk5uYeEogYglShSWFZpqcvHp/U0X67PFNcR4Mo4Au6VqxanV3305mKzVveXt/bG3tsTsm88RJJUdHMbIDbt/SYoijW5k2vfgtsGSkFjudAJGSoSJR4PJ5v21IF/tVq0pYKUALsGQnPaRHKnN6EjW3l5Hjz3JZpqiEjZEkQBfleBeBSV7cpBHL3F1/OVYQo3bhxHbqhrRsK0HEE2u7c8iHJskzTI6VUECLNsBE2IKSUAsgL+IJlQ+E5jYA/8PSCaQ+aLy6dP3+BD4k4cOCbTolUy5cvygfYt2dXp6q67qxd+9JsQCxb9pxr0sTJqw8cBN3Qfh21gFQbit5YNAdQkckCFkLYUkp14N4sT5bu9XoLB39PZ5lPwjXlyvkf66Vl9llI2WPc704A2BLTsmxXZ5cpAWzbNAsnF1bt2LHtJhADLkB67zMWMNB8BX4DgakoiiWTve99eIdUr19v2QacA+LAjeHwnE5CkZuXMx4gGoltB4InqxpKAcLR5KEgbVsCXL3SGHK53PFPP6s8WrZw6c4zJw6364YWGozraBICcnpxkTG9uMgAtEz/lbY9pKNOUyDGT/ArdTWN6zdtfK8iFDYmRKNJjysr320TCOtyU4MB2KqiXgBIWN5PTp+sthWR/nh23AXd93pUQAmFjaKHlIHMG5f/O7AdaHq+eLmI9sXE6dYeO2FPsce5q9PiOuqCq7/prsK8iZ5rsvXN1zesmwRws+OBDTC3dFGe15v9ZIN2bsniFauunDh1OAGYXhXpVVsY6nY0ooB+z/vfaxrqaofaq6quvgGvtm5o9kj4w3ZBv+cAJ45US6DpyMFk1dsymfvzF2ruA+zctbUaOAvsBdANzRqJHDKIQG11gy+1rBlqj9vtiQItuqHtzoTUiYD8RQvLpzZeqt3/3dfVcwB6U6d7OPRXAmDnrq06cEY3tA+ckqcVEPAFl6SWOwDReKk23e03ackE3h8t+T8QA8jXvFixtr2jbdWm196fCZCVlacCXL/RFAb4fN/HHQD37t1pByzd0F4YLTk4vA8oimK5XZ4/5pUu3gK8MlZySNMFg1IA0F+EdannRwC6oXWNlfx/YX8DgqXu8WTA0vcAAAAASUVORK5CYII='
(ITEM / 'station.png').write_bytes(base64.b64decode(STATION_B64))

CATEGORIES = [
    'SURVIVAL','MINING','FARMING','BUILDING','COMBAT','MAGIC','REDSTONE','EXPLORATION',
    'VILLAGE','OCEAN','NETHER','END','DEEP_DARK','STRUCTURES','MOBS','TOOLS','ARMOR',
    'WEAPONS','FOOD','DECOR','COLLECTIONS','PRO','MYTHIC','MASTER','MODDED','RANDOM',
    'BIOMES','TREASURE'
]
ACCENTS = [
    0x8D63FF,0xA768E8,0x9C72FF,0xB15EFF,0x7E62D9,0xC47DFF,0xB45CBA,0xD895FF,
    0x6F9DFF,0x61C7D8,0xD06080,0x9B6CFF,0x4F8F9B,0xB997FF,0x7FB779,0x8AA2D9,
    0xB0A0FF,0xD18F62,0xE2A45F,0xD78AD8,0xC994FF,0xD46EFF,0xF08BCB,0xF0C66D,
    0x5EC8D8,0xE2B04C,0x8FBF79,0xE9B45C
]

def png(path, w, h, pixels):
    raw = b''.join(b'\x00' + bytes(sum((list(px) for px in pixels[y*w:(y+1)*w]), [])) for y in range(h))
    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff)
    out = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)) + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')
    path.write_bytes(out)

def color(v, f=1.0):
    return (min(255, int(((v >> 16) & 255) * f)), min(255, int(((v >> 8) & 255) * f)), min(255, int((v & 255) * f)), 255)

def make_tex(v, idx):
    p = [(24,10,32,255)] * 256
    def put(x,y,c):
        if 0 <= x < 16 and 0 <= y < 16: p[y*16+x] = c
    dark=color(v,.42); mid=color(v,.72); hi=color(v,1.14); glow=(240,221,255,255)
    for y in range(3,14):
        for x in range(2,14): put(x,y,mid if x < 8 else dark)
    for y in range(1,5):
        for x in range(3,13): put(x,y,hi)
    for x in range(2,14): put(x,3,dark); put(x,13,dark)
    for y in range(3,14): put(2,y,dark); put(13,y,dark)
    for y in range(6,12): put(7,y,glow); put(8,y,glow)
    # deterministic category rune
    for bit in range(9):
        if ((idx * 37 + 0x15) >> bit) & 1:
            put(10 + (bit % 3), 6 + (bit // 3), glow)
    return p

for i, cat in enumerate(CATEGORIES):
    png(TEX / f'{cat.lower()}.png', 16, 16, make_tex(ACCENTS[i], i))

metal = []
for y in range(16):
    for x in range(16):
        border = x in (0,15) or y in (0,15)
        metal.append((202,135,255,255) if border else (70 + y, 42 + y, 86 + y, 255))
png(TEX / 'metal.png', 16, 16, metal)

base = {
 'ambientocclusion': False,
 'textures': {'particle':'#crate'},
 'display': {
   'gui': {'rotation':[28,225,0],'translation':[0,0,0],'scale':[0.9,0.9,0.9]},
   'ground': {'rotation':[0,0,0],'translation':[0,3,0],'scale':[0.55,0.55,0.55]},
   'fixed': {'rotation':[0,180,0],'translation':[0,0,0],'scale':[0.75,0.75,0.75]},
   'thirdperson_righthand': {'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[0.4,0.4,0.4]},
   'firstperson_righthand': {'rotation':[0,45,0],'translation':[0,0,0],'scale':[0.5,0.5,0.5]}
 },
 'elements': [
   {'from':[2,2,2],'to':[14,10,14],'faces':{k:{'texture':'#crate'} for k in ['north','south','west','east','up','down']}},
   {'from':[1,10,1],'to':[15,13,15],'faces':{k:{'texture':'#metal'} for k in ['north','south','west','east','down']} | {'up':{'texture':'#crate'}}},
   {'from':[7,6,1],'to':[9,10,2],'faces':{k:{'texture':'#metal'} for k in ['north','south','west','east','up','down']}},
   {'from':[3,3,1],'to':[4,10,2],'faces':{'north':{'texture':'#metal'}}},
   {'from':[12,3,1],'to':[13,10,2],'faces':{'north':{'texture':'#metal'}}}
 ]
}
(MODELS / 'base_case.json').write_text(json.dumps(base, separators=(',',':')), encoding='utf-8')

for cat in CATEGORIES:
    child = {'parent':'musordrop:item/case_models/base_case','textures':{'crate':f'musordrop:item/cases/{cat.lower()}','metal':'musordrop:item/cases/metal'}}
    (MODELS / f'{cat.lower()}.json').write_text(json.dumps(child, separators=(',',':')), encoding='utf-8')

item_model = {'parent':'musordrop:item/case_models/survival','overrides':[]}
for i, cat in enumerate(CATEGORIES[1:], start=1):
    item_model['overrides'].append({'predicate':{'custom_model_data':i},'model':f'musordrop:item/case_models/{cat.lower()}'})
(ROOT / 'models/item/case_display.json').write_text(json.dumps(item_model, separators=(',',':')), encoding='utf-8')

station_model = {'parent':'minecraft:item/generated','textures':{'layer0':'musordrop:item/station'}}
(ROOT / 'models/item/station.json').write_text(json.dumps(station_model, separators=(',',':')), encoding='utf-8')

print('Generated', len(CATEGORIES), 'Musor Drop 3D case model variants')
