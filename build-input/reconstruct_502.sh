#!/usr/bin/env bash
set -euxo pipefail

git fetch origin main --depth=1
rm -rf /tmp/base-input /tmp/musor-base.zip /tmp/musor-stability.zip /tmp/apply-ui-490.py /tmp/apply-assets-490.py /tmp/musor500.patch musor-drop
mkdir -p /tmp/base-input musor-drop
git archive --format=tar origin/main build-input | tar -xf - -C /tmp/base-input
cat /tmp/base-input/build-input/part-*.b64 | base64 -d > /tmp/musor-base.zip
unzip -tq /tmp/musor-base.zip
unzip -q /tmp/musor-base.zip -d musor-drop

MAIN='musor-drop/src/main/java/net/execheinz/upgrader/Upgrader.java'
ITEMS='musor-drop/src/main/java/net/execheinz/upgrader/registry/ModItems.java'
MENUS='musor-drop/src/main/java/net/execheinz/upgrader/registry/ModMenus.java'
ITEM='musor-drop/src/main/java/net/execheinz/upgrader/item/UpgraderItem.java'
SCREEN='musor-drop/src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java'
sed -i 's/public static final String MODID = "upgrader";/public static final String MODID = "musordrop";/' "$MAIN" || true
sed -i 's/ITEMS.register("upgrader",/ITEMS.register("station",/' "$ITEMS" || true
sed -i 's/MENUS.register("upgrader",/MENUS.register("station",/' "$MENUS" || true
sed -i 's/"menu.upgrader.title"/"menu.musordrop.title"/' "$ITEM" || true
sed -i 's/renderTooltip(g, mouseX, mouseY);/renderCaseTooltip(g, mouseX, mouseY);/' "$SCREEN" || true
sed -i 's/private void renderTooltip(GuiGraphics g, int mouseX, int mouseY)/private void renderCaseTooltip(GuiGraphics g, int mouseX, int mouseY)/' "$SCREEN" || true
[ ! -d musor-drop/src/main/resources/assets/upgrader ] || mv musor-drop/src/main/resources/assets/upgrader musor-drop/src/main/resources/assets/musordrop
[ ! -d musor-drop/src/main/resources/data/upgrader ] || mv musor-drop/src/main/resources/data/upgrader musor-drop/src/main/resources/data/musordrop
[ ! -f musor-drop/src/main/resources/assets/musordrop/models/item/upgrader.json ] || mv musor-drop/src/main/resources/assets/musordrop/models/item/upgrader.json musor-drop/src/main/resources/assets/musordrop/models/item/station.json
[ ! -f musor-drop/src/main/resources/data/musordrop/recipes/upgrader.json ] || mv musor-drop/src/main/resources/data/musordrop/recipes/upgrader.json musor-drop/src/main/resources/data/musordrop/recipes/station.json
find musor-drop/src/main/resources -type f -name '*.json' -print0 | xargs -0 -r sed -i -e 's/upgrader:/musordrop:/g' -e 's/musordrop:upgrader/musordrop:station/g'

cat build-input/stability/part-*.b64 | base64 -d > /tmp/musor-stability.zip
unzip -tq /tmp/musor-stability.zip
unzip -qo /tmp/musor-stability.zip -d musor-drop
cp -a build-input/unified-src/src/. musor-drop/src/
python3 build-input/unified-src/generate_visual_assets.py
python3 build-input/unified-src/apply_ui_470.py musor-drop
python3 build-input/unified-src/prepare_support_480.py musor-drop
python3 build-input/unified-src/apply_support_page.py musor-drop
python3 build-input/unified-src/apply_ui_480.py musor-drop
base64 -d build-input/unified-src/apply_ui_490.py.gz.b64 | gzip -dc > /tmp/apply-ui-490.py
base64 -d build-input/unified-src/apply_assets_490.py.gz.b64 | gzip -dc > /tmp/apply-assets-490.py
python3 /tmp/apply-ui-490.py musor-drop
python3 /tmp/apply-assets-490.py musor-drop

base64 -d build-input/musor-500-ui.patch.gz.b64 | gzip -dc > /tmp/musor500.patch
set +e
patch -p1 -d musor-drop < /tmp/musor500.patch
PATCH_RC=$?
set -e
if [ "$PATCH_RC" -ne 0 ]; then
  REJECTS="$(find musor-drop -name '*.rej' -print)"
  test "$REJECTS" = 'musor-drop/gradle.properties.rej'
fi
rm -f musor-drop/gradle.properties.rej
base64 -d build-input/station500.png.b64 > musor-drop/src/main/resources/assets/musordrop/textures/item/station.png

python3 build-input/apply_501_run9.py musor-drop
python3 build-input/apply_502_run10.py musor-drop
sed -i 's/^mod_version=.*/mod_version=5.0.2/' musor-drop/gradle.properties
sed -i 's|^mod_description=.*|mod_description=Musor Drop 5.0.2 professional Forge 1.20.1 station with lossless pending modded reward recovery, direction-locked networking, durable paid rewards, resize-safe GUI state, cached target discovery, Musor Shards, selling, upgrading and 224 cases.|' musor-drop/gradle.properties

grep -q '^minecraft_version=1.20.1$' musor-drop/gradle.properties
grep -q '^forge_version=47.4.10$' musor-drop/gradle.properties
grep -q '^mod_id=musordrop$' musor-drop/gradle.properties
grep -q '^mod_version=5.0.2$' musor-drop/gradle.properties
grep -q 'GUI_W = 360' musor-drop/src/main/java/net/execheinz/upgrader/menu/StationLayout.java
grep -q 'GUI_H = 252' musor-drop/src/main/java/net/execheinz/upgrader/menu/StationLayout.java
grep -q 'NetworkDirection.PLAY_TO_SERVER' musor-drop/src/main/java/net/execheinz/upgrader/network/ModNetwork.java
grep -q 'NetworkDirection.PLAY_TO_CLIENT' musor-drop/src/main/java/net/execheinz/upgrader/network/ModNetwork.java
grep -q 'CompoundTag original = list.getCompound(i).copy();' musor-drop/src/main/java/net/execheinz/upgrader/economy/PendingRewardService.java
grep -q 'preserved for a later retry' musor-drop/src/main/java/net/execheinz/upgrader/economy/PendingRewardService.java
grep -q 'preservedTargetQuery' "$SCREEN"
grep -q 'cachedTargetCatalog' "$SCREEN"
test -z "$(find musor-drop -name '*.rej' -print)"
echo RECONSTRUCT_502_OK
