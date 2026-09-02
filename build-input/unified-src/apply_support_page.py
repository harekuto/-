from pathlib import Path
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'musor-drop')
screen = root / 'src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java'
text = screen.read_text(encoding='utf-8')


def once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'Patch anchor {label!r} expected once, found {count}')
    text = text.replace(old, new, 1)


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
        casesTab = addRenderableWidget(button(84, 33, 62, 18, ModLanguage.tr("КЕЙСЫ", "CASES"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.CASES)));
        supportTab = addRenderableWidget(button(150, 33, 78, 18, ModLanguage.tr("ПОДДЕРЖКА", "SUPPORT"),
            MusorButton.Style.TAB, this::showSupportPage));
'''
once(old_tabs, new_tabs, 'top tabs')

once('        caseSearch = addRenderableWidget(new EditBox(font, leftPos + 188, topPos + 34, 152, 16, Component.literal("Search cases")));\n',
     '        caseSearch = addRenderableWidget(new EditBox(font, leftPos + 232, topPos + 34, 108, 16, Component.literal("Search cases")));\n', 'case search geometry')

support_buttons_anchor = '''        sellStackButton = addRenderableWidget(button(84, 213, 68, 18, ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK"),
            MusorButton.Style.COMPACT, () -> ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.SELL_STACK, ""))));

        rebuildCaseFilter();
'''
support_buttons_new = '''        sellStackButton = addRenderableWidget(button(84, 213, 68, 18, ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK"),
            MusorButton.Style.COMPACT, () -> ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.SELL_STACK, ""))));

        boostyButton = addRenderableWidget(button(18, 128, 92, 18, ModLanguage.tr("ОТКРЫТЬ BOOSTY", "OPEN BOOSTY"),
            MusorButton.Style.PRIMARY, () -> openSupportLink(SupportLinks.BOOSTY, "Boosty")));
        donationAlertsButton = addRenderableWidget(button(128, 128, 92, 18, ModLanguage.tr("DONATION ALERTS", "DONATION ALERTS"),
            MusorButton.Style.PRIMARY, () -> openSupportLink(SupportLinks.DONATION_ALERTS, "DonationAlerts")));
        discordButton = addRenderableWidget(button(238, 128, 92, 18, ModLanguage.tr("DISCORD", "DISCORD"),
            MusorButton.Style.PRIMARY, () -> openSupportLink(SupportLinks.DISCORD, "Discord")));

        rebuildCaseFilter();
'''
once(support_buttons_anchor, support_buttons_new, 'support buttons init')

switch_old = '''    private void switchTab(MainTab next) {
        if (next == MainTab.LANGUAGE || caseAnimation.active()) return;
        tab = next;
        updateWidgetVisibility();
        setFocused(null);
    }
'''
switch_new = '''    private void switchTab(MainTab next) {
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
'''
once(switch_old, switch_new, 'switch/support methods')

once('        casesTab.setMessage(Component.literal(ModLanguage.tr("КЕЙСЫ", "CASES")));\n',
     '        casesTab.setMessage(Component.literal(ModLanguage.tr("КЕЙСЫ", "CASES")));\n        supportTab.setMessage(Component.literal(ModLanguage.tr("ПОДДЕРЖКА", "SUPPORT")));\n', 'refresh support tab')

once('        sellStackButton.setMessage(Component.literal(ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK")));\n',
     '        sellStackButton.setMessage(Component.literal(ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK")));\n        boostyButton.setMessage(Component.literal(ModLanguage.tr("ОТКРЫТЬ BOOSTY", "OPEN BOOSTY")));\n        donationAlertsButton.setMessage(Component.literal("DONATION ALERTS"));\n        discordButton.setMessage(Component.literal("DISCORD"));\n', 'refresh support buttons')

visibility_old = '''        boolean language = tab == MainTab.LANGUAGE;
        boolean normal = !language;
        boolean upgrade = tab == MainTab.UPGRADE;
        boolean cases = tab == MainTab.CASES;
'''
visibility_new = '''        boolean language = tab == MainTab.LANGUAGE;
        boolean normal = !language;
        boolean support = normal && supportPage;
        boolean upgrade = tab == MainTab.UPGRADE && !support;
        boolean cases = tab == MainTab.CASES && !support;
'''
once(visibility_old, visibility_new, 'visibility booleans')

once('        upgradeTab.visible = normal;\n        casesTab.visible = normal;\n        languageToggle.visible = normal;\n\n        upgradeTab.setSelected(upgrade);\n        casesTab.setSelected(cases);\n',
     '        upgradeTab.visible = normal;\n        casesTab.visible = normal;\n        supportTab.visible = normal;\n        languageToggle.visible = normal;\n\n        upgradeTab.setSelected(upgrade);\n        casesTab.setSelected(cases);\n        supportTab.setSelected(support);\n', 'support tab visibility')

once('        sellStackButton.visible = cases;\n\n        updateCaseButtons();\n',
     '        sellStackButton.visible = cases;\n\n        boostyButton.visible = support;\n        donationAlertsButton.visible = support;\n        discordButton.visible = support;\n\n        updateCaseButtons();\n', 'support button visibility')

once('        caseSearch.active = tab == MainTab.CASES && ready;\n',
     '        caseSearch.active = tab == MainTab.CASES && !supportPage && ready;\n', 'case search support guard')

once('        if (tab == MainTab.UPGRADE) renderUpgradePane(g);\n        else renderCasesPane(g, mouseX, mouseY);\n',
     '        if (supportPage) renderSupportPane(g);\n        else if (tab == MainTab.UPGRADE) renderUpgradePane(g);\n        else renderCasesPane(g, mouseX, mouseY);\n', 'support render branch')

support_render = '''
    private void renderSupportPane(GuiGraphics g) {
        centeredScaled(g, ModLanguage.tr("ПОДДЕРЖКА MUSOR DROP", "SUPPORT MUSOR DROP"),
            leftPos + imageWidth / 2, topPos + 59, 0.82F, MusorTheme.TEXT);
        centeredScaled(g, ModLanguage.tr("Добровольно • без игровых преимуществ", "Voluntary • no gameplay advantages"),
            leftPos + imageWidth / 2, topPos + 72, 0.60F, MusorTheme.MUTED);

        renderSupportCard(g, leftPos + 12, topPos + 84, 104, 70, "BOOSTY",
            ModLanguage.tr("Поддержать разработку", "Support development"),
            "boosty.to/harekuto", 0xFFD17CFF);
        renderSupportCard(g, leftPos + 122, topPos + 84, 104, 70, "DONATION ALERTS",
            ModLanguage.tr("Разовый донат", "One-time donation"),
            "donationalerts.com", 0xFFFF83C8);
        renderSupportCard(g, leftPos + 232, topPos + 84, 104, 70, "DISCORD",
            ModLanguage.tr("Новости и сообщество", "News & community"),
            "discord.gg/micro", 0xFF9DA4FF);

        MusorTheme.separator(g, leftPos + 12, topPos + 165, leftPos + imageWidth - 12);
    }

    private void renderSupportCard(GuiGraphics g, int x, int y, int w, int h,
                                   String title, String description, String url, int accent) {
        MusorTheme.panel(g, x, y, w, h, 0xFF100817, MusorTheme.mix(MusorTheme.BORDER, accent, 0.46F));
        g.fill(x + 4, y + 4, x + 5, y + h - 4, accent);
        g.fill(x + 9, y + 9, x + 15, y + 15, MusorTheme.darken(accent, 28));
        g.fill(x + 11, y + 7, x + 13, y + 17, accent);
        g.fill(x + 7, y + 11, x + 17, y + 13, accent);
        drawScaledTrimmed(g, title, x + 22, y + 8, w - 29, 0.66F, MusorTheme.TEXT);
        drawScaledTrimmed(g, description, x + 10, y + 25, w - 20, 0.57F, MusorTheme.DIM);
        drawScaledTrimmed(g, url, x + 10, y + 39, w - 20, 0.54F, MusorTheme.MUTED);
    }

'''
once('    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY) {\n',
     support_render + '    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY) {\n', 'support render methods')

once('        if (tab == MainTab.CASES && button == 0 && !caseAnimation.active()) {\n',
     '        if (!supportPage && tab == MainTab.CASES && button == 0 && !caseAnimation.active()) {\n', 'case click support guard')

once('        if (tab == MainTab.CASES && !caseAnimation.active()\n',
     '        if (!supportPage && tab == MainTab.CASES && !caseAnimation.active()\n', 'case scroll support guard')

once('        tab = MainTab.UPGRADE;\n        playUi(SoundEvents.PLAYER_LEVELUP, 0.92F);\n',
     '        supportPage = false;\n        tab = MainTab.UPGRADE;\n        playUi(SoundEvents.PLAYER_LEVELUP, 0.92F);\n', 'language support reset')

once('        if (tab != MainTab.CASES || caseAnimation.active()) return;\n',
     '        if (supportPage || tab != MainTab.CASES || caseAnimation.active()) return;\n', 'tooltip support guard')

# On the support page the permanent lower station dock behaves like the stake dock,
# not like the selling controls from a previously selected Cases tab.
once('        if (tab == MainTab.CASES) {\n',
     '        if (tab == MainTab.CASES && !supportPage) {\n', 'inventory dock support mode')

screen.write_text(text, encoding='utf-8')
print('SUPPORT_PAGE_PATCH_OK', screen)
