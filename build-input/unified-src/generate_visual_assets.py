from pathlib import Path
import json, struct, zlib

ROOT = Path('musor-drop/src/main/resources/assets/musordrop')
MODELS = ROOT / 'models/item/case_models'
TEX = ROOT / 'textures/item/cases'
ITEM = ROOT / 'textures/item'
MODELS.mkdir(parents=True, exist_ok=True)
TEX.mkdir(parents=True, exist_ok=True)
ITEM.mkdir(parents=True, exist_ok=True)

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


def rgba(v, factor=1.0, alpha=255):
    return (
        min(255, int(((v >> 16) & 255) * factor)),
        min(255, int(((v >> 8) & 255) * factor)),
        min(255, int((v & 255) * factor)),
        alpha,
    )


def make_station_icon():
    p = [(0, 0, 0, 0)] * (32 * 32)
    def rect(x0, y0, x1, y1, c):
        for y in range(y0, y1):
            for x in range(x0, x1):
                if 0 <= x < 32 and 0 <= y < 32:
                    p[y*32+x] = c
    deep = (73, 36, 101, 255)
    mid = (174, 94, 231, 255)
    hi = (220, 167, 255, 255)
    glow = (245, 224, 255, 255)
    rect(5, 4, 11, 28, deep)
    rect(7, 4, 13, 26, mid)
    rect(9, 5, 15, 10, hi)
    rect(10, 12, 22, 18, mid)
    rect(10, 18, 18, 24, hi)
    rect(13, 8, 19, 14, mid)
    rect(18, 14, 25, 20, deep)
    rect(20, 15, 25, 19, mid)
    rect(7, 5, 9, 23, glow)
    rect(11, 13, 18, 15, glow)
    return p


png(ITEM / 'station.png', 32, 32, make_station_icon())


def make_case_tex(v, idx):
    bg = (12, 7, 17, 255)
    p = [bg] * 256
    def put(x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            p[y*16+x] = c
    dark = rgba(v, .34)
    shade = rgba(v, .50)
    mid = rgba(v, .72)
    light = rgba(v, .96)
    hi = rgba(v, 1.20)
    glow = (241, 221, 255, 255)
    gold = (231, 190, 106, 255)

    for y in range(4, 14):
        for x in range(2, 14):
            checker = ((x + y + idx) & 3) == 0
            put(x, y, mid if x < 8 else shade)
            if checker and 3 < x < 13 and 5 < y < 13:
                put(x, y, rgba(v, .62))

    for y in range(1, 5):
        for x in range(3, 13):
            put(x, y, light if y < 3 else mid)
    for x in range(2, 14):
        put(x, 4, dark)
        put(x, 13, dark)
    for y in range(4, 14):
        put(2, y, dark)
        put(13, y, dark)
    for x in range(4, 12):
        put(x, 2, hi if ((x + idx) & 1) == 0 else light)

    for y in range(7, 12):
        put(7, y, gold)
        put(8, y, glow if y in (8, 9) else gold)
    for y in range(5, 13):
        put(4, y, dark)
        put(11, y, dark)

    rune = (idx * 0x45D9 + 0x1D3) & 0x1FF
    for bit in range(9):
        if (rune >> bit) & 1:
            put(5 + (bit % 3), 7 + (bit // 3), glow)

    gemx = 10 + (idx & 1)
    gemy = 6 + ((idx >> 1) & 1)
    put(gemx, gemy, hi)
    put(gemx + 1, gemy, glow)
    return p


for i, cat in enumerate(CATEGORIES):
    png(TEX / f'{cat.lower()}.png', 16, 16, make_case_tex(ACCENTS[i], i))

metal = []
for y in range(16):
    for x in range(16):
        if x in (0, 15) or y in (0, 15):
            metal.append((225, 169, 255, 255))
        elif ((x + y) & 3) == 0:
            metal.append((111, 71, 130, 255))
        else:
            metal.append((68 + y, 39 + y, 82 + y, 255))
png(TEX / 'metal.png', 16, 16, metal)


def all_faces(texture):
    return {k: {'texture': texture} for k in ['north', 'south', 'west', 'east', 'up', 'down']}


base = {
    'ambientocclusion': True,
    'textures': {'particle': '#crate'},
    'display': {
        'gui': {'rotation': [30, 225, 0], 'translation': [0, 0.4, 0], 'scale': [0.96, 0.96, 0.96]},
        'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.56, 0.56, 0.56]},
        'fixed': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [0.78, 0.78, 0.78]},
        'thirdperson_righthand': {'rotation': [72, 42, 0], 'translation': [0, 2.5, 0], 'scale': [0.42, 0.42, 0.42]},
        'firstperson_righthand': {'rotation': [0, 42, 0], 'translation': [0, 0, 0], 'scale': [0.52, 0.52, 0.52]},
    },
    'elements': [
        {'from': [2, 2, 2], 'to': [14, 10, 14], 'faces': all_faces('#crate')},
        {'from': [1, 10, 1], 'to': [15, 13, 15], 'faces': all_faces('#crate')},
        {'from': [2, 13, 2], 'to': [14, 14, 14], 'faces': all_faces('#metal')},
        {'from': [7, 6, 0.5], 'to': [9, 11, 2], 'faces': all_faces('#metal')},
        {'from': [3, 3, 1], 'to': [4, 10, 2.2], 'faces': all_faces('#metal')},
        {'from': [12, 3, 1], 'to': [13, 10, 2.2], 'faces': all_faces('#metal')},
        {'from': [1, 3, 3], 'to': [2.3, 10, 4], 'faces': all_faces('#metal')},
        {'from': [13.7, 3, 12], 'to': [15, 10, 13], 'faces': all_faces('#metal')},
        {'from': [4, 10.5, 0.5], 'to': [12, 12, 1.5], 'faces': all_faces('#metal')},
        {'from': [7, 13.8, 6.5], 'to': [9, 15, 9.5], 'faces': all_faces('#crate')},
    ],
}
(MODELS / 'base_case.json').write_text(json.dumps(base, separators=(',', ':')), encoding='utf-8')

for cat in CATEGORIES:
    child = {
        'parent': 'musordrop:item/case_models/base_case',
        'textures': {
            'crate': f'musordrop:item/cases/{cat.lower()}',
            'metal': 'musordrop:item/cases/metal',
        },
    }
    (MODELS / f'{cat.lower()}.json').write_text(json.dumps(child, separators=(',', ':')), encoding='utf-8')

item_model = {'parent': 'musordrop:item/case_models/survival', 'overrides': []}
for i, cat in enumerate(CATEGORIES[1:], start=1):
    item_model['overrides'].append({
        'predicate': {'custom_model_data': i},
        'model': f'musordrop:item/case_models/{cat.lower()}',
    })
(ROOT / 'models/item/case_display.json').write_text(json.dumps(item_model, separators=(',', ':')), encoding='utf-8')

station_model = {
    'parent': 'minecraft:item/generated',
    'textures': {'layer0': 'musordrop:item/station'},
    'display': {
        'gui': {'rotation': [0, 0, -4], 'translation': [0, 0, 0], 'scale': [0.92, 0.92, 0.92]},
    },
}
(ROOT / 'models/item/station.json').write_text(json.dumps(station_model, separators=(',', ':')), encoding='utf-8')

# Forge 47.4.x exposes the registered sound constants as Holder.Reference<SoundEvent>.
# Keep the source UI helper strongly typed to the actual API rather than unsafe casts.
screen_path = Path('musor-drop/src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java')
if screen_path.exists():
    src = screen_path.read_text(encoding='utf-8')
    src = src.replace(
        'private void playUi(SoundEvent event, float pitch)',
        'private void playUi(net.minecraft.core.Holder<SoundEvent> event, float pitch)'
    )
    screen_path.write_text(src, encoding='utf-8')

print('Generated', len(CATEGORIES), 'premium Musor Drop 3D case variants')
