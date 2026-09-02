package net.execheinz.upgrader.client.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.execheinz.upgrader.Upgrader;
import net.execheinz.upgrader.cases.CaseCatalog;
import net.execheinz.upgrader.cases.CaseDefinition;
import net.execheinz.upgrader.client.ClientState;
import net.execheinz.upgrader.economy.ItemPolicyService;
import net.execheinz.upgrader.economy.ItemValueService;
import net.execheinz.upgrader.menu.StationLayout;
import net.execheinz.upgrader.menu.UpgraderMenu;
import net.execheinz.upgrader.network.ModNetwork;
import net.execheinz.upgrader.network.packet.C2SCaseActionPacket;
import net.execheinz.upgrader.network.packet.C2SUpgradePacket;
import net.execheinz.upgrader.upgrade.UpgradeOdds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/**
 * Single-screen Musor Drop UI. The layout is intentionally compact and remains fully visible
 * at high Minecraft GUI scales while keeping every transactional action server-authoritative.
 */
public final class UpgraderScreen extends AbstractContainerScreen<UpgraderMenu> {
    private MainTab tab;

    private MusorButton upgradeTab;
    private MusorButton casesTab;
    private MusorButton languageToggle;
    private MusorButton russianButton;
    private MusorButton englishButton;

    private MusorButton targetNext;
    private MusorButton upgradeButton;

    private MusorButton categoryButton;
    private MusorButton prevButton;
    private MusorButton nextButton;
    private MusorButton openCaseButton;
    private MusorButton refreshButton;
    private MusorButton sellOneButton;
    private MusorButton sellStackButton;

    private EditBox targetSearch;
    private EditBox caseSearch;

    private final List<ResourceLocation> targetCatalog = new ArrayList<>();
    private int targetCursor;
    private ResourceLocation selectedTarget = new ResourceLocation("minecraft", "diamond");

    private List<CaseDefinition> filteredCases = List.of();
    private final List<String> categories = new ArrayList<>();
    private int categoryIndex;
    private int page;
    private String selectedCaseId = "first_day";
    private String lastCaseQuery = "";

    private final CaseAnimationState caseAnimation = new CaseAnimationState();

    private long seenCaseSerial;
    private long seenUpgradeSerial;
    private long seenSellSerial;
    private long seenErrorSerial;

    private String toast = "";
    private long toastUntil;

    private int lastSpinSoundStep = -1;
    private boolean revealSoundPlayed;

    public UpgraderScreen(UpgraderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = StationLayout.GUI_W;
        this.imageHeight = StationLayout.GUI_H;
        this.inventoryLabelX = StationLayout.INVENTORY_X;
        this.inventoryLabelY = StationLayout.INVENTORY_Y - 12;
        this.tab = ModLanguage.selected() ? MainTab.UPGRADE : MainTab.LANGUAGE;

        this.seenCaseSerial = ClientState.caseSerial;
        this.seenUpgradeSerial = ClientState.upgradeSerial;
        this.seenSellSerial = ClientState.sellSerial;
        this.seenErrorSerial = ClientState.errorSerial;
    }

    @Override
    protected void init() {
        super.init();
        buildTargetCatalog();
        buildCategories();

        upgradeTab = addRenderableWidget(button(12, 33, 82, 18, ModLanguage.tr("АПГРЕЙД", "UPGRADE"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.UPGRADE)));
        casesTab = addRenderableWidget(button(98, 33, 82, 18, ModLanguage.tr("КЕЙСЫ", "CASES"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.CASES)));
        languageToggle = addRenderableWidget(button(312, 7, 28, 17, ModLanguage.russian() ? "RU" : "EN",
            MusorButton.Style.COMPACT, () -> {
                ModLanguage.choose(!ModLanguage.russian());
                refreshTexts();
            }));

        russianButton = addRenderableWidget(button(68, 124, 96, 28, "РУССКИЙ",
            MusorButton.Style.PRIMARY, () -> chooseLanguage(true)));
        englishButton = addRenderableWidget(button(184, 124, 96, 28, "ENGLISH",
            MusorButton.Style.PRIMARY, () -> chooseLanguage(false)));

        targetSearch = addRenderableWidget(new EditBox(font, leftPos + 246, topPos + 103, 84, 14, Component.literal("Search target")));
        targetSearch.setMaxLength(48);
        targetSearch.setBordered(false);
        targetSearch.setHint(Component.literal(ModLanguage.tr("Поиск...", "Search...")));

        targetNext = addRenderableWidget(button(246, 120, 84, 16, ModLanguage.tr("ДРУГАЯ ЦЕЛЬ", "NEXT TARGET"),
            MusorButton.Style.COMPACT, this::nextTarget));

        upgradeButton = addRenderableWidget(button(124, 111, 100, 20, ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE"),
            MusorButton.Style.PRIMARY, () -> {
                ModNetwork.sendToServer(new C2SUpgradePacket(selectedTarget.toString()));
                toast = ModLanguage.tr("Сервер проверяет попытку...", "Server is resolving...");
                toastUntil = System.currentTimeMillis() + 1400L;
            }));

        caseSearch = addRenderableWidget(new EditBox(font, leftPos + 188, topPos + 34, 152, 16, Component.literal("Search cases")));
        caseSearch.setMaxLength(48);
        caseSearch.setBordered(false);
        caseSearch.setHint(Component.literal(ModLanguage.tr("Поиск кейса...", "Search cases...")));
        caseSearch.setResponder(v -> rebuildCaseFilter());

        CaseLayout.Rect prev = CaseLayout.prevButton();
        CaseLayout.Rect cat = CaseLayout.categoryButton();
        CaseLayout.Rect next = CaseLayout.nextButton();
        CaseLayout.Rect open = CaseLayout.openButton();

        prevButton = addRenderableWidget(button(prev.x(), prev.y(), prev.w(), prev.h(), "<",
            MusorButton.Style.COMPACT, () -> changePage(-1)));
        categoryButton = addRenderableWidget(button(cat.x(), cat.y(), cat.w(), cat.h(), "ALL",
            MusorButton.Style.SECONDARY, this::cycleCategory));
        nextButton = addRenderableWidget(button(next.x(), next.y(), next.w(), next.h(), ">",
            MusorButton.Style.COMPACT, () -> changePage(1)));
        openCaseButton = addRenderableWidget(button(open.x(), open.y(), open.w(), open.h(), ModLanguage.tr("ОТКРЫТЬ", "OPEN"),
            MusorButton.Style.PRIMARY, this::openSelectedCase));

        refreshButton = addRenderableWidget(button(102, 183, 50, 18, ModLanguage.tr("ОБН.", "SYNC"),
            MusorButton.Style.COMPACT, () -> ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.SYNC, ""))));
        sellOneButton = addRenderableWidget(button(18, 213, 62, 18, ModLanguage.tr("ПРОДАТЬ 1", "SELL 1"),
            MusorButton.Style.COMPACT, () -> ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.SELL_ONE, ""))));
        sellStackButton = addRenderableWidget(button(84, 213, 68, 18, ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK"),
            MusorButton.Style.COMPACT, () -> ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.SELL_STACK, ""))));

        rebuildCaseFilter();
        updateWidgetVisibility();
    }

    private MusorButton button(int relX, int relY, int w, int h, String label, MusorButton.Style style, Runnable action) {
        return new MusorButton(leftPos + relX, topPos + relY, w, h, Component.literal(label), style, action);
    }

    private void switchTab(MainTab next) {
        if (next == MainTab.LANGUAGE || caseAnimation.active()) return;
        tab = next;
        updateWidgetVisibility();
        setFocused(null);
    }

    private void refreshTexts() {
        if (languageToggle == null) return;
        languageToggle.setMessage(Component.literal(ModLanguage.russian() ? "RU" : "EN"));
        upgradeTab.setMessage(Component.literal(ModLanguage.tr("АПГРЕЙД", "UPGRADE")));
        casesTab.setMessage(Component.literal(ModLanguage.tr("КЕЙСЫ", "CASES")));
        targetNext.setMessage(Component.literal(ModLanguage.tr("ДРУГАЯ ЦЕЛЬ", "NEXT TARGET")));
        upgradeButton.setMessage(Component.literal(ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE")));
        refreshButton.setMessage(Component.literal(ModLanguage.tr("ОБН.", "SYNC")));
        sellOneButton.setMessage(Component.literal(ModLanguage.tr("ПРОДАТЬ 1", "SELL 1")));
        sellStackButton.setMessage(Component.literal(ModLanguage.tr("ВЕСЬ СТЕК", "SELL STACK")));
        targetSearch.setHint(Component.literal(ModLanguage.tr("Поиск...", "Search...")));
        caseSearch.setHint(Component.literal(ModLanguage.tr("Поиск кейса...", "Search cases...")));
        rebuildCaseFilter();
        updateWidgetVisibility();
    }

    private void updateWidgetVisibility() {
        boolean language = tab == MainTab.LANGUAGE;
        boolean normal = !language;
        boolean upgrade = tab == MainTab.UPGRADE;
        boolean cases = tab == MainTab.CASES;

        russianButton.visible = language;
        englishButton.visible = language;

        upgradeTab.visible = normal;
        casesTab.visible = normal;
        languageToggle.visible = normal;

        upgradeTab.setSelected(upgrade);
        casesTab.setSelected(cases);

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

        updateCaseButtons();
    }

    private void buildTargetCatalog() {
        targetCatalog.clear();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            try {
                if (!ItemPolicyService.isUpgradeTargetEligible(item)) continue;
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if (id != null) targetCatalog.add(id);
            } catch (RuntimeException ex) {
                Upgrader.LOGGER.debug("Skipping invalid third-party target item", ex);
            }
        }
        targetCatalog.sort(Comparator.comparing(ResourceLocation::toString));
        int diamond = targetCatalog.indexOf(new ResourceLocation("minecraft", "diamond"));
        targetCursor = Math.max(0, diamond);
        if (!targetCatalog.isEmpty()) selectedTarget = targetCatalog.get(targetCursor);
    }

    private void nextTarget() {
        if (targetCatalog.isEmpty()) return;
        String q = targetSearch.getValue().trim().toLowerCase(Locale.ROOT);
        for (int step = 1; step <= targetCatalog.size(); step++) {
            int idx = (targetCursor + step) % targetCatalog.size();
            ResourceLocation id = targetCatalog.get(idx);
            Item item = ForgeRegistries.ITEMS.getValue(id);
            String name = safeItemName(item).toLowerCase(Locale.ROOT);
            if (q.isEmpty() || id.toString().toLowerCase(Locale.ROOT).contains(q) || name.contains(q)) {
                targetCursor = idx;
                selectedTarget = id;
                playUi(SoundEvents.UI_BUTTON_CLICK, 1.08F);
                return;
            }
        }
    }

    private void buildCategories() {
        categories.clear();
        categories.add("ALL");
        Set<String> seen = new LinkedHashSet<>();
        for (CaseDefinition c : CaseCatalog.all()) seen.add(c.category());
        categories.addAll(seen);
        if (categoryIndex >= categories.size()) categoryIndex = 0;
    }

    private void cycleCategory() {
        if (categories.isEmpty()) return;
        categoryIndex = (categoryIndex + 1) % categories.size();
        categoryButton.setMessage(Component.literal(categories.get(categoryIndex)));
        page = 0;
        rebuildCaseFilter();
    }

    private void rebuildCaseFilter() {
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
        int pages = Math.max(1, (filteredCases.size() + 7) / 8);
        page = Math.max(0, Math.min(page, pages - 1));

        if (filteredCases.stream().noneMatch(c -> c.id().equals(selectedCaseId)) && !filteredCases.isEmpty()) {
            selectedCaseId = filteredCases.get(Math.min(filteredCases.size() - 1, page * 8)).id();
        }
        updateCaseButtons();
    }

    private void updateCaseButtons() {
        if (prevButton == null) return;
        int pages = Math.max(1, (filteredCases.size() + 7) / 8);
        boolean ready = !caseAnimation.active();

        prevButton.active = page > 0 && ready;
        nextButton.active = page + 1 < pages && ready;
        categoryButton.active = ready;
        caseSearch.active = tab == MainTab.CASES && ready;

        CaseDefinition selected = selectedCase();
        openCaseButton.active = selected != null && ready && ClientState.balance >= selected.cost();
        if (selected == null) {
            openCaseButton.setMessage(Component.literal(ModLanguage.tr("ОТКРЫТЬ", "OPEN")));
        } else {
            String label = ModLanguage.tr("ОТКРЫТЬ · ", "OPEN · ") + compact(selected.cost()) + "✦";
            openCaseButton.setMessage(Component.literal(label));
        }

        refreshButton.active = ready;
        sellOneButton.active = ready;
        sellStackButton.active = ready;
    }

    private void changePage(int delta) {
        int pages = Math.max(1, (filteredCases.size() + 7) / 8);
        page = Math.max(0, Math.min(pages - 1, page + delta));
        if (!filteredCases.isEmpty()) {
            selectedCaseId = filteredCases.get(Math.min(filteredCases.size() - 1, page * 8)).id();
        }
        updateCaseButtons();
    }

    private CaseDefinition selectedCase() {
        for (CaseDefinition c : filteredCases) {
            if (c.id().equals(selectedCaseId)) return c;
        }
        return null;
    }

    private void openSelectedCase() {
        CaseDefinition c = selectedCase();
        if (c == null || caseAnimation.active()) return;
        ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.OPEN, c.id()));
        toast = ModLanguage.tr("Открытие подтверждается сервером...", "Server is confirming opening...");
        toastUntil = System.currentTimeMillis() + 1400L;
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (caseSearch != null && !caseSearch.getValue().trim().toLowerCase(Locale.ROOT).equals(lastCaseQuery)) {
            rebuildCaseFilter();
        }

        if (ClientState.caseSerial != seenCaseSerial) {
            seenCaseSerial = ClientState.caseSerial;
            caseAnimation.start(ClientState.caseId, ClientState.rewardId, ClientState.rewardCount);
            selectedCaseId = ClientState.caseId;
            lastSpinSoundStep = -1;
            revealSoundPlayed = false;
            playUi(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.72F);
            updateCaseButtons();
        }

        if (caseAnimation.active()) {
            double p = caseAnimation.progress();
            int soundStep = Math.min(16, (int) Math.floor(p * 17D));
            if (soundStep != lastSpinSoundStep) {
                lastSpinSoundStep = soundStep;
                playUi(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.72F + soundStep * 0.025F);
            }
            if (p >= 0.93D && !revealSoundPlayed) {
                revealSoundPlayed = true;
                playUi(SoundEvents.PLAYER_LEVELUP, 1.12F);
            }
        } else {
            lastSpinSoundStep = -1;
        }

        if (ClientState.upgradeSerial != seenUpgradeSerial) {
            seenUpgradeSerial = ClientState.upgradeSerial;
            toast = ClientState.upgradeSuccess
                ? ModLanguage.tr("УСПЕХ • ", "SUCCESS • ") + shortId(ClientState.upgradeReward)
                : ModLanguage.tr("НЕУДАЧА • предмет потерян", "FAILED • item lost");
            toastUntil = System.currentTimeMillis() + 2800L;
            playUi(ClientState.upgradeSuccess ? SoundEvents.PLAYER_LEVELUP : SoundEvents.UI_BUTTON_CLICK,
                ClientState.upgradeSuccess ? 1.08F : 0.72F);
        }

        if (ClientState.sellSerial != seenSellSerial) {
            seenSellSerial = ClientState.sellSerial;
            toast = ModLanguage.tr("ПРОДАНО • +", "SOLD • +") + format(ClientState.sellDelta) + " ✦";
            toastUntil = System.currentTimeMillis() + 2200L;
            playUi(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.18F);
        }

        if (ClientState.errorSerial != seenErrorSerial) {
            seenErrorSerial = ClientState.errorSerial;
            toast = localizeError(ClientState.error);
            toastUntil = System.currentTimeMillis() + 2600L;
            playUi(SoundEvents.UI_BUTTON_CLICK, 0.66F);
        }

        updateCaseButtons();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        if (tab == MainTab.LANGUAGE) {
            renderLanguageGate(g);
            russianButton.render(g, mouseX, mouseY, partialTick);
            englishButton.render(g, mouseX, mouseY, partialTick);
            return;
        }

        super.render(g, mouseX, mouseY, partialTick);

        if (tab == MainTab.CASES && caseAnimation.active()) {
            renderCaseAnimation(g);
        }

        if (toastUntil > System.currentTimeMillis() && !toast.isBlank()) {
            renderToast(g);
        }

        renderCaseTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        MusorTheme.panel(g, leftPos, topPos, imageWidth, imageHeight, MusorTheme.BG, MusorTheme.BORDER);

        // Header: restrained, pixel-native hierarchy instead of a full-width heavy bar.
        g.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + 28, 0xFF100817);
        MusorTheme.separator(g, leftPos + 12, topPos + 28, leftPos + imageWidth - 12);
        drawBrandMark(g, leftPos + 10, topPos + 7);
        drawScaled(g, "MUSOR DROP", leftPos + 31, topPos + 10, 0.88F, MusorTheme.TEXT);

        if (ClientState.balance > 0L || tab == MainTab.CASES) {
            MusorTheme.chip(g, leftPos + 235, topPos + 7, 70, 17, MusorTheme.GOLD, true);
            drawScaledTrimmed(g, "✦ " + compact(ClientState.balance), leftPos + 244, topPos + 12, 54, 0.72F, MusorTheme.GOLD);
        }

        if (tab == MainTab.UPGRADE) renderUpgradePane(g);
        else renderCasesPane(g, mouseX, mouseY);

        drawInventoryFrame(g);
    }

    private void drawBrandMark(GuiGraphics g, int x, int y) {
        g.fill(x, y + 1, x + 4, y + 15, MusorTheme.ACCENT_DARK);
        g.fill(x + 2, y, x + 6, y + 13, MusorTheme.ACCENT);
        g.fill(x + 5, y + 3, x + 10, y + 7, MusorTheme.ACCENT_GLOW);
        g.fill(x + 5, y + 8, x + 12, y + 12, MusorTheme.ACCENT_2);
        g.fill(x + 2, y + 1, x + 3, y + 10, 0xFFF1DFFF);
    }

    private void renderUpgradePane(GuiGraphics g) {
        int y = topPos + 58;
        int leftX = leftPos + 12;
        int centerX = leftPos + 116;
        int rightX = leftPos + 240;

        MusorTheme.panel(g, leftX, y, 96, 82, MusorTheme.PANEL, 0xFF4A2A5E);
        MusorTheme.panel(g, centerX, y, 116, 82, 0xFF100817, MusorTheme.BORDER);
        MusorTheme.panel(g, rightX, y, 96, 82, MusorTheme.PANEL, 0xFF4A2A5E);

        centeredScaled(g, ModLanguage.tr("СТАВКА", "STAKE"), leftX + 48, y + 7, 0.72F, MusorTheme.ACCENT);
        centeredScaled(g, ModLanguage.tr("ШАНС", "CHANCE"), centerX + 58, y + 7, 0.72F, MusorTheme.DIM);
        centeredScaled(g, ModLanguage.tr("ЦЕЛЬ", "TARGET"), rightX + 48, y + 7, 0.72F, MusorTheme.ACCENT);

        ItemStack input = menu.getInputStack();
        renderSafeItemScaled(g, input, leftX + 9, y + 27, 1.35F);
        String inputName = input.isEmpty()
            ? ModLanguage.tr("Положите предмет", "Place an item")
            : safeStackName(input);
        drawScaledTrimmed(g, inputName, leftX + 38, y + 29, 52, 0.72F,
            input.isEmpty() ? MusorTheme.MUTED : MusorTheme.TEXT);
        if (!input.isEmpty()) {
            drawScaledTrimmed(g, ModLanguage.tr("ценн. ", "value ") + format((long) safeValue(input)),
                leftX + 38, y + 42, 52, 0.66F, MusorTheme.DIM);
        }
        drawScaledTrimmed(g, ModLanguage.tr("слот ниже", "slot below"), leftX + 10, y + 65, 76, 0.64F, MusorTheme.MUTED);

        Item target = ForgeRegistries.ITEMS.getValue(selectedTarget);
        double chance = estimateChance(input, target);
        centeredScaled(g, String.format(Locale.ROOT, "%.2f%%", chance * 100D), centerX + 58, y + 26,
            1.10F, chance > 0D ? MusorTheme.TEXT : MusorTheme.FAIL);
        centeredScaled(g, chanceLabel(chance), centerX + 58, y + 44, 0.62F,
            chance > 0D ? chanceColor(chance) : MusorTheme.MUTED);

        int gaugeX = centerX + 12;
        int gaugeY = y + 58;
        int gaugeW = 92;
        g.fill(gaugeX, gaugeY, gaugeX + gaugeW, gaugeY + 6, 0xFF24132F);
        g.fill(gaugeX + 1, gaugeY + 1, gaugeX + gaugeW - 1, gaugeY + 2, 0xFF503064);
        int fill = (int) Math.round(gaugeW * chance);
        if (fill > 0) {
            int gaugeColor = chanceColor(chance);
            g.fill(gaugeX, gaugeY, gaugeX + fill, gaugeY + 6, gaugeColor);
            if (fill > 6) g.fill(gaugeX + 2, gaugeY + 1, gaugeX + fill - 2, gaugeY + 2, MusorTheme.brighten(gaugeColor, 32));
        }

        renderSafeItemScaled(g, target == null ? ItemStack.EMPTY : new ItemStack(target), rightX + 8, y + 24, 1.25F);
        drawScaledTrimmed(g, safeItemName(target), rightX + 35, y + 27, 55, 0.70F, MusorTheme.TEXT);
        drawScaledTrimmed(g, shortNamespace(selectedTarget), rightX + 35, y + 39, 55, 0.60F, MusorTheme.MUTED);

        MusorTheme.panel(g, rightX + 5, topPos + 101, 88, 18, 0xFF09050E, MusorTheme.BORDER_SOFT);
        g.fill(rightX + 8, topPos + 104, rightX + 9, topPos + 116, 0xFF5B3471);

        MusorTheme.separator(g, leftPos + 12, topPos + 165, leftPos + imageWidth - 12);
    }

    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY) {
        MusorTheme.panel(g, leftPos + 185, topPos + 31, 158, 22, 0xFF09050E, MusorTheme.BORDER_SOFT);
        g.fill(leftPos + 188, topPos + 34, leftPos + 189, topPos + 49, 0xFF5B3471);

        int start = page * 8;
        for (int i = 0; i < 8; i++) {
            int idx = start + i;
            if (idx >= filteredCases.size()) break;
            renderCaseCard(g, filteredCases.get(idx), i, mouseX, mouseY);
        }

        int pages = Math.max(1, (filteredCases.size() + 7) / 8);
        drawScaled(g, (page + 1) + "/" + pages, leftPos + 118, topPos + 135, 0.62F, MusorTheme.MUTED);

        CaseDefinition c = selectedCase();
        CaseLayout.Rect selected = CaseLayout.selectedPanel();
        int selectedBorder = c == null ? MusorTheme.BORDER_SOFT : c.accent();
        MusorTheme.panel(g, leftPos + selected.x(), topPos + selected.y(), selected.w(), selected.h(),
            0xFF100817, selectedBorder);

        if (c != null) {
            CaseArt.render3d(g, c.category(), leftPos + selected.x() + 3, topPos + selected.y() + 1, 0.90F);
            drawScaledTrimmed(g, c.name(ModLanguage.russian()), leftPos + selected.x() + 21, topPos + selected.y() + 6,
                55, 0.64F, MusorTheme.TEXT);
            drawScaledTrimmed(g, compact(c.cost()) + "✦", leftPos + selected.x() + 77, topPos + selected.y() + 6,
                24, 0.62F, MusorTheme.GOLD);
        }

        MusorTheme.separator(g, leftPos + 12, topPos + 165, leftPos + imageWidth - 12);
    }

    private void renderCaseCard(GuiGraphics g, CaseDefinition c, int localIndex, int mouseX, int mouseY) {
        CaseLayout.Rect r = CaseLayout.card(localIndex);
        int x = leftPos + r.x();
        int y = topPos + r.y();

        boolean hover = r.contains(mouseX - leftPos, mouseY - topPos);
        boolean selected = c.id().equals(selectedCaseId);
        int border = selected
            ? MusorTheme.brighten(c.accent(), 35)
            : hover ? MusorTheme.mix(MusorTheme.BORDER, c.accent(), 0.46F) : MusorTheme.BORDER_SOFT;
        int fill = selected
            ? MusorTheme.mix(0xFF110817, c.accent(), 0.10F)
            : hover ? MusorTheme.PANEL_HOVER : 0xFF100817;

        MusorTheme.panel(g, x, y, r.w(), r.h(), fill, border);

        if (selected || hover) {
            g.fill(x + 3, y + 3, x + 4, y + r.h() - 3, selected ? c.accent() : MusorTheme.BORDER);
        }

        float bob = hover ? (float) (Math.sin(System.currentTimeMillis() / 170D + localIndex) * 0.55D) : 0F;
        CaseArt.render3dAnimated(g, c.category(), x + 4, y + 10, 1.22F, hover ? -2.0F : 0F, bob);

        drawScaledTrimmed(g, c.name(ModLanguage.russian()), x + 31, y + 6, 42, 0.72F, MusorTheme.TEXT);
        drawScaledTrimmed(g, shortRarity(c.rarity()), x + 31, y + 17, 42, 0.64F, rarityColor(c.rarity()));
        drawScaledTrimmed(g, compact(c.cost()) + " ✦", x + 31, y + 27, 42, 0.66F, MusorTheme.GOLD);

        if (selected) {
            g.fill(x + 6, y + r.h() - 3, x + r.w() - 6, y + r.h() - 1, c.accent());
        }
    }

    private void renderCaseAnimation(GuiGraphics g) {
        double p = caseAnimation.progress();
        double e = caseAnimation.eased();

        CaseDefinition rolling = CaseCatalog.byId(caseAnimation.caseId());
        int accent = rolling == null ? MusorTheme.ACCENT : rolling.accent();
        String category = rolling == null ? "SURVIVAL" : rolling.category();

        int x = leftPos + 48;
        int y = topPos + 54;
        int w = 252;
        int h = 108;

        // Dim the content behind the modal and build a crisp layered frame.
        g.fill(leftPos + 6, topPos + 30, leftPos + imageWidth - 6, topPos + 165, 0xB8000000);
        MusorTheme.panel(g, x, y, w, h, 0xFA09050E, MusorTheme.brighten(accent, 24));
        g.fill(x + 5, y + 5, x + w - 5, y + 6, MusorTheme.darken(accent, 26));

        String phase = p < .18D
            ? ModLanguage.tr("ПОДГОТОВКА", "PRIMING")
            : p < .72D
                ? ModLanguage.tr("ОТКРЫТИЕ", "OPENING")
                : p < .93D
                    ? ModLanguage.tr("ФИКСАЦИЯ", "LOCKING")
                    : ModLanguage.tr("НАГРАДА", "REWARD");
        centeredScaled(g, phase, x + w / 2, y + 10, 0.68F, p >= .93D ? MusorTheme.GOLD : MusorTheme.DIM);

        int cx = x + w / 2;
        int cy = y + 56;
        double pulse = 1D + Math.sin(p * Math.PI * 10D) * (p < .82D ? 0.035D : 0.015D);
        double shakeStrength = p < .72D ? Math.min(2.8D, p * 5D) : Math.max(0D, (0.93D - p) * 8D);
        int shakeX = (int) Math.round(Math.sin(p * 95D) * shakeStrength);
        float angle = (float) (Math.sin(p * 46D) * (p < .82D ? 4.0D : 1.2D));
        float scale = (float) (2.45D * pulse);

        // Pixel halo and corner brackets behind the 3D case.
        int halo = MusorTheme.mix(MusorTheme.BORDER_SOFT, accent, (float) Math.min(1D, p * 1.4D));
        g.fill(cx - 34, cy - 28, cx + 34, cy - 27, halo);
        g.fill(cx - 34, cy + 27, cx + 34, cy + 28, MusorTheme.darken(halo, 25));
        g.fill(cx - 35, cy - 27, cx - 34, cy + 28, halo);
        g.fill(cx + 34, cy - 27, cx + 35, cy + 28, halo);
        if (p > .48D) {
            int spark = MusorTheme.brighten(accent, 35);
            int spread = 22 + (int) Math.round(12D * e);
            g.fill(cx - spread - 8, cy - 1, cx - spread, cy + 1, spark);
            g.fill(cx + spread, cy - 1, cx + spread + 8, cy + 1, spark);
            g.fill(cx - 1, cy - spread - 3, cx + 1, cy - spread + 5, spark);
        }

        CaseArt.render3dAnimated(g, category, cx - 20 + shakeX, cy - 20, scale, angle, 0F);

        int barX = x + 14;
        int barY = y + 86;
        int barW = w - 28;
        g.fill(barX, barY, barX + barW, barY + 4, 0xFF21102B);
        int progress = Math.max(1, (int) Math.round(barW * p));
        g.fill(barX, barY, barX + progress, barY + 4, accent);
        if (progress > 5) g.fill(barX + 2, barY + 1, barX + progress - 2, barY + 2, MusorTheme.brighten(accent, 36));

        if (p >= .93D) {
            String reward = ModLanguage.tr("ВЫПАЛО: ", "DROPPED: ") + shortId(caseAnimation.rewardId()) + " ×" + caseAnimation.count();
            centeredScaledTrimmed(g, reward, x + w / 2, y + 96, w - 20, 0.72F, MusorTheme.SUCCESS);
        } else {
            centeredScaled(g, ModLanguage.tr("результат скрыт", "result hidden"), x + w / 2, y + 96, 0.62F, MusorTheme.MUTED);
        }
    }

    private void renderLanguageGate(GuiGraphics g) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        MusorTheme.panel(g, x, y, imageWidth, imageHeight, MusorTheme.BG, MusorTheme.BORDER);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + 28, 0xFF100817);
        MusorTheme.separator(g, x + 12, y + 28, x + imageWidth - 12);

        drawBrandMark(g, x + 95, y + 54);
        centeredScaled(g, "MUSOR DROP", x + imageWidth / 2, y + 56, 1.04F, MusorTheme.TEXT);
        centeredScaled(g, "PREMIUM STATION", x + imageWidth / 2, y + 78, 0.62F, MusorTheme.ACCENT);
        centeredScaled(g, "ВЫБЕРИТЕ ЯЗЫК / CHOOSE LANGUAGE", x + imageWidth / 2, y + 103, 0.68F, MusorTheme.DIM);

        MusorTheme.softPanel(g, x + 50, y + 116, 248, 47);
        centeredScaled(g, ModLanguage.tr("Язык можно сменить позже", "Language can be changed later"),
            x + imageWidth / 2, y + 166, 0.62F, MusorTheme.MUTED);
        centeredScaled(g, "Minecraft 1.20.1 • Forge 47.x", x + imageWidth / 2, y + 216, 0.60F, MusorTheme.MUTED);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tab == MainTab.LANGUAGE) {
            if (button == 0) {
                if (russianButton.mouseClicked(mouseX, mouseY, button)) return true;
                if (englishButton.mouseClicked(mouseX, mouseY, button)) return true;
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
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int gridBottom = CaseLayout.GRID_Y + 2 * CaseLayout.CARD_H + CaseLayout.GAP_Y;
        if (tab == MainTab.CASES && !caseAnimation.active()
            && mouseY >= topPos + CaseLayout.GRID_Y && mouseY < topPos + gridBottom) {
            changePage(delta > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (tab == MainTab.LANGUAGE) {
            if (keyCode == 49 || keyCode == 82) {
                chooseLanguage(true);
                return true;
            }
            if (keyCode == 50 || keyCode == 69) {
                chooseLanguage(false);
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void chooseLanguage(boolean russian) {
        ModLanguage.choose(russian);
        tab = MainTab.UPGRADE;
        playUi(SoundEvents.PLAYER_LEVELUP, 0.92F);
        refreshTexts();
        updateWidgetVisibility();
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        // All labels are intentionally rendered in absolute screen coordinates to support
        // scaled micro-typography and consistent alignment at high GUI scale.
    }

    private void drawInventoryFrame(GuiGraphics g) {
        int invX = leftPos + StationLayout.INVENTORY_X;
        int invY = topPos + StationLayout.INVENTORY_Y;

        drawScaled(g, ModLanguage.tr("ИНВЕНТАРЬ", "INVENTORY"), invX, topPos + 164, 0.68F, MusorTheme.DIM);
        g.fill(invX + 68, topPos + 168, leftPos + imageWidth - 12, topPos + 169, MusorTheme.BORDER_FAINT);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                MusorTheme.slot(g, invX + col * 18, invY + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            MusorTheme.slot(g, invX + col * 18, topPos + StationLayout.HOTBAR_Y);
        }

        MusorTheme.panel(g, leftPos + 12, topPos + 168, 150, 73, 0xFF100817, 0xFF4D2B62);
        ItemStack input = menu.getInputStack();

        if (tab == MainTab.CASES) {
            drawScaled(g, ModLanguage.tr("ПРОДАЖА", "SELL"), leftPos + 18, topPos + 175, 0.72F, MusorTheme.GOLD);
            drawScaled(g, "72%", leftPos + 72, topPos + 175, 0.62F, MusorTheme.MUTED);
            MusorTheme.slot(g, leftPos + StationLayout.INPUT_X, topPos + StationLayout.INPUT_Y);

            long estimate = input.isEmpty() ? 0L : Math.max(0L, (long) Math.floor(safeValue(input) * 0.72D));
            drawScaledTrimmed(g,
                input.isEmpty() ? ModLanguage.tr("положите предмет", "place item") : safeStackName(input),
                leftPos + 55, topPos + 187, 42, 0.62F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.TEXT);
            if (!input.isEmpty()) {
                drawScaledTrimmed(g, "+" + format(estimate) + " ✦", leftPos + 55, topPos + 198, 42, 0.60F, MusorTheme.GOLD);
            }
        } else {
            drawScaled(g, ModLanguage.tr("СЛОТ СТАВКИ", "STAKE SLOT"), leftPos + 18, topPos + 175, 0.68F, MusorTheme.ACCENT);
            MusorTheme.slot(g, leftPos + StationLayout.INPUT_X, topPos + StationLayout.INPUT_Y);
            drawScaledTrimmed(g,
                input.isEmpty() ? ModLanguage.tr("перетащите предмет", "drag an item") : safeStackName(input),
                leftPos + 55, topPos + 190, 92, 0.64F, input.isEmpty() ? MusorTheme.MUTED : MusorTheme.TEXT);
            drawScaledTrimmed(g,
                input.isEmpty() ? ModLanguage.tr("Shift+клик тоже работает", "Shift-click also works")
                    : ModLanguage.tr("готово к попытке", "ready for attempt"),
                leftPos + 18, topPos + 218, 132, 0.58F, MusorTheme.MUTED);
        }
    }

    private void renderSafeItem(GuiGraphics g, ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) {
            drawFallbackCube(g, x, y, MusorTheme.BORDER);
            return;
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id != null && "minecraft".equals(id.getNamespace())) {
            try {
                g.renderItem(stack, x, y);
                return;
            } catch (RuntimeException ex) {
                Upgrader.LOGGER.debug("Vanilla preview render failed for {}", id, ex);
            }
        }

        drawFallbackCube(g, x, y, MusorTheme.ACCENT_DARK);
    }

    private void renderSafeItemScaled(GuiGraphics g, ItemStack stack, int x, int y, float scale) {
        g.pose().pushPose();
        g.pose().translate(x, y, 80F);
        g.pose().scale(scale, scale, 1F);
        renderSafeItem(g, stack, 0, 0);
        g.pose().popPose();
    }

    private static void drawFallbackCube(GuiGraphics g, int x, int y, int accent) {
        g.fill(x + 2, y + 5, x + 14, y + 16, MusorTheme.darken(accent, 35));
        g.fill(x + 4, y + 2, x + 12, y + 6, MusorTheme.brighten(accent, 20));
        g.fill(x + 6, y + 7, x + 10, y + 11, MusorTheme.brighten(accent, 45));
    }

    private double safeValue(ItemStack stack) {
        try {
            return ItemValueService.value(stack);
        } catch (RuntimeException ex) {
            ResourceLocation id = stack == null || stack.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(stack.getItem());
            Upgrader.LOGGER.debug("Failed to estimate UI value for {}", id, ex);
            return 0D;
        }
    }

    private double estimateChance(ItemStack input, Item target) {
        if (input == null || input.isEmpty() || target == null) return 0D;
        try {
            return UpgradeOdds.calculate(safeValue(input), ItemValueService.unitValue(target));
        } catch (RuntimeException ex) {
            Upgrader.LOGGER.debug("Failed to estimate upgrade chance for target {}", selectedTarget, ex);
            return 0D;
        }
    }

    private String safeItemName(Item item) {
        if (item == null) return ModLanguage.tr("Нет цели", "No target");
        try {
            return new ItemStack(item).getHoverName().getString();
        } catch (RuntimeException ex) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            return id == null ? ModLanguage.tr("Неизвестно", "Unknown") : id.getPath().replace('_', ' ');
        }
    }

    private String safeStackName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        try {
            return stack.getHoverName().getString();
        } catch (RuntimeException ex) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            return id == null ? ModLanguage.tr("Предмет", "Item") : id.getPath().replace('_', ' ');
        }
    }

    private String localizeError(String code) {
        return switch (code) {
            case "not_enough" -> ModLanguage.tr("Недостаточно Musor Shards", "Not enough Musor Shards");
            case "not_sellable" -> ModLanguage.tr("Этот предмет нельзя продать", "This item cannot be sold");
            case "too_cheap" -> ModLanguage.tr("Предмет слишком дешёвый", "Item value is too low");
            case "no_reward" -> ModLanguage.tr("Нет подходящей награды — валюта не списана", "No eligible reward — no shards spent");
            case "cooldown" -> ModLanguage.tr("Слишком быстро — подождите", "Too fast — wait a moment");
            case "invalid_upgrade", "invalid_value" -> ModLanguage.tr("Нельзя выполнить улучшение", "Upgrade cannot be performed");
            case "same_item" -> ModLanguage.tr("Выберите другой предмет", "Choose a different target");
            default -> ModLanguage.tr("Операция отклонена сервером", "Operation rejected by server");
        };
    }

    private void renderToast(GuiGraphics g) {
        String s = trim(toast, 210);
        int w = Math.min(224, font.width(s) + 14);
        int x = leftPos + imageWidth / 2 - w / 2;
        int y = topPos + 4;
        MusorTheme.panel(g, x, y, w, 20, 0xF02A1638, MusorTheme.ACCENT_2);
        centeredScaledTrimmed(g, s, x + w / 2, y + 6, w - 12, 0.72F, MusorTheme.TEXT);
    }

    private void renderCaseTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (tab != MainTab.CASES || caseAnimation.active()) return;

        int start = page * 8;
        for (int i = 0; i < 8; i++) {
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
    }

    private void drawScaled(GuiGraphics g, String text, int x, int y, float scale, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0F);
        g.pose().scale(scale, scale, 1F);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    private void drawScaledTrimmed(GuiGraphics g, String text, int x, int y, int maxWidth, float scale, int color) {
        String cut = trimScaled(text, maxWidth, scale);
        drawScaled(g, cut, x, y, scale, color);
    }

    private void centeredScaled(GuiGraphics g, String text, int cx, int y, float scale, int color) {
        int visualWidth = Math.round(font.width(text) * scale);
        drawScaled(g, text, cx - visualWidth / 2, y, scale, color);
    }

    private void centeredScaledTrimmed(GuiGraphics g, String text, int cx, int y, int maxWidth, float scale, int color) {
        String cut = trimScaled(text, maxWidth, scale);
        centeredScaled(g, cut, cx, y, scale, color);
    }

    private String trimScaled(String text, int maxPx, float scale) {
        String source = text == null ? "" : text;
        int sourcePx = Math.max(1, (int) Math.floor(maxPx / Math.max(0.01F, scale)));
        String cut = font.plainSubstrByWidth(source, sourcePx);
        if (!cut.equals(source) && cut.length() > 1) return cut.substring(0, cut.length() - 1) + "…";
        return cut;
    }

    private String trim(String text, int maxPx) {
        String source = text == null ? "" : text;
        String cut = font.plainSubstrByWidth(source, maxPx);
        if (!cut.equals(source) && cut.length() > 1) return cut.substring(0, cut.length() - 1) + "…";
        return cut;
    }

    private void playUi(SoundEvent event, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch));
    }

    private static String chanceLabel(double chance) {
        if (chance <= 0D) return "—";
        if (chance >= .70D) return ModLanguage.tr("ВЫСОКИЙ", "HIGH");
        if (chance >= .35D) return ModLanguage.tr("СРЕДНИЙ", "MEDIUM");
        return ModLanguage.tr("РИСК", "RISK");
    }

    private static int chanceColor(double chance) {
        if (chance >= .70D) return MusorTheme.SUCCESS;
        if (chance >= .35D) return MusorTheme.GOLD;
        return MusorTheme.ACCENT_2;
    }

    private static String shortNamespace(ResourceLocation id) {
        if (id == null) return "?";
        return id.getNamespace() + ":" + id.getPath();
    }

    private static String shortId(String id) {
        if (id == null || id.isBlank()) return "?";
        int colon = id.indexOf(':');
        return (colon >= 0 ? id.substring(colon + 1) : id).replace('_', ' ');
    }

    private static String shortRarity(String rarity) {
        return switch (rarity) {
            case "UNCOMMON" -> "UNC";
            case "LEGENDARY" -> "LEG";
            default -> rarity;
        };
    }

    private static int rarityColor(String rarity) {
        return switch (rarity) {
            case "UNCOMMON" -> 0xFF9FE6A4;
            case "RARE" -> 0xFF98B8FF;
            case "EPIC" -> 0xFFD995FF;
            case "LEGENDARY" -> 0xFFFF9D74;
            case "MYTHIC" -> 0xFFFF99ED;
            case "MASTER" -> 0xFFFFD479;
            case "RNG" -> 0xFFFFC86B;
            default -> 0xFFB9B9C5;
        };
    }

    private static String format(long n) {
        return String.format(Locale.ROOT, "%,d", Math.max(0, n)).replace(',', ' ');
    }

    private static String compact(long n) {
        n = Math.max(0, n);
        if (n < 1_000L) return Long.toString(n);
        if (n < 1_000_000L) return String.format(Locale.ROOT, "%.1fK", n / 1_000D);
        if (n < 1_000_000_000L) return String.format(Locale.ROOT, "%.1fM", n / 1_000_000D);
        return "999M+";
    }
}
