from pathlib import Path
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'musor-drop')
screen = root / 'src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java'
text = screen.read_text(encoding='utf-8')


def once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'Support patch anchor {label!r} expected once, found {count}')
    text = text.replace(old, new, 1)


def replace_method(signature: str, replacement: str) -> None:
    global text
    start = text.find(signature)
    if start < 0:
        raise SystemExit(f'Support method not found: {signature}')
    brace = text.find('{', start)
    depth = 0
    end = None
    for i in range(brace, len(text)):
        if text[i] == '{':
            depth += 1
        elif text[i] == '}':
            depth -= 1
            if depth == 0:
                end = i + 1
                break
    if end is None:
        raise SystemExit(f'Unbalanced method: {signature}')
    text = text[:start] + replacement.rstrip() + text[end:]


once('import net.minecraft.client.Minecraft;\n', 'import net.minecraft.Util;\nimport net.minecraft.client.Minecraft;\n', 'Util import')
once('    private MusorButton upgradeTab;\n    private MusorButton casesTab;\n    private MusorButton languageToggle;\n',
     '    private MusorButton upgradeTab;\n    private MusorButton casesTab;\n    private MusorButton supportTab;\n    private MusorButton languageToggle;\n', 'tab fields')
once('    private MusorButton sellOneButton;\n    private MusorButton sellStackButton;\n',
     '    private MusorButton sellOneButton;\n    private MusorButton sellStackButton;\n\n    private MusorButton boostyButton;\n    private MusorButton donationAlertsButton;\n    private MusorButton discordButton;\n', 'support button fields')
once('    private String toast = "";\n    private long toastUntil;\n',
     '    private String toast = "";\n    private long toastUntil;\n    private boolean supportPage;\n', 'support state')

old_tabs = '''        upgradeTab = addRenderableWidget(button(12, 33, 82, 18, ModLanguage.tr("АПГРЕЙД", "UPGRADE"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.UPGRADE)));
        casesTab = addRenderableWidget(button(98, 33, 82, 18, ModLanguage.tr("КЕЙСЫ", "CASES"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.CASES)));
'''
new_tabs = '''        upgradeTab = addRenderableWidget(button(12, 33, 68, 18, ModLanguage.tr("АПГРЕЙД", "UPGRADE"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.UPGRADE)));
        casesTab = addRenderableWidget(button(84, 33, 60, 18, ModLanguage.tr("КЕЙСЫ", "CASES"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.CASES)));
        supportTab = addRenderableWidget(button(148, 33, 80, 18, ModLanguage.tr("ПОДДЕРЖКА", "SUPPORT"),
            MusorButton.Style.TAB, this::showSupportPage));
'''
once(old_tabs, new_tabs, 'top tabs')
once('        caseSearch = addRenderableWidget(new EditBox(font, leftPos + 188, topPos + 34, 152, 16, Component.literal("Search cases")));\n',
     '        caseSearch = addRenderableWidget(new EditBox(font, leftPos + 232, topPos + 34, 108, 16, Component.literal("Search cases")));\n', 'case search geometry')

support_buttons_anchor = '''        sellStackButton = addRenderableWidget(button(86, 220, 68, 18, ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK"),
            MusorButton.Style.COMPACT, () -> ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.SELL_STACK, ""))));

        rebuildCaseFilter();
'''
support_buttons_new = '''        sellStackButton = addRenderableWidget(button(86, 220, 68, 18, ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK"),
            MusorButton.Style.COMPACT, () -> ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.SELL_STACK, ""))));

        boostyButton = addRenderableWidget(button(20, 166, 88, 20, ModLanguage.tr("ОТКРЫТЬ", "OPEN"),
            MusorButton.Style.PRIMARY, () -> openSupportLink(SupportLinks.BOOSTY, "Boosty")));
        donationAlertsButton = addRenderableWidget(button(132, 166, 88, 20, ModLanguage.tr("ОТКРЫТЬ", "OPEN"),
            MusorButton.Style.PRIMARY, () -> openSupportLink(SupportLinks.DONATION_ALERTS, "DonationAlerts")));
        discordButton = addRenderableWidget(button(244, 166, 88, 20, ModLanguage.tr("ОТКРЫТЬ", "OPEN"),
            MusorButton.Style.PRIMARY, () -> openSupportLink(SupportLinks.DISCORD, "Discord")));

        rebuildCaseFilter();
'''
once(support_buttons_anchor, support_buttons_new, 'support buttons init')

once('''    private void switchTab(MainTab next) {
        if (next == MainTab.LANGUAGE || caseAnimation.active()) return;
        tab = next;
        updateWidgetVisibility();
        setFocused(null);
    }
''', '''    private void switchTab(MainTab next) {
        if (next == MainTab.LANGUAGE || caseAnimation.active()) return;
        supportPage = false;
        tab = next;
        updateWidgetVisibility();
        setFocused(null);
    }

    private void showSupportPage() {
        if (caseAnimation.active() || tab == MainTab.LANGUAGE) return;
        supportPage = true;
        updateWidgetVisibility();
        setFocused(null);
        playUi(SoundEvents.AMETHYST_BLOCK_CHIME, 1.08F);
    }

    private void openSupportLink(String url, String label) {
        if (!SupportLinks.isTrusted(url)) {
            toast = ModLanguage.tr("Ссылка заблокирована", "Link blocked");
            toastUntil = System.currentTimeMillis() + 2400L;
            playUi(SoundEvents.UI_BUTTON_CLICK, 0.64F);
            return;
        }
        try {
            Minecraft.getInstance().keyboardHandler.setClipboard(url);
            Util.getPlatform().openUri(url);
            toast = ModLanguage.tr("Открыто: ", "Opened: ") + label;
            toastUntil = System.currentTimeMillis() + 2200L;
            playUi(SoundEvents.AMETHYST_BLOCK_CHIME, 1.22F);
        } catch (RuntimeException ex) {
            Minecraft.getInstance().keyboardHandler.setClipboard(url);
            Upgrader.LOGGER.warn("Unable to open support link {}; copied it to clipboard instead", label, ex);
            toast = ModLanguage.tr("Ссылка скопирована", "Link copied");
            toastUntil = System.currentTimeMillis() + 2600L;
        }
    }
''', 'support switching')

once('        casesTab.setMessage(Component.literal(ModLanguage.tr("КЕЙСЫ", "CASES")));\n',
     '        casesTab.setMessage(Component.literal(ModLanguage.tr("КЕЙСЫ", "CASES")));\n        supportTab.setMessage(Component.literal(ModLanguage.tr("ПОДДЕРЖКА", "SUPPORT")));\n', 'refresh support tab')
once('        sellStackButton.setMessage(Component.literal(ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK")));\n',
     '        sellStackButton.setMessage(Component.literal(ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK")));\n        boostyButton.setMessage(Component.literal(ModLanguage.tr("ОТКРЫТЬ", "OPEN")));\n        donationAlertsButton.setMessage(Component.literal(ModLanguage.tr("ОТКРЫТЬ", "OPEN")));\n        discordButton.setMessage(Component.literal(ModLanguage.tr("ОТКРЫТЬ", "OPEN")));\n', 'refresh support buttons')

once('''        boolean language = tab == MainTab.LANGUAGE;
        boolean normal = !language;
        boolean upgrade = tab == MainTab.UPGRADE;
        boolean cases = tab == MainTab.CASES;
''', '''        boolean language = tab == MainTab.LANGUAGE;
        boolean normal = !language;
        boolean support = normal && supportPage;
        boolean upgrade = tab == MainTab.UPGRADE && !support;
        boolean cases = tab == MainTab.CASES && !support;
''', 'visibility booleans')
once('''        upgradeTab.visible = normal;
        casesTab.visible = normal;
        languageToggle.visible = normal;

        upgradeTab.setSelected(upgrade);
        casesTab.setSelected(cases);
''', '''        upgradeTab.visible = normal;
        casesTab.visible = normal;
        supportTab.visible = normal;
        languageToggle.visible = normal;

        upgradeTab.setSelected(upgrade);
        casesTab.setSelected(cases);
        supportTab.setSelected(support);
''', 'support tab visibility')
once('        sellStackButton.visible = cases;\n\n        updateCaseButtons();\n',
     '        sellStackButton.visible = cases;\n\n        boostyButton.visible = support;\n        donationAlertsButton.visible = support;\n        discordButton.visible = support;\n\n        updateCaseButtons();\n', 'support button visibility')
once('        caseSearch.active = tab == MainTab.CASES && ready;\n',
     '        caseSearch.active = tab == MainTab.CASES && !supportPage && ready;\n', 'case search support guard')

once('''        if (tab == MainTab.UPGRADE) renderUpgradePane(g);
        else renderCasesPane(g, mouseX, mouseY);

        drawInventoryFrame(g);
''', '''        if (supportPage) renderSupportPane(g);
        else if (tab == MainTab.UPGRADE) renderUpgradePane(g);
        else renderCasesPane(g, mouseX, mouseY);

        if (!supportPage) drawInventoryFrame(g);
''', 'support render branch')

support_render = r'''
    private void renderSupportPane(GuiGraphics g) {
        centeredScaled(g, ModLanguage.tr("ПОДДЕРЖКА MUSOR DROP", "SUPPORT MUSOR DROP"),
            leftPos + imageWidth / 2, topPos + 62, 0.78F, MusorTheme.TEXT);
        centeredScaled(g, ModLanguage.tr("Добровольно • без игровых преимуществ", "Voluntary • no gameplay advantages"),
            leftPos + imageWidth / 2, topPos + 76, 0.54F, MusorTheme.MUTED);

        renderSupportCard(g, leftPos + 12, topPos + 91, 104, 102, "BOOSTY",
            ModLanguage.tr("Поддержать разработку", "Support development"),
            "boosty.to/harekuto", 0xFFD17CFF);
        renderSupportCard(g, leftPos + 124, topPos + 91, 104, 102, "DONATION ALERTS",
            ModLanguage.tr("Разовый донат", "One-time donation"),
            "donationalerts.com", 0xFFFF83C8);
        renderSupportCard(g, leftPos + 236, topPos + 91, 104, 102, "DISCORD",
            ModLanguage.tr("Новости и сообщество", "News & community"),
            "discord.gg/micro", 0xFF9DA4FF);

        MusorTheme.panel(g, leftPos + 12, topPos + 204, 328, 34, 0xFF0E0714, MusorTheme.BORDER_SOFT);
        g.fill(leftPos + 18, topPos + 211, leftPos + 20, topPos + 231, MusorTheme.GOLD_SOFT);
        drawScaledTrimmed(g,
            ModLanguage.tr("Поддержка не меняет шансы, награды, баланс или доступ к функциям.",
                "Support never changes odds, rewards, balance, or feature access."),
            leftPos + 27, topPos + 212, 300, 0.54F, MusorTheme.DIM);
        drawScaledTrimmed(g,
            ModLanguage.tr("Ссылки открываются только через проверенный HTTPS allowlist.",
                "Links open only through a trusted HTTPS allowlist."),
            leftPos + 27, topPos + 225, 300, 0.50F, MusorTheme.MUTED);
    }

    private void renderSupportCard(GuiGraphics g, int x, int y, int w, int h,
                                   String title, String description, String url, int accent) {
        int border = MusorTheme.mix(MusorTheme.BORDER, accent, 0.48F);
        MusorTheme.panel(g, x, y, w, h, 0xFF100817, border);
        MusorTheme.cornerAccents(g, x, y, w, h, accent);
        g.fill(x + 8, y + 13, x + 16, y + 21, MusorTheme.darken(accent, 28));
        g.fill(x + 11, y + 9, x + 13, y + 25, accent);
        g.fill(x + 7, y + 14, x + 17, y + 20, accent);
        drawScaledTrimmed(g, title, x + 24, y + 11, w - 31, 0.62F, MusorTheme.TEXT);
        drawScaledTrimmed(g, description, x + 9, y + 37, w - 18, 0.54F, MusorTheme.DIM);
        drawScaledTrimmed(g, url, x + 9, y + 54, w - 18, 0.49F, MusorTheme.MUTED);
        MusorTheme.separator(g, x + 9, y + 70, x + w - 9);
    }

'''
once('    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY) {\n',
     support_render + '    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY) {\n', 'support render methods')

replace_method('    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick)', r'''    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        if (tab == MainTab.LANGUAGE) {
            renderLanguageGate(g);
            russianButton.render(g, mouseX, mouseY, partialTick);
            englishButton.render(g, mouseX, mouseY, partialTick);
            return;
        }

        // Support intentionally does not render container slots. It is a clean informational view,
        // not a recycled Upgrade/Cases layout with unrelated inventory controls underneath it.
        if (supportPage) {
            renderBg(g, partialTick, mouseX, mouseY);
            upgradeTab.render(g, mouseX, mouseY, partialTick);
            casesTab.render(g, mouseX, mouseY, partialTick);
            supportTab.render(g, mouseX, mouseY, partialTick);
            languageToggle.render(g, mouseX, mouseY, partialTick);
            boostyButton.render(g, mouseX, mouseY, partialTick);
            donationAlertsButton.render(g, mouseX, mouseY, partialTick);
            discordButton.render(g, mouseX, mouseY, partialTick);
            if (toastUntil > System.currentTimeMillis() && !toast.isBlank()) renderToast(g);
            return;
        }

        super.render(g, mouseX, mouseY, partialTick);
        if (tab == MainTab.CASES && caseAnimation.active()) renderCaseAnimation(g);
        if (toastUntil > System.currentTimeMillis() && !toast.isBlank()) renderToast(g);
        renderCaseTooltip(g, mouseX, mouseY);
    }''')

replace_method('    public boolean mouseClicked(double mouseX, double mouseY, int button)', r'''    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
            int start = page * 8;
            for (int i = 0; i < 8; i++) {
                int idx = start + i;
                if (idx >= filteredCases.size()) break;
                CaseLayout.Rect r = CaseLayout.card(i);
                if (r.contains(mouseX - leftPos, mouseY - topPos)) {
                    selectedCaseId = filteredCases.get(idx).id();
                    playUi(SoundEvents.UI_BUTTON_CLICK, 1.06F);
                    updateCaseButtons();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }''')

once('        if (tab == MainTab.CASES && !caseAnimation.active()\n',
     '        if (!supportPage && tab == MainTab.CASES && !caseAnimation.active()\n', 'case scroll support guard')
once('        tab = MainTab.UPGRADE;\n        playUi(SoundEvents.PLAYER_LEVELUP, 0.92F);\n',
     '        supportPage = false;\n        tab = MainTab.UPGRADE;\n        playUi(SoundEvents.PLAYER_LEVELUP, 0.92F);\n', 'language support reset')
once('        if (tab != MainTab.CASES || caseAnimation.active()) return;\n',
     '        if (supportPage || tab != MainTab.CASES || caseAnimation.active()) return;\n', 'tooltip support guard')

screen.write_text(text, encoding='utf-8')
print('SUPPORT_PAGE_470_PATCH_OK', screen)
