from pathlib import Path
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else "musor-drop")
screen = root / "src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java"
text = screen.read_text(encoding="utf-8")


def once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"UI 4.8 anchor {label!r} expected once, found {count}")
    text = text.replace(old, new, 1)


def replace_method(signature: str, replacement: str) -> None:
    global text
    start = text.find(signature)
    if start < 0:
        raise SystemExit(f"UI 4.8 method not found: {signature}")
    brace = text.find("{", start)
    if brace < 0:
        raise SystemExit(f"UI 4.8 method brace not found: {signature}")
    depth = 0
    end = None
    for i in range(brace, len(text)):
        ch = text[i]
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                break
    if end is None:
        raise SystemExit(f"UI 4.8 unbalanced method: {signature}")
    text = text[:start] + replacement.rstrip() + text[end:]


once(
    '''        upgradeTab = addRenderableWidget(button(12, 33, 68, 18, ModLanguage.tr("АПГРЕЙД", "UPGRADE"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.UPGRADE)));
        casesTab = addRenderableWidget(button(84, 33, 60, 18, ModLanguage.tr("КЕЙСЫ", "CASES"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.CASES)));
        supportTab = addRenderableWidget(button(148, 33, 80, 18, ModLanguage.tr("ПОДДЕРЖКА", "SUPPORT"),
            MusorButton.Style.TAB, this::showSupportPage));
''',
    '''        upgradeTab = addRenderableWidget(button(12, 32, 60, 17, ModLanguage.tr("АПГРЕЙД", "UPGRADE"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.UPGRADE)));
        casesTab = addRenderableWidget(button(76, 32, 54, 17, ModLanguage.tr("КЕЙСЫ", "CASES"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.CASES)));
        supportTab = addRenderableWidget(button(134, 32, 68, 17, ModLanguage.tr("ПОДДЕРЖКА", "SUPPORT"),
            MusorButton.Style.TAB, this::showSupportPage));
''',
    "top navigation geometry",
)
once('        languageToggle = addRenderableWidget(button(312, 7, 28, 17, ModLanguage.russian() ? "RU" : "EN",\n',
     '        languageToggle = addRenderableWidget(button(286, 7, 22, 15, ModLanguage.russian() ? "RU" : "EN",\n',
     "language toggle geometry")
once('        russianButton = addRenderableWidget(button(68, 124, 96, 28, "РУССКИЙ",\n',
     '        russianButton = addRenderableWidget(button(58, 122, 92, 22, "РУССКИЙ",\n',
     "russian button geometry")
once('        englishButton = addRenderableWidget(button(184, 124, 96, 28, "ENGLISH",\n',
     '        englishButton = addRenderableWidget(button(170, 122, 92, 22, "ENGLISH",\n',
     "english button geometry")

once('        targetSearch = addRenderableWidget(new EditBox(font, leftPos + 243, topPos + 108, 92, 14, Component.literal("Search target")));\n',
     '        targetSearch = addRenderableWidget(new EditBox(font, leftPos + 218, topPos + 103, 88, 14, Component.literal("Search target")));\n',
     "target search geometry")
once('        targetNext = addRenderableWidget(button(243, 126, 92, 17, ModLanguage.tr("ДРУГАЯ ЦЕЛЬ", "NEXT TARGET"),\n',
     '        targetNext = addRenderableWidget(button(218, 121, 88, 16, ModLanguage.tr("ДРУГАЯ ЦЕЛЬ", "NEXT TARGET"),\n',
     "target next geometry")
once('        upgradeButton = addRenderableWidget(button(126, 123, 100, 21, ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE"),\n',
     '        upgradeButton = addRenderableWidget(button(110, 119, 100, 18, ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE"),\n',
     "upgrade button geometry")

once('        caseSearch = addRenderableWidget(new EditBox(font, leftPos + 232, topPos + 34, 108, 16, Component.literal("Search cases")));\n',
     '        caseSearch = addRenderableWidget(new EditBox(font, leftPos + 204, topPos + 33, 104, 15, Component.literal("Search cases")));\n',
     "case search geometry")
once('        refreshButton = addRenderableWidget(button(105, 190, 49, 17, ModLanguage.tr("ОБН.", "SYNC"),\n',
     '        refreshButton = addRenderableWidget(button(88, 169, 36, 15, ModLanguage.tr("ОБН.", "SYNC"),\n',
     "sync geometry")
once('        sellOneButton = addRenderableWidget(button(18, 220, 64, 18, ModLanguage.tr("ПРОДАТЬ 1", "SELL 1"),\n',
     '        sellOneButton = addRenderableWidget(button(18, 210, 48, 17, ModLanguage.tr("ПРОДАТЬ 1", "SELL 1"),\n',
     "sell one geometry")
once('        sellStackButton = addRenderableWidget(button(86, 220, 68, 18, ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK"),\n',
     '        sellStackButton = addRenderableWidget(button(70, 210, 54, 17, ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK"),\n',
     "sell stack geometry")

once('        boostyButton = addRenderableWidget(button(20, 166, 88, 20, ModLanguage.tr("ОТКРЫТЬ", "OPEN"),\n',
     '        boostyButton = addRenderableWidget(button(241, 84, 66, 16, ModLanguage.tr("ОТКРЫТЬ", "OPEN"),\n',
     "boosty button geometry")
once('        donationAlertsButton = addRenderableWidget(button(132, 166, 88, 20, ModLanguage.tr("ОТКРЫТЬ", "OPEN"),\n',
     '        donationAlertsButton = addRenderableWidget(button(241, 116, 66, 16, ModLanguage.tr("ОТКРЫТЬ", "OPEN"),\n',
     "donationalerts button geometry")
once('        discordButton = addRenderableWidget(button(244, 166, 88, 20, ModLanguage.tr("ОТКРЫТЬ", "OPEN"),\n',
     '        discordButton = addRenderableWidget(button(241, 148, 66, 16, ModLanguage.tr("ОТКРЫТЬ", "OPEN"),\n',
     "discord button geometry")

replace_method("    private void updateWidgetVisibility()", r'''    private void updateWidgetVisibility() {
        boolean language = tab == MainTab.LANGUAGE;
        boolean normal = !language;
        boolean support = normal && supportPage;
        boolean upgrade = tab == MainTab.UPGRADE && !support;
        boolean cases = tab == MainTab.CASES && !support;

        russianButton.visible = language;
        englishButton.visible = language;

        upgradeTab.visible = normal;
        casesTab.visible = normal;
        supportTab.visible = normal;
        languageToggle.visible = normal;

        upgradeTab.setSelected(upgrade);
        casesTab.setSelected(cases);
        supportTab.setSelected(support);

        targetSearch.visible = upgrade;
        targetSearch.active = upgrade;
        targetNext.visible = upgrade;
        upgradeButton.visible = upgrade;

        caseSearch.visible = cases;
        caseSearch.active = cases;
        categoryButton.visible = cases;
        prevButton.visible = cases;
        nextButton.visible = cases;
        openCaseButton.visible = cases;
        refreshButton.visible = cases;
        sellOneButton.visible = cases;
        sellStackButton.visible = cases;

        boostyButton.visible = support;
        donationAlertsButton.visible = support;
        discordButton.visible = support;

        updateUpgradeButtonState();
        updateCaseButtons();
    }''')

replace_method("    private void rebuildCaseFilter()", r'''    private void rebuildCaseFilter() {
        String q = caseSearch == null ? "" : caseSearch.getValue().trim().toLowerCase(Locale.ROOT);
        lastCaseQuery = q;
        String category = categories.isEmpty() ? "ALL" : categories.get(Math.min(categoryIndex, categories.size() - 1));

        List<CaseDefinition> out = new ArrayList<>();
        for (CaseDefinition c : CaseCatalog.all()) {
            if (!"ALL".equals(category) && !category.equals(c.category())) continue;
            if (!q.isEmpty()) {
                String hay = (c.id() + " " + c.nameRu() + " " + c.nameEn() + " " + c.category()).toLowerCase(Locale.ROOT);
                if (!hay.contains(q)) continue;
            }
            out.add(c);
        }

        filteredCases = List.copyOf(out);
        int pages = Math.max(1, (filteredCases.size() + CaseLayout.PAGE_SIZE - 1) / CaseLayout.PAGE_SIZE);
        page = Math.max(0, Math.min(page, pages - 1));

        if (filteredCases.stream().noneMatch(c -> c.id().equals(selectedCaseId)) && !filteredCases.isEmpty()) {
            selectedCaseId = filteredCases.get(Math.min(filteredCases.size() - 1, page * CaseLayout.PAGE_SIZE)).id();
        }
        updateCaseButtons();
    }''')

replace_method("    private void changePage(int delta)", r'''    private void changePage(int delta) {
        int pages = Math.max(1, (filteredCases.size() + CaseLayout.PAGE_SIZE - 1) / CaseLayout.PAGE_SIZE);
        page = Math.max(0, Math.min(pages - 1, page + delta));
        if (!filteredCases.isEmpty()) {
            selectedCaseId = filteredCases.get(Math.min(filteredCases.size() - 1, page * CaseLayout.PAGE_SIZE)).id();
        }
        updateCaseButtons();
    }''')

replace_method("    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY)", r'''    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        MusorTheme.panel(g, leftPos, topPos, imageWidth, imageHeight, MusorTheme.BG, MusorTheme.BORDER_SOFT);

        g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + 27, MusorTheme.BG_SOFT);
        drawBrandMark(g, leftPos + 9, topPos + 6);
        drawScaled(g, "MUSOR DROP", leftPos + 29, topPos + 8, 0.78F, MusorTheme.TEXT);
        drawScaled(g, "PREMIUM STATION", leftPos + 29, topPos + 18, 0.42F, MusorTheme.MUTED);
        MusorTheme.separator(g, leftPos + 10, topPos + 28, leftPos + imageWidth - 10);

        MusorTheme.chip(g, leftPos + 224, topPos + 6, 56, 16, MusorTheme.GOLD, true);
        drawScaledTrimmed(g, "✦ " + compact(ClientState.balance), leftPos + 232, topPos + 11, 42, 0.63F, MusorTheme.GOLD);

        if (supportPage) renderSupportPane(g);
        else if (tab == MainTab.UPGRADE) renderUpgradePane(g);
        else renderCasesPane(g, mouseX, mouseY);

        if (!supportPage) drawInventoryFrame(g);
    }''')

replace_method("    private void renderUpgradePane(GuiGraphics g)", r'''    private void renderUpgradePane(GuiGraphics g) {
        int x = leftPos + 12;
        int y = topPos + 56;
        int w = imageWidth - 24;
        int h = 92;

        MusorTheme.softPanel(g, x, y, w, h);
        MusorTheme.glowLine(g, x + 12, y + 3, x + w - 12, MusorTheme.ACCENT_DARK);

        int leftEnd = leftPos + 106;
        int rightStart = leftPos + 214;
        g.fill(leftEnd, y + 9, leftEnd + 1, y + h - 9, MusorTheme.BORDER_FAINT);
        g.fill(rightStart, y + 9, rightStart + 1, y + h - 9, MusorTheme.BORDER_FAINT);

        centeredScaled(g, ModLanguage.tr("СТАВКА", "STAKE"), leftPos + 59, y + 8, 0.62F, MusorTheme.ACCENT);
        centeredScaled(g, ModLanguage.tr("ШАНС", "CHANCE"), leftPos + 160, y + 8, 0.62F, MusorTheme.DIM);
        centeredScaled(g, ModLanguage.tr("ЦЕЛЬ", "TARGET"), leftPos + 262, y + 8, 0.62F, MusorTheme.ACCENT);

        ItemStack input = menu.getInputStack();
        MusorTheme.inset(g, leftPos + 22, y + 25, 28, 28);
        if (input.isEmpty()) {
            drawFallbackCube(g, leftPos + 28, y + 30, MusorTheme.BORDER);
            drawScaledTrimmed(g, ModLanguage.tr("Положите предмет", "Place an item"), leftPos + 55, y + 28, 45, 0.55F, MusorTheme.MUTED);
            drawScaledTrimmed(g, ModLanguage.tr("в слот ниже", "in slot below"), leftPos + 55, y + 40, 45, 0.49F, MusorTheme.MUTED);
            drawScaledTrimmed(g, ModLanguage.tr("Ожидание ставки", "Waiting for stake"), leftPos + 22, y + 67, 76, 0.49F, MusorTheme.MUTED);
        } else {
            renderSafeItemScaled(g, input, leftPos + 27, y + 30, 1.12F);
            drawScaledTrimmed(g, safeStackName(input), leftPos + 55, y + 27, 45, 0.55F, MusorTheme.TEXT);
            drawScaledTrimmed(g, ModLanguage.tr("ценн. ", "value ") + format((long) safeValue(input)), leftPos + 55, y + 40, 45, 0.48F, MusorTheme.DIM);
            drawScaledTrimmed(g, ModLanguage.tr("Готово", "Ready"), leftPos + 22, y + 67, 76, 0.50F, MusorTheme.SUCCESS);
        }

        Item target = ForgeRegistries.ITEMS.getValue(selectedTarget);
        double chance = estimateChance(input, target);
        centeredScaled(g, String.format(Locale.ROOT, "%.2f%%", chance * 100D), leftPos + 160, y + 28, 0.96F, chance > 0D ? chanceColor(chance) : MusorTheme.FAIL);
        centeredScaled(g, chance > 0D ? chanceLabel(chance) : ModLanguage.tr("НЕТ СТАВКИ", "NO STAKE"), leftPos + 160, y + 43, 0.49F, chance > 0D ? chanceColor(chance) : MusorTheme.MUTED);

        int gaugeX = leftPos + 119;
        int gaugeY = y + 57;
        int gaugeW = 82;
        MusorTheme.inset(g, gaugeX, gaugeY, gaugeW, 6);
        int fill = (int) Math.round((gaugeW - 4) * chance);
        if (fill > 0) {
            int color = chanceColor(chance);
            g.fill(gaugeX + 2, gaugeY + 2, gaugeX + 2 + fill, gaugeY + 4, color);
        }
        centeredScaled(g, ModLanguage.tr("сервер решает результат", "server resolves result"), leftPos + 160, y + 69, 0.42F, MusorTheme.MUTED);

        MusorTheme.inset(g, leftPos + 220, y + 23, 28, 28);
        renderSafeItemScaled(g, target == null ? ItemStack.EMPTY : new ItemStack(target), leftPos + 225, y + 28, 1.12F);
        drawScaledTrimmed(g, safeItemName(target), leftPos + 252, y + 26, 51, 0.55F, MusorTheme.TEXT);
        drawScaledTrimmed(g, shortNamespace(selectedTarget), leftPos + 252, y + 39, 51, 0.45F, MusorTheme.MUTED);
    }''')

replace_method("    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY)", r'''    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY) {
        MusorTheme.inset(g, leftPos + 202, topPos + 31, 108, 19);
        g.fill(leftPos + 205, topPos + 34, leftPos + 206, topPos + 47, MusorTheme.ACCENT_DARK);

        int start = page * CaseLayout.PAGE_SIZE;
        for (int i = 0; i < CaseLayout.PAGE_SIZE; i++) {
            int idx = start + i;
            if (idx >= filteredCases.size()) break;
            renderCaseCard(g, filteredCases.get(idx), i, mouseX, mouseY);
        }

        int pages = Math.max(1, (filteredCases.size() + CaseLayout.PAGE_SIZE - 1) / CaseLayout.PAGE_SIZE);
        CaseDefinition selected = selectedCase();
        centeredScaled(g, (page + 1) + "/" + pages, leftPos + 190, topPos + 149, 0.50F, MusorTheme.MUTED);
        if (selected != null) {
            int sx = leftPos + 162;
            int sy = topPos + 145;
            g.fill(sx, sy, sx + 1, sy + 15, selected.accent());
            drawScaledTrimmed(g, compact(selected.cost()) + "✦", sx + 5, sy + 5, 48, 0.50F, MusorTheme.GOLD);
        }
        MusorTheme.separator(g, leftPos + 12, topPos + 160, leftPos + imageWidth - 12);
    }''')

replace_method("    private void renderCaseCard(GuiGraphics g, CaseDefinition c, int localIndex, int mouseX, int mouseY)", r'''    private void renderCaseCard(GuiGraphics g, CaseDefinition c, int localIndex, int mouseX, int mouseY) {
        CaseLayout.Rect r = CaseLayout.card(localIndex);
        int x = leftPos + r.x();
        int y = topPos + r.y();
        boolean hover = r.contains(mouseX - leftPos, mouseY - topPos);
        boolean selected = c.id().equals(selectedCaseId);

        int border = selected ? MusorTheme.brighten(c.accent(), 20) : hover ? MusorTheme.mix(MusorTheme.BORDER_SOFT, c.accent(), 0.48F) : MusorTheme.BORDER_FAINT;
        int fill = selected ? MusorTheme.mix(MusorTheme.PANEL, c.accent(), 0.10F) : hover ? MusorTheme.PANEL_HOVER : MusorTheme.PANEL;
        MusorTheme.panel(g, x, y, r.w(), r.h(), fill, border);
        if (selected) g.fill(x + 5, y + r.h() - 2, x + r.w() - 5, y + r.h() - 1, c.accent());

        float bob = hover ? (float) (Math.sin(System.currentTimeMillis() / 190D + localIndex * 0.65D) * 0.35D) : 0F;
        CaseArt.render3dAnimated(g, c.category(), x + 4, y + 9, hover ? 1.17F : 1.10F, hover ? -1.5F : 0F, bob);
        drawScaledTrimmed(g, c.name(ModLanguage.russian()), x + 31, y + 6, 57, 0.56F, MusorTheme.TEXT);
        drawScaledTrimmed(g, shortRarity(c.rarity()), x + 31, y + 18, 31, 0.50F, rarityColor(c.rarity()));
        drawScaledTrimmed(g, compact(c.cost()) + " ✦", x + 62, y + 18, 27, 0.49F, MusorTheme.GOLD);
        drawScaledTrimmed(g, c.category(), x + 31, y + 29, 57, 0.43F, MusorTheme.MUTED);
    }''')

replace_method("    private void drawInventoryFrame(GuiGraphics g)", r'''    private void drawInventoryFrame(GuiGraphics g) {
        int invX = leftPos + StationLayout.INVENTORY_X;
        int invY = topPos + StationLayout.INVENTORY_Y;
        ItemStack input = menu.getInputStack();

        MusorTheme.separator(g, leftPos + 12, topPos + 160, leftPos + imageWidth - 12);
        drawScaled(g, ModLanguage.tr("ИНВЕНТАРЬ", "INVENTORY"), invX, topPos + 156, 0.55F, MusorTheme.DIM);
        g.fill(invX + 55, topPos + 159, leftPos + imageWidth - 12, topPos + 160, MusorTheme.BORDER_FAINT);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) MusorTheme.slot(g, invX + col * 18, invY + row * 18);
        }
        for (int col = 0; col < 9; col++) MusorTheme.slot(g, invX + col * 18, topPos + StationLayout.HOTBAR_Y);

        MusorTheme.softPanel(g, leftPos + 12, topPos + 164, 122, 70);
        MusorTheme.slot(g, leftPos + StationLayout.INPUT_X, topPos + StationLayout.INPUT_Y);

        if (tab == MainTab.CASES) {
            drawScaled(g, ModLanguage.tr("ПРОДАЖА", "SELL"), leftPos + 18, topPos + 169, 0.57F, MusorTheme.GOLD);
            drawScaled(g, "72%", leftPos + 64, topPos + 169, 0.46F, MusorTheme.MUTED);
            long estimate = input.isEmpty() ? 0L : Math.max(0L, (long) Math.floor(safeValue(input) * 0.72D));
            drawScaledTrimmed(g, input.isEmpty() ? ModLanguage.tr("Предмет для продажи", "Item to sell") : safeStackName(input), leftPos + 54, topPos + 183, 72, 0.49F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.TEXT);
            drawScaledTrimmed(g, input.isEmpty() ? ModLanguage.tr("Shift+клик", "Shift-click") : "+" + format(estimate) + " ✦", leftPos + 54, topPos + 195, 72, 0.47F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.GOLD);
        } else {
            drawScaled(g, ModLanguage.tr("СЛОТ СТАВКИ", "STAKE SLOT"), leftPos + 18, topPos + 169, 0.55F, MusorTheme.ACCENT);
            drawScaledTrimmed(g, input.isEmpty() ? ModLanguage.tr("Перетащите предмет", "Drag an item") : safeStackName(input), leftPos + 54, topPos + 183, 72, 0.49F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.TEXT);
            drawScaledTrimmed(g, input.isEmpty() ? ModLanguage.tr("или Shift+клик", "or Shift-click") : ModLanguage.tr("готово к попытке", "ready for attempt"), leftPos + 54, topPos + 195, 72, 0.46F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.SUCCESS);
        }
    }''')

replace_method("    private void renderSupportPane(GuiGraphics g)", r'''    private void renderSupportPane(GuiGraphics g) {
        centeredScaled(g, ModLanguage.tr("ПОДДЕРЖКА MUSOR DROP", "SUPPORT MUSOR DROP"), leftPos + imageWidth / 2, topPos + 57, 0.66F, MusorTheme.TEXT);
        centeredScaled(g, ModLanguage.tr("Добровольно • без игровых преимуществ", "Voluntary • no gameplay advantages"), leftPos + imageWidth / 2, topPos + 69, 0.46F, MusorTheme.MUTED);

        renderSupportCard(g, leftPos + 12, topPos + 79, 296, 27, "BOOSTY", ModLanguage.tr("Поддержать разработку", "Support development"), "boosty.to/harekuto", 0xFFC77CFF);
        renderSupportCard(g, leftPos + 12, topPos + 111, 296, 27, "DONATION ALERTS", ModLanguage.tr("Разовый донат", "One-time donation"), "donationalerts.com", 0xFFFF7EC3);
        renderSupportCard(g, leftPos + 12, topPos + 143, 296, 27, "DISCORD", ModLanguage.tr("Новости и сообщество", "News & community"), "discord.gg/micro", 0xFF8FA0FF);

        MusorTheme.softPanel(g, leftPos + 12, topPos + 178, 296, 36);
        g.fill(leftPos + 18, topPos + 185, leftPos + 20, topPos + 207, MusorTheme.GOLD_SOFT);
        drawScaledTrimmed(g, ModLanguage.tr("Поддержка не влияет на шансы, награды или баланс.", "Support never changes odds, rewards, or balance."), leftPos + 27, topPos + 185, 270, 0.49F, MusorTheme.DIM);
        drawScaledTrimmed(g, ModLanguage.tr("Только проверенные HTTPS-ссылки. Никаких скрытых действий.", "Trusted HTTPS links only. No hidden actions."), leftPos + 27, topPos + 198, 270, 0.45F, MusorTheme.MUTED);
    }''')

replace_method("    private void renderSupportCard(GuiGraphics g, int x, int y, int w, int h,", r'''    private void renderSupportCard(GuiGraphics g, int x, int y, int w, int h,
                                   String title, String description, String url, int accent) {
        int border = MusorTheme.mix(MusorTheme.BORDER_SOFT, accent, 0.34F);
        MusorTheme.panel(g, x, y, w, h, MusorTheme.PANEL, border);
        g.fill(x + 8, y + 7, x + 18, y + 17, MusorTheme.darken(accent, 36));
        g.fill(x + 12, y + 4, x + 14, y + 20, accent);
        g.fill(x + 5, y + 11, x + 21, y + 13, accent);
        drawScaledTrimmed(g, title, x + 28, y + 6, 78, 0.55F, MusorTheme.TEXT);
        drawScaledTrimmed(g, description, x + 106, y + 6, 118, 0.47F, MusorTheme.DIM);
        drawScaledTrimmed(g, url, x + 28, y + 17, 192, 0.42F, MusorTheme.MUTED);
    }''')

replace_method("    private void renderLanguageGate(GuiGraphics g)", r'''    private void renderLanguageGate(GuiGraphics g) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        MusorTheme.panel(g, x, y, imageWidth, imageHeight, MusorTheme.BG, MusorTheme.BORDER_SOFT);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + 27, MusorTheme.BG_SOFT);
        MusorTheme.separator(g, x + 10, y + 28, x + imageWidth - 10);

        drawBrandMark(g, x + imageWidth / 2 - 61, y + 49);
        centeredScaled(g, "MUSOR DROP", x + imageWidth / 2 + 6, y + 50, 0.92F, MusorTheme.TEXT);
        centeredScaled(g, "PREMIUM STATION", x + imageWidth / 2, y + 68, 0.50F, MusorTheme.ACCENT);
        centeredScaled(g, "ВЫБЕРИТЕ ЯЗЫК / CHOOSE LANGUAGE", x + imageWidth / 2, y + 91, 0.58F, MusorTheme.DIM);
        MusorTheme.softPanel(g, x + 48, y + 108, 224, 49);
        centeredScaled(g, ModLanguage.tr("Язык можно сменить позже", "Language can be changed later"), x + imageWidth / 2, y + 149, 0.49F, MusorTheme.MUTED);
        centeredScaled(g, "Minecraft 1.20.1 • Forge 47.4.10", x + imageWidth / 2, y + 208, 0.48F, MusorTheme.MUTED);
    }''')

replace_method("    private void renderToast(GuiGraphics g)", r'''    private void renderToast(GuiGraphics g) {
        String s = trim(toast, 178);
        int w = Math.min(194, font.width(s) + 14);
        int x = leftPos + imageWidth / 2 - w / 2;
        int y = topPos + 5;
        MusorTheme.panel(g, x, y, w, 17, 0xF021132B, MusorTheme.ACCENT_2);
        centeredScaledTrimmed(g, s, x + w / 2, y + 5, w - 12, 0.60F, MusorTheme.TEXT);
    }''')

replace_method("    private void renderCaseTooltip(GuiGraphics g, int mouseX, int mouseY)", r'''    private void renderCaseTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (supportPage || tab != MainTab.CASES || caseAnimation.active()) return;
        int start = page * CaseLayout.PAGE_SIZE;
        for (int i = 0; i < CaseLayout.PAGE_SIZE; i++) {
            int idx = start + i;
            if (idx >= filteredCases.size()) break;
            CaseLayout.Rect r = CaseLayout.card(i);
            if (!r.contains(mouseX - leftPos, mouseY - topPos)) continue;
            CaseDefinition c = filteredCases.get(idx);
            List<Component> lines = List.of(
                Component.literal(c.name(ModLanguage.russian())).withStyle(style -> style.withColor(c.accent() & 0xFFFFFF)),
                Component.literal(c.rarity() + " • " + c.category()).withStyle(style -> style.withColor(MusorTheme.DIM & 0xFFFFFF)),
                Component.literal(ModLanguage.tr("Цена: ", "Cost: ") + format(c.cost()) + " ✦"),
                Component.literal(c.description(ModLanguage.russian())).withStyle(style -> style.withColor(MusorTheme.MUTED & 0xFFFFFF))
            );
            g.renderComponentTooltip(font, lines, mouseX, mouseY);
            return;
        }
    }''')

replace_method("    public boolean mouseClicked(double mouseX, double mouseY, int button)", r'''    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tab == MainTab.LANGUAGE) {
            if (button == 0) {
                if (russianButton.mouseClicked(mouseX, mouseY, button)) return true;
                if (englishButton.mouseClicked(mouseX, mouseY, button)) return true;
            }
            return true;
        }
        if (supportPage) {
            if (button == 0) {
                if (upgradeTab.mouseClicked(mouseX, mouseY, button)) return true;
                if (casesTab.mouseClicked(mouseX, mouseY, button)) return true;
                if (supportTab.mouseClicked(mouseX, mouseY, button)) return true;
                if (languageToggle.mouseClicked(mouseX, mouseY, button)) return true;
                if (boostyButton.mouseClicked(mouseX, mouseY, button)) return true;
                if (donationAlertsButton.mouseClicked(mouseX, mouseY, button)) return true;
                if (discordButton.mouseClicked(mouseX, mouseY, button)) return true;
            }
            return true;
        }
        if (tab == MainTab.CASES && button == 0 && !caseAnimation.active()) {
            int start = page * CaseLayout.PAGE_SIZE;
            for (int i = 0; i < CaseLayout.PAGE_SIZE; i++) {
                int idx = start + i;
                if (idx >= filteredCases.size()) break;
                CaseLayout.Rect r = CaseLayout.card(i);
                if (r.contains(mouseX - leftPos, mouseY - topPos)) {
                    selectedCaseId = filteredCases.get(idx).id();
                    playUi(SoundEvents.UI_BUTTON_CLICK, 1.03F);
                    updateCaseButtons();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }''')

replace_method("    public boolean mouseScrolled(double mouseX, double mouseY, double delta)", r'''    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int gridBottom = CaseLayout.GRID_Y + 2 * CaseLayout.CARD_H + CaseLayout.GAP_Y;
        if (!supportPage && tab == MainTab.CASES && !caseAnimation.active()
            && mouseY >= topPos + CaseLayout.GRID_Y && mouseY < topPos + gridBottom) {
            changePage(delta > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }''')

screen.write_text(text, encoding="utf-8")
print("UI_480_PATCH_OK", screen)
