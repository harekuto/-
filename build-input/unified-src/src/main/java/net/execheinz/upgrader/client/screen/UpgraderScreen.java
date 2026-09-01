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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

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

    public UpgraderScreen(UpgraderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = StationLayout.GUI_W;
        this.imageHeight = StationLayout.GUI_H;
        this.inventoryLabelX = StationLayout.INVENTORY_X;
        this.inventoryLabelY = StationLayout.INVENTORY_Y - 11;
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

        upgradeTab = addRenderableWidget(button(14, 31, 90, 22, ModLanguage.tr("АПГРЕЙД", "UPGRADE"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.UPGRADE)));
        casesTab = addRenderableWidget(button(108, 31, 90, 22, ModLanguage.tr("КЕЙСЫ", "CASES"),
            MusorButton.Style.TAB, () -> switchTab(MainTab.CASES)));
        languageToggle = addRenderableWidget(button(338, 7, 34, 18, ModLanguage.russian() ? "RU" : "EN",
            MusorButton.Style.COMPACT, () -> {
                ModLanguage.choose(!ModLanguage.russian());
                refreshTexts();
            }));

        russianButton = addRenderableWidget(button(78, 123, 104, 32, "РУССКИЙ",
            MusorButton.Style.PRIMARY, () -> chooseLanguage(true)));
        englishButton = addRenderableWidget(button(202, 123, 104, 32, "ENGLISH",
            MusorButton.Style.PRIMARY, () -> chooseLanguage(false)));

        targetSearch = addRenderableWidget(new EditBox(font, leftPos + 217, topPos + 91, 150, 18, Component.literal("Search target")));
        targetSearch.setMaxLength(48);
        targetSearch.setBordered(false);
        targetSearch.setHint(Component.literal(ModLanguage.tr("Поиск цели...", "Search target...")));

        targetNext = addRenderableWidget(button(217, 115, 150, 20, ModLanguage.tr("СЛЕДУЮЩАЯ ЦЕЛЬ", "NEXT TARGET"),
            MusorButton.Style.SECONDARY, this::nextTarget));

        upgradeButton = addRenderableWidget(button(132, 170, 120, 22, ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE"),
            MusorButton.Style.PRIMARY, () -> {
                ModNetwork.sendToServer(new C2SUpgradePacket(selectedTarget.toString()));
                toast = ModLanguage.tr("Сервер проверяет попытку...", "Server is resolving...");
                toastUntil = System.currentTimeMillis() + 1400L;
            }));

        caseSearch = addRenderableWidget(new EditBox(font, leftPos + 220, topPos + 33, 148, 18, Component.literal("Search cases")));
        caseSearch.setMaxLength(48);
        caseSearch.setBordered(false);
        caseSearch.setHint(Component.literal(ModLanguage.tr("Поиск кейса...", "Search cases...")));
        caseSearch.setResponder(v -> rebuildCaseFilter());

        CaseLayout.Rect prev = CaseLayout.prevButton();
        CaseLayout.Rect cat = CaseLayout.categoryButton();
        CaseLayout.Rect next = CaseLayout.nextButton();
        CaseLayout.Rect open = CaseLayout.openButton();

        prevButton = addRenderableWidget(button(prev.x(), prev.y(), prev.w(), prev.h(), ModLanguage.tr("◀ НАЗАД", "◀ PREV"),
            MusorButton.Style.COMPACT, () -> changePage(-1)));
        categoryButton = addRenderableWidget(button(cat.x(), cat.y(), cat.w(), cat.h(), "ALL",
            MusorButton.Style.SECONDARY, this::cycleCategory));
        nextButton = addRenderableWidget(button(next.x(), next.y(), next.w(), next.h(), ModLanguage.tr("ДАЛЕЕ ▶", "NEXT ▶"),
            MusorButton.Style.COMPACT, () -> changePage(1)));
        openCaseButton = addRenderableWidget(button(open.x(), open.y(), open.w(), open.h(), ModLanguage.tr("ОТКРЫТЬ", "OPEN"),
            MusorButton.Style.PRIMARY, this::openSelectedCase));

        refreshButton = addRenderableWidget(button(53, 220, 47, 18, ModLanguage.tr("ОБН.", "SYNC"),
            MusorButton.Style.COMPACT, () -> ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.SYNC, ""))));
        sellOneButton = addRenderableWidget(button(14, 250, 40, 18, ModLanguage.tr("1 ШТ.", "SELL 1"),
            MusorButton.Style.COMPACT, () -> ModNetwork.sendToServer(new C2SCaseActionPacket(C2SCaseActionPacket.SELL_ONE, ""))));
        sellStackButton = addRenderableWidget(button(58, 250, 42, 18, ModLanguage.tr("СТЕК", "STACK"),
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
        targetNext.setMessage(Component.literal(ModLanguage.tr("СЛЕДУЮЩАЯ ЦЕЛЬ", "NEXT TARGET")));
        upgradeButton.setMessage(Component.literal(ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE")));
        prevButton.setMessage(Component.literal(ModLanguage.tr("◀ НАЗАД", "◀ PREV")));
        nextButton.setMessage(Component.literal(ModLanguage.tr("ДАЛЕЕ ▶", "NEXT ▶")));
        openCaseButton.setMessage(Component.literal(ModLanguage.tr("ОТКРЫТЬ", "OPEN")));
        refreshButton.setMessage(Component.literal(ModLanguage.tr("ОБН.", "SYNC")));
        sellOneButton.setMessage(Component.literal(ModLanguage.tr("1 ШТ.", "SELL 1")));
        sellStackButton.setMessage(Component.literal(ModLanguage.tr("СТЕК", "STACK")));
        targetSearch.setHint(Component.literal(ModLanguage.tr("Поиск цели...", "Search target...")));
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
        toast = ModLanguage.tr("Открытие запрошено...", "Opening requested...");
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
            updateCaseButtons();
        }

        if (ClientState.upgradeSerial != seenUpgradeSerial) {
            seenUpgradeSerial = ClientState.upgradeSerial;
            toast = ClientState.upgradeSuccess
                ? ModLanguage.tr("УСПЕХ • ", "SUCCESS • ") + shortId(ClientState.upgradeReward)
                : ModLanguage.tr("НЕУДАЧА • предмет потерян", "FAILED • item lost");
            toastUntil = System.currentTimeMillis() + 2800L;
        }

        if (ClientState.sellSerial != seenSellSerial) {
            seenSellSerial = ClientState.sellSerial;
            toast = ModLanguage.tr("ПРОДАНО • +", "SOLD • +") + format(ClientState.sellDelta) + " ✦";
            toastUntil = System.currentTimeMillis() + 2200L;
        }

        if (ClientState.errorSerial != seenErrorSerial) {
            seenErrorSerial = ClientState.errorSerial;
            toast = localizeError(ClientState.error);
            toastUntil = System.currentTimeMillis() + 2600L;
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

        g.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + 28, 0xFF130A1D);
        g.fill(leftPos + 18, topPos + 27, leftPos + imageWidth - 18, topPos + 28, 0xFF4E2B64);
        centered(g, "MUSOR DROP", leftPos + imageWidth / 2, topPos + 10, MusorTheme.TEXT);

        g.fill(leftPos + 10, topPos + 9, leftPos + 14, topPos + 13, MusorTheme.ACCENT);
        g.fill(leftPos + 14, topPos + 12, leftPos + 18, topPos + 16, MusorTheme.ACCENT_2);
        g.fill(leftPos + 10, topPos + 15, leftPos + 14, topPos + 19, 0xFFDAB3FF);

        if (ClientState.balance > 0L || tab == MainTab.CASES) {
            g.drawString(font, "✦ " + compact(ClientState.balance), leftPos + 282, topPos + 10, MusorTheme.GOLD, false);
        }

        if (tab == MainTab.UPGRADE) renderUpgradePane(g);
        else renderCasesPane(g, mouseX, mouseY);

        drawInventoryFrame(g);
    }

    private void renderUpgradePane(GuiGraphics g) {
        int y = topPos + 59;

        MusorTheme.panel(g, leftPos + 14, y, 170, 94, MusorTheme.PANEL, 0xFF56306D);
        MusorTheme.panel(g, leftPos + 200, y, 170, 94, MusorTheme.PANEL, 0xFF56306D);

        centered(g, ModLanguage.tr("ПРЕДМЕТ-СТАВКА", "STAKE ITEM"), leftPos + 99, y + 9, MusorTheme.ACCENT);
        centered(g, ModLanguage.tr("ЖЕЛАЕМЫЙ ДРОП", "TARGET DROP"), leftPos + 285, y + 9, MusorTheme.ACCENT);

        ItemStack input = menu.getInputStack();
        renderSafeItem(g, input, leftPos + 28, y + 31);
        String inputName = input.isEmpty()
            ? ModLanguage.tr("Положите предмет в слот", "Place an item in the slot")
            : safeStackName(input);
        g.drawString(font, trim(inputName, 124), leftPos + 51, y + 31, input.isEmpty() ? MusorTheme.DIM : MusorTheme.TEXT, false);

        if (!input.isEmpty()) {
            g.drawString(font, ModLanguage.tr("Ценность ", "Value ") + format((long) safeValue(input)),
                leftPos + 51, y + 44, MusorTheme.DIM, false);
        }
        g.drawString(font, ModLanguage.tr("Слот ставки находится снизу слева", "Stake slot is below on the left"),
            leftPos + 24, y + 72, MusorTheme.MUTED, false);

        Item target = ForgeRegistries.ITEMS.getValue(selectedTarget);
        renderSafeItem(g, target == null ? ItemStack.EMPTY : new ItemStack(target), leftPos + 214, y + 31);
        g.drawString(font, trim(safeItemName(target), 128), leftPos + 237, y + 31, MusorTheme.TEXT, false);
        g.drawString(font, trim(selectedTarget.toString(), 128), leftPos + 237, y + 44, MusorTheme.MUTED, false);

        MusorTheme.panel(g, leftPos + 214, topPos + 88, 156, 23, 0xFF09050E, 0xFF4E2B64);

        double chance = estimateChance(input, target);
        int gaugeX = leftPos + 78;
        int gaugeY = topPos + 158;
        int gaugeW = 228;

        centered(g, String.format(Locale.ROOT, "%.2f%%", chance * 100D), leftPos + 192, topPos + 146,
            chance > 0D ? MusorTheme.TEXT : MusorTheme.FAIL);

        g.fill(gaugeX, gaugeY, gaugeX + gaugeW, gaugeY + 8, 0xFF21102C);
        g.fill(gaugeX + 1, gaugeY + 1, gaugeX + gaugeW - 1, gaugeY + 2, 0xFF4C2B61);
        int fill = (int) Math.round(gaugeW * chance);
        if (fill > 0) {
            g.fill(gaugeX, gaugeY, gaugeX + fill, gaugeY + 8, chance >= 0.5D ? MusorTheme.SUCCESS : MusorTheme.ACCENT_2);
        }
    }

    private void renderCasesPane(GuiGraphics g, int mouseX, int mouseY) {
        MusorTheme.panel(g, leftPos + 217, topPos + 30, 154, 23, 0xFF09050E, 0xFF4E2B64);

        int start = page * 8;
        for (int i = 0; i < 8; i++) {
            int idx = start + i;
            if (idx >= filteredCases.size()) break;
            renderCaseCard(g, filteredCases.get(idx), i, mouseX, mouseY);
        }

        int pages = Math.max(1, (filteredCases.size() + 7) / 8);
        centered(g, (page + 1) + " / " + pages, leftPos + 103, topPos + 168, MusorTheme.DIM);

        CaseDefinition c = selectedCase();
        CaseLayout.Rect selected = CaseLayout.selectedPanel();

        MusorTheme.panel(g, leftPos + selected.x(), topPos + selected.y(), selected.w(), selected.h(),
            0xFF110817, c == null ? MusorTheme.BORDER_SOFT : c.accent());

        if (c != null) {
            CaseArt.render3d(g, c.category(), leftPos + selected.x() + 2, topPos + selected.y() + 1, 1.0F);
            g.drawString(font, trim(c.name(ModLanguage.russian()), 95), leftPos + selected.x() + 22, topPos + selected.y() + 5,
                MusorTheme.TEXT, false);
            g.drawString(font, shortRarity(c.rarity()), leftPos + selected.x() + 124, topPos + selected.y() + 5,
                rarityColor(c.rarity()), false);
            g.drawString(font, format(c.cost()) + " ✦", leftPos + selected.x() + 183, topPos + selected.y() + 5,
                MusorTheme.GOLD, false);
        }
    }

    private void renderCaseCard(GuiGraphics g, CaseDefinition c, int localIndex, int mouseX, int mouseY) {
        CaseLayout.Rect r = CaseLayout.card(localIndex);
        int x = leftPos + r.x();
        int y = topPos + r.y();

        boolean hover = r.contains(mouseX - leftPos, mouseY - topPos);
        boolean selected = c.id().equals(selectedCaseId);

        int border = selected
            ? MusorTheme.brighten(c.accent(), 38)
            : hover ? MusorTheme.brighten(c.accent(), 12) : 0xFF452658;

        MusorTheme.panel(g, x, y, r.w(), r.h(), hover ? 0xFF1D0F28 : 0xFF110817, border);
        CaseArt.render3d(g, c.category(), x + 2, y + 8, 1.55F);

        g.drawString(font, trim(c.name(ModLanguage.russian()), 45), x + 35, y + 7, MusorTheme.TEXT, false);
        g.drawString(font, shortRarity(c.rarity()), x + 35, y + 18, rarityColor(c.rarity()), false);
        g.drawString(font, format(c.cost()), x + 35, y + 32, MusorTheme.GOLD, false);

        if (selected) {
            g.fill(x + 5, y + r.h() - 3, x + r.w() - 5, y + r.h() - 1, c.accent());
        }
    }

    private void renderCaseAnimation(GuiGraphics g) {
        double p = caseAnimation.progress();
        double e = caseAnimation.eased();

        CaseDefinition rolling = CaseCatalog.byId(caseAnimation.caseId());
        int accent = rolling == null ? MusorTheme.ACCENT : rolling.accent();

        int x = leftPos + 42;
        int y = topPos + 69;
        int w = 300;
        int h = 92;

        MusorTheme.panel(g, x, y, w, h, 0xFA08050D, MusorTheme.brighten(accent, 20));

        String phase = p < .20D
            ? ModLanguage.tr("РАЗГОН", "ACCELERATING")
            : p < .76D
                ? ModLanguage.tr("КРУТКА", "SPINNING")
                : p < .95D
                    ? ModLanguage.tr("ТОРМОЖЕНИЕ", "DECELERATING")
                    : ModLanguage.tr("ФИКСАЦИЯ", "LOCKING");
        centered(g, phase, x + w / 2, y + 8, MusorTheme.TEXT);

        int center = x + w / 2;
        int slot = 38;
        int base = (int) Math.floor(e * 42D);
        int shift = (int) Math.round((e * 42D - base) * slot);

        List<CaseDefinition> source = filteredCases.isEmpty() ? CaseCatalog.all() : filteredCases;
        for (int i = -4; i <= 4; i++) {
            int sx = center + i * slot - shift - 13;
            if (sx < x + 8 || sx > x + w - 34) continue;

            CaseDefinition iconCase = source.isEmpty()
                ? rolling
                : source.get(Math.floorMod(base + i + Math.max(0, page * 8), source.size()));
            String category = iconCase == null ? "SURVIVAL" : iconCase.category();

            int dist = Math.abs((sx + 13) - center);
            int frame = dist < 18 ? MusorTheme.brighten(accent, 40) : 0xFF3D234D;
            MusorTheme.panel(g, sx - 2, y + 28, 30, 31, 0xFF100817, frame);
            CaseArt.render3d(g, category, sx, y + 32, 1.45F);
        }

        g.fill(center - 1, y + 24, center + 1, y + 63, 0xFFF4E7FF);

        int barX = x + 12;
        int barW = w - 24;
        g.fill(barX, y + 67, barX + barW, y + 71, 0xFF2B1737);
        g.fill(barX, y + 67, barX + (int) Math.round(barW * p), y + 71, accent);

        if (p >= .92D) {
            centeredTrimmed(g,
                ModLanguage.tr("ВЫПАЛО: ", "DROPPED: ") + shortId(caseAnimation.rewardId()) + " ×" + caseAnimation.count(),
                x + w / 2, y + 77, w - 18, MusorTheme.SUCCESS);
        } else {
            centered(g, "???", x + w / 2, y + 77, MusorTheme.DIM);
        }
    }

    private void renderLanguageGate(GuiGraphics g) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        MusorTheme.panel(g, x, y, imageWidth, imageHeight, MusorTheme.BG, MusorTheme.BORDER);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + 29, 0xFF130A1D);

        centered(g, "MUSOR DROP", x + imageWidth / 2, y + 44, MusorTheme.TEXT);

        g.fill(x + 184, y + 68, x + 200, y + 72, MusorTheme.ACCENT);
        g.fill(x + 180, y + 72, x + 204, y + 76, MusorTheme.ACCENT_2);
        g.fill(x + 188, y + 76, x + 196, y + 88, 0xFFE6C7FF);

        centered(g, "ВЫБЕРИТЕ ЯЗЫК / CHOOSE LANGUAGE", x + imageWidth / 2, y + 99, MusorTheme.DIM);
        centered(g, "1 / R", x + 130, y + 165, MusorTheme.MUTED);
        centered(g, "2 / E", x + 254, y + 165, MusorTheme.MUTED);
        centered(g, "Minecraft 1.20.1 • Forge 47.x", x + imageWidth / 2, y + 242, MusorTheme.MUTED);
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
                    updateCaseButtons();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (tab == MainTab.CASES && !caseAnimation.active() && mouseY >= topPos + CaseLayout.GRID_Y && mouseY < topPos + 161) {
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
        refreshTexts();
        updateWidgetVisibility();
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
    }

    private void drawInventoryFrame(GuiGraphics g) {
        int invX = leftPos + StationLayout.INVENTORY_X;
        int invY = topPos + StationLayout.INVENTORY_Y;

        g.drawString(font, ModLanguage.tr("ИНВЕНТАРЬ", "INVENTORY"), invX, topPos + 200, MusorTheme.DIM, false);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                MusorTheme.slot(g, invX + col * 18, invY + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            MusorTheme.slot(g, invX + col * 18, topPos + StationLayout.HOTBAR_Y);
        }

        MusorTheme.panel(g, leftPos + 12, topPos + 207, 90, 68, 0xFF100817, 0xFF4D2B62);
        g.drawString(font,
            tab == MainTab.CASES ? ModLanguage.tr("ПРОДАЖА • 72%", "SELL • 72%") : ModLanguage.tr("СТАВКА", "STAKE"),
            leftPos + 18, topPos + 212, tab == MainTab.CASES ? MusorTheme.GOLD : MusorTheme.ACCENT, false);

        MusorTheme.slot(g, leftPos + StationLayout.INPUT_X, topPos + StationLayout.INPUT_Y);

        if (tab == MainTab.UPGRADE) {
            ItemStack input = menu.getInputStack();
            centeredTrimmed(g,
                input.isEmpty() ? ModLanguage.tr("предмет", "item") : safeStackName(input),
                leftPos + 57, topPos + 260, 80, MusorTheme.MUTED);
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
        String s = trim(toast, 230);
        int w = Math.min(246, font.width(s) + 16);
        int x = leftPos + imageWidth / 2 - w / 2;
        int y = topPos + 4;
        MusorTheme.panel(g, x, y, w, 20, 0xF02A1638, MusorTheme.ACCENT_2);
        centered(g, s, x + w / 2, y + 6, MusorTheme.TEXT);
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

    private void centered(GuiGraphics g, String text, int cx, int y, int color) {
        g.drawString(font, text, cx - font.width(text) / 2, y, color, false);
    }

    private void centeredTrimmed(GuiGraphics g, String text, int cx, int y, int maxWidth, int color) {
        String cut = trim(text, maxWidth);
        centered(g, cut, cx, y, color);
    }

    private String trim(String text, int maxPx) {
        String source = text == null ? "" : text;
        String cut = font.plainSubstrByWidth(source, maxPx);
        if (!cut.equals(source) && cut.length() > 1) return cut.substring(0, cut.length() - 1) + "…";
        return cut;
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
