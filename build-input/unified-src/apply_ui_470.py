from pathlib import Path
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'musor-drop')
screen = root / 'src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java'
text = screen.read_text(encoding='utf-8')


def replace_once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'UI 4.7 anchor {label!r} expected once, found {count}')
    text = text.replace(old, new, 1)


def replace_method(signature: str, replacement: str) -> None:
    global text
    start = text.find(signature)
    if start < 0:
        raise SystemExit(f'Method signature not found: {signature}')
    brace = text.find('{', start)
    if brace < 0:
        raise SystemExit(f'Method brace not found: {signature}')
    depth = 0
    end = None
    for i in range(brace, len(text)):
        ch = text[i]
        if ch == '{':
            depth += 1
        elif ch == '}':
            depth -= 1
            if depth == 0:
                end = i + 1
                break
    if end is None:
        raise SystemExit(f'Unbalanced method: {signature}')
    text = text[:start] + replacement.rstrip() + text[end:]


replace_once(
    '        targetSearch = addRenderableWidget(new EditBox(font, leftPos + 246, topPos + 103, 84, 14, Component.literal("Search target")));\n',
    '        targetSearch = addRenderableWidget(new EditBox(font, leftPos + 243, topPos + 108, 92, 14, Component.literal("Search target")));\n',
    'target search geometry')
replace_once(
    '        targetNext = addRenderableWidget(button(246, 120, 84, 16, ModLanguage.tr("ДРУГАЯ ЦЕЛЬ", "NEXT TARGET"),\n',
    '        targetNext = addRenderableWidget(button(243, 126, 92, 17, ModLanguage.tr("ДРУГАЯ ЦЕЛЬ", "NEXT TARGET"),\n',
    'target next geometry')
replace_once(
    '        upgradeButton = addRenderableWidget(button(124, 111, 100, 20, ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE"),\n',
    '        upgradeButton = addRenderableWidget(button(126, 123, 100, 21, ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE"),\n',
    'upgrade button geometry')
replace_once(
    '        refreshButton = addRenderableWidget(button(102, 183, 50, 18, ModLanguage.tr("ОБН.", "SYNC"),\n',
    '        refreshButton = addRenderableWidget(button(105, 190, 49, 17, ModLanguage.tr("ОБН.", "SYNC"),\n',
    'refresh geometry')
replace_once(
    '        sellOneButton = addRenderableWidget(button(18, 213, 62, 18, ModLanguage.tr("ПРОДАТЬ 1", "SELL 1"),\n',
    '        sellOneButton = addRenderableWidget(button(18, 220, 64, 18, ModLanguage.tr("ПРОДАТЬ 1", "SELL 1"),\n',
    'sell one geometry')
replace_once(
    '        sellStackButton = addRenderableWidget(button(84, 213, 68, 18, ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK"),\n',
    '        sellStackButton = addRenderableWidget(button(86, 220, 68, 18, ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK"),\n',
    'sell stack geometry')

replace_method('    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY)', r'''    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        MusorTheme.panel(g, leftPos, topPos, imageWidth, imageHeight, MusorTheme.BG, MusorTheme.BORDER);

        // Header: one quiet hierarchy shared by Upgrade, Cases and Support.
        g.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + 29, 0xFF100716);
        MusorTheme.separator(g, leftPos + 12, topPos + 29, leftPos + imageWidth - 12);
        drawBrandMark(g, leftPos + 10, topPos + 7);
        drawScaled(g, "MUSOR DROP", leftPos + 32, topPos + 10, 0.86F, MusorTheme.TEXT);
        drawScaled(g, "PREMIUM STATION", leftPos + 32, topPos + 20, 0.48F, MusorTheme.MUTED);

        MusorTheme.chip(g, leftPos + 243, topPos + 7, 61, 17, MusorTheme.GOLD, true);
        drawScaledTrimmed(g, "✦ " + compact(ClientState.balance), leftPos + 252, topPos + 12, 45, 0.69F, MusorTheme.GOLD);

        if (tab == MainTab.UPGRADE) renderUpgradePane(g);
        else renderCasesPane(g, mouseX, mouseY);

        drawInventoryFrame(g);
    }''')

replace_method('    private void renderUpgradePane(GuiGraphics g)', r'''    private void renderUpgradePane(GuiGraphics g) {
        int y = topPos + 58;
        int leftX = leftPos + 12;
        int centerX = leftPos + 121;
        int rightX = leftPos + 236;

        MusorTheme.panel(g, leftX, y, 102, 94, MusorTheme.PANEL, 0xFF4C2B60);
        MusorTheme.panel(g, centerX, y, 108, 94, 0xFF100817, MusorTheme.BORDER);
        MusorTheme.panel(g, rightX, y, 104, 94, MusorTheme.PANEL, 0xFF4C2B60);
        MusorTheme.cornerAccents(g, leftX, y, 102, 94, MusorTheme.ACCENT_DARK);
        MusorTheme.cornerAccents(g, centerX, y, 108, 94, MusorTheme.ACCENT);
        MusorTheme.cornerAccents(g, rightX, y, 104, 94, MusorTheme.ACCENT_DARK);

        centeredScaled(g, ModLanguage.tr("ПРЕДМЕТ", "STAKE"), leftX + 51, y + 8, 0.68F, MusorTheme.ACCENT);
        centeredScaled(g, ModLanguage.tr("ШАНС", "CHANCE"), centerX + 54, y + 8, 0.68F, MusorTheme.DIM);
        centeredScaled(g, ModLanguage.tr("ЦЕЛЬ", "TARGET"), rightX + 52, y + 8, 0.68F, MusorTheme.ACCENT);

        ItemStack input = menu.getInputStack();
        if (input.isEmpty()) {
            MusorTheme.inset(g, leftX + 9, y + 26, 28, 28);
            drawFallbackCube(g, leftX + 15, y + 31, MusorTheme.BORDER);
            drawScaledTrimmed(g, ModLanguage.tr("Положите предмет", "Place an item"), leftX + 43, y + 29, 51, 0.61F, MusorTheme.MUTED);
            drawScaledTrimmed(g, ModLanguage.tr("в слот ниже", "in the slot below"), leftX + 43, y + 41, 51, 0.55F, MusorTheme.MUTED);
            drawScaledTrimmed(g, ModLanguage.tr("Шанс появится после ставки", "Chance appears after stake"), leftX + 10, y + 69, 82, 0.53F, MusorTheme.MUTED);
        } else {
            MusorTheme.inset(g, leftX + 9, y + 26, 28, 28);
            renderSafeItemScaled(g, input, leftX + 14, y + 31, 1.15F);
            drawScaledTrimmed(g, safeStackName(input), leftX + 43, y + 28, 51, 0.63F, MusorTheme.TEXT);
            drawScaledTrimmed(g, ModLanguage.tr("Ценность: ", "Value: ") + format((long) safeValue(input)), leftX + 43, y + 41, 51, 0.53F, MusorTheme.DIM);
            drawScaledTrimmed(g, ModLanguage.tr("Готово к расчёту", "Ready to calculate"), leftX + 10, y + 69, 82, 0.54F, MusorTheme.SUCCESS);
        }

        Item target = ForgeRegistries.ITEMS.getValue(selectedTarget);
        double chance = estimateChance(input, target);
        centeredScaled(g, String.format(Locale.ROOT, "%.2f%%", chance * 100D), centerX + 54, y + 27,
            1.08F, chance > 0D ? chanceColor(chance) : MusorTheme.FAIL);
        centeredScaled(g, chance > 0D ? chanceLabel(chance) : ModLanguage.tr("НЕТ СТАВКИ", "NO STAKE"),
            centerX + 54, y + 44, 0.55F, chance > 0D ? chanceColor(chance) : MusorTheme.MUTED);

        int gaugeX = centerX + 10;
        int gaugeY = y + 57;
        int gaugeW = 88;
        MusorTheme.inset(g, gaugeX, gaugeY, gaugeW, 7);
        int fill = (int) Math.round((gaugeW - 4) * chance);
        if (fill > 0) {
            int color = chanceColor(chance);
            g.fill(gaugeX + 2, gaugeY + 2, gaugeX + 2 + fill, gaugeY + 5, color);
            if (fill > 7) g.fill(gaugeX + 4, gaugeY + 2, gaugeX + fill, gaugeY + 3, MusorTheme.brighten(color, 34));
        }
        centeredScaled(g, ModLanguage.tr("серверный расчёт", "server calculated"), centerX + 54, y + 70, 0.48F, MusorTheme.MUTED);

        MusorTheme.inset(g, rightX + 8, y + 24, 28, 28);
        renderSafeItemScaled(g, target == null ? ItemStack.EMPTY : new ItemStack(target), rightX + 13, y + 29, 1.15F);
        drawScaledTrimmed(g, safeItemName(target), rightX + 42, y + 27, 54, 0.62F, MusorTheme.TEXT);
        drawScaledTrimmed(g, shortNamespace(selectedTarget), rightX + 42, y + 40, 54, 0.52F, MusorTheme.MUTED);
        MusorTheme.inset(g, rightX + 6, topPos + 106, 94, 18);
        g.fill(rightX + 9, topPos + 109, rightX + 10, topPos + 121, 0xFF5B3471);

        // Directional flow: stake -> server chance -> target.
        g.fill(leftX + 103, y + 47, centerX - 2, y + 48, MusorTheme.BORDER_SOFT);
        g.fill(centerX + 109, y + 47, rightX - 2, y + 48, MusorTheme.BORDER_SOFT);
        g.fill(centerX - 4, y + 45, centerX - 2, y + 50, MusorTheme.ACCENT_DARK);
        g.fill(rightX - 4, y + 45, rightX - 2, y + 50, MusorTheme.ACCENT_DARK);

        MusorTheme.separator(g, leftPos + 12, topPos + 170, leftPos + imageWidth - 12);
    }''')

replace_method('    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY)', r'''    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY) {
        MusorTheme.inset(g, leftPos + 229, topPos + 32, 111, 20);
        g.fill(leftPos + 232, topPos + 35, leftPos + 233, topPos + 49, 0xFF5B3471);

        int start = page * 8;
        for (int i = 0; i < 8; i++) {
            int idx = start + i;
            if (idx >= filteredCases.size()) break;
            renderCaseCard(g, filteredCases.get(idx), i, mouseX, mouseY);
        }

        int pages = Math.max(1, (filteredCases.size() + 7) / 8);
        centeredScaled(g, (page + 1) + " / " + pages, leftPos + 163, topPos + 154, 0.54F, MusorTheme.MUTED);

        CaseDefinition c = selectedCase();
        CaseLayout.Rect selected = CaseLayout.selectedPanel();
        int selectedBorder = c == null ? MusorTheme.BORDER_SOFT : MusorTheme.brighten(c.accent(), 18);
        MusorTheme.panel(g, leftPos + selected.x(), topPos + selected.y(), selected.w(), selected.h(), 0xFF100817, selectedBorder);
        if (c != null) {
            CaseArt.render3d(g, c.category(), leftPos + selected.x() + 2, topPos + selected.y(), 0.80F);
            drawScaledTrimmed(g, c.name(ModLanguage.russian()), leftPos + selected.x() + 19, topPos + selected.y() + 5, 38, 0.56F, MusorTheme.TEXT);
            drawScaledTrimmed(g, compact(c.cost()) + "✦", leftPos + selected.x() + 59, topPos + selected.y() + 5, 22, 0.54F, MusorTheme.GOLD);
        }

        MusorTheme.separator(g, leftPos + 12, topPos + 171, leftPos + imageWidth - 12);
    }''')

replace_method('    private void renderCaseCard(GuiGraphics g, CaseDefinition c, int localIndex, int mouseX, int mouseY)', r'''    private void renderCaseCard(GuiGraphics g, CaseDefinition c, int localIndex, int mouseX, int mouseY) {
        CaseLayout.Rect r = CaseLayout.card(localIndex);
        int x = leftPos + r.x();
        int y = topPos + r.y();
        boolean hover = r.contains(mouseX - leftPos, mouseY - topPos);
        boolean selected = c.id().equals(selectedCaseId);

        int border = selected
            ? MusorTheme.brighten(c.accent(), 34)
            : hover ? MusorTheme.mix(MusorTheme.BORDER, c.accent(), 0.52F) : MusorTheme.BORDER_SOFT;
        int fill = selected
            ? MusorTheme.mix(0xFF100817, c.accent(), 0.12F)
            : hover ? MusorTheme.PANEL_HOVER : 0xFF100817;

        MusorTheme.panel(g, x, y, r.w(), r.h(), fill, border);
        if (selected) MusorTheme.cornerAccents(g, x, y, r.w(), r.h(), c.accent());

        float bob = hover ? (float) (Math.sin(System.currentTimeMillis() / 180D + localIndex * 0.7D) * 0.45D) : 0F;
        CaseArt.render3dAnimated(g, c.category(), x + 3, y + 10, hover ? 1.20F : 1.14F, hover ? -2.0F : 0F, bob);

        drawScaledTrimmed(g, c.name(ModLanguage.russian()), x + 30, y + 6, 44, 0.60F, MusorTheme.TEXT);
        drawScaledTrimmed(g, shortRarity(c.rarity()), x + 30, y + 18, 44, 0.55F, rarityColor(c.rarity()));
        drawScaledTrimmed(g, compact(c.cost()) + " ✦", x + 30, y + 29, 44, 0.57F, MusorTheme.GOLD);

        if (selected) {
            g.fill(x + 7, y + r.h() - 3, x + r.w() - 7, y + r.h() - 1, c.accent());
        }
    }''')

replace_method('    private void drawInventoryFrame(GuiGraphics g)', r'''    private void drawInventoryFrame(GuiGraphics g) {
        int invX = leftPos + StationLayout.INVENTORY_X;
        int invY = topPos + StationLayout.INVENTORY_Y;
        ItemStack input = menu.getInputStack();

        drawScaled(g, ModLanguage.tr("ИНВЕНТАРЬ", "INVENTORY"), invX, topPos + 173, 0.64F, MusorTheme.DIM);
        g.fill(invX + 63, topPos + 177, leftPos + imageWidth - 12, topPos + 178, MusorTheme.BORDER_FAINT);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                MusorTheme.slot(g, invX + col * 18, invY + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            MusorTheme.slot(g, invX + col * 18, topPos + StationLayout.HOTBAR_Y);
        }

        MusorTheme.panel(g, leftPos + 12, topPos + 176, 154, 72, 0xFF100817, 0xFF4D2B62);
        MusorTheme.cornerAccents(g, leftPos + 12, topPos + 176, 154, 72,
            tab == MainTab.CASES ? MusorTheme.GOLD_SOFT : MusorTheme.ACCENT_DARK);
        MusorTheme.slot(g, leftPos + StationLayout.INPUT_X, topPos + StationLayout.INPUT_Y);

        if (tab == MainTab.CASES) {
            drawScaled(g, ModLanguage.tr("ПРОДАЖА", "SELL"), leftPos + 18, topPos + 182, 0.68F, MusorTheme.GOLD);
            drawScaled(g, "72%", leftPos + 70, topPos + 182, 0.54F, MusorTheme.MUTED);
            long estimate = input.isEmpty() ? 0L : Math.max(0L, (long) Math.floor(safeValue(input) * 0.72D));
            drawScaledTrimmed(g,
                input.isEmpty() ? ModLanguage.tr("Предмет для продажи", "Item to sell") : safeStackName(input),
                leftPos + 57, topPos + 195, 40, 0.55F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.TEXT);
            drawScaledTrimmed(g,
                input.isEmpty() ? ModLanguage.tr("Shift+клик", "Shift-click") : "+" + format(estimate) + " ✦",
                leftPos + 57, topPos + 207, 40, 0.53F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.GOLD);
        } else {
            drawScaled(g, ModLanguage.tr("СЛОТ СТАВКИ", "STAKE SLOT"), leftPos + 18, topPos + 182, 0.65F, MusorTheme.ACCENT);
            drawScaledTrimmed(g,
                input.isEmpty() ? ModLanguage.tr("Перетащите предмет", "Drag an item") : safeStackName(input),
                leftPos + 57, topPos + 196, 92, 0.58F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.TEXT);
            drawScaledTrimmed(g,
                input.isEmpty() ? ModLanguage.tr("или Shift+клик", "or Shift-click") : ModLanguage.tr("готово к попытке", "ready for attempt"),
                leftPos + 57, topPos + 209, 92, 0.52F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.SUCCESS);
        }
    }''')

replace_method('    private void renderToast(GuiGraphics g)', r'''    private void renderToast(GuiGraphics g) {
        String s = trim(toast, 208);
        int w = Math.min(224, font.width(s) + 16);
        int x = leftPos + imageWidth / 2 - w / 2;
        int y = topPos + 5;
        MusorTheme.panel(g, x, y, w, 19, 0xF0251232, MusorTheme.ACCENT_2);
        g.fill(x + 5, y + 4, x + 6, y + 15, MusorTheme.ACCENT_GLOW);
        centeredScaledTrimmed(g, s, x + w / 2 + 2, y + 6, w - 18, 0.68F, MusorTheme.TEXT);
    }''')

screen.write_text(text, encoding='utf-8')
print('UI_470_PATCH_OK', screen)
