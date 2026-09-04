#!/usr/bin/env python3
from pathlib import Path
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else "musor-drop")


def replace_once(path: Path, old: str, new: str) -> None:
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one match in {path}: got {count}\n--- needle ---\n{old}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")

# 1) Packet direction hardening. The wire format is unchanged; only accepted direction is constrained.
network = root / "src/main/java/net/execheinz/upgrader/network/ModNetwork.java"
replace_once(network,
    "package net.execheinz.upgrader.network;\n\n",
    "package net.execheinz.upgrader.network;\n\nimport java.util.Optional;\n")
replace_once(network,
    "import net.minecraftforge.network.NetworkRegistry;\n",
    "import net.minecraftforge.network.NetworkDirection;\nimport net.minecraftforge.network.NetworkRegistry;\n")
replace_once(network,
    """        CHANNEL.registerMessage(id++, C2SCaseActionPacket.class, C2SCaseActionPacket::encode, C2SCaseActionPacket::decode, C2SCaseActionPacket::handle);\n        CHANNEL.registerMessage(id++, C2SUpgradePacket.class, C2SUpgradePacket::encode, C2SUpgradePacket::decode, C2SUpgradePacket::handle);\n        CHANNEL.registerMessage(id++, S2CStatePacket.class, S2CStatePacket::encode, S2CStatePacket::decode, S2CStatePacket::handle);\n""",
    """        CHANNEL.registerMessage(id++, C2SCaseActionPacket.class, C2SCaseActionPacket::encode, C2SCaseActionPacket::decode, C2SCaseActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));\n        CHANNEL.registerMessage(id++, C2SUpgradePacket.class, C2SUpgradePacket::encode, C2SUpgradePacket::decode, C2SUpgradePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));\n        CHANNEL.registerMessage(id++, S2CStatePacket.class, S2CStatePacket::encode, S2CStatePacket::decode, S2CStatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));\n""")

# 2) Durable paid-reward semantics. Queue pressure is an operator warning, never grounds to discard a paid reward.
pending = root / "src/main/java/net/execheinz/upgrader/economy/PendingRewardService.java"
replace_once(pending, "private static final int MAX_PENDING = 128;", "private static final int WARN_PENDING = 128;")
replace_once(pending,
    """        if (list.size() >= MAX_PENDING) {\n            Upgrader.LOGGER.error(\"Pending Musor Drop reward queue reached safety limit for {}\", player.getGameProfile().getName());\n            // Final best-effort world delivery. This path is only reachable after many prior delivery failures.\n            try { player.drop(stack.copy(), false); }\n            catch (RuntimeException ex) { Upgrader.LOGGER.error(\"Final reward world-drop fallback failed\", ex); }\n            return;\n        }\n""",
    """        if (list.size() >= WARN_PENDING) {\n            // This queue is populated only after an authoritative paid transaction. Never discard\n            // a purchased reward because a diagnostic threshold was reached; retain it durably and\n            // warn operators instead. Delivery is retried on the next login.\n            Upgrader.LOGGER.error(\"Pending Musor Drop reward queue is unusually large ({} entries) for {}\", list.size(), player.getGameProfile().getName());\n        }\n""")

# 3) GUI resize/re-init continuity + target-catalog performance.
screen = root / "src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java"
replace_once(screen,
    """public final class UpgraderScreen extends AbstractContainerScreen<UpgraderMenu> {\n    private MainTab tab;\n""",
    """public final class UpgraderScreen extends AbstractContainerScreen<UpgraderMenu> {\n    private static volatile List<ResourceLocation> cachedTargetCatalog = List.of();\n    private static volatile boolean targetCatalogCached;\n\n    private MainTab tab;\n""")
replace_once(screen,
    """    @Override\n    protected void init() {\n        super.init();\n        buildTargetCatalog();\n        buildCategories();\n""",
    """    @Override\n    protected void init() {\n        // Minecraft re-runs init() after window/GUI-scale changes. Preserve user context across a\n        // purely visual resize instead of resetting searches and the selected upgrade target.\n        String preservedTargetQuery = targetSearch == null ? \"\" : targetSearch.getValue();\n        String preservedCaseQuery = caseSearch == null ? \"\" : caseSearch.getValue();\n\n        super.init();\n        buildTargetCatalog();\n        buildCategories();\n""")
replace_once(screen,
    """        targetSearch.setMaxLength(48);\n        targetSearch.setBordered(false);\n        targetSearch.setHint(Component.literal(ModLanguage.tr(\"Поиск цели...\", \"Search target...\")));\n""",
    """        targetSearch.setMaxLength(48);\n        targetSearch.setBordered(false);\n        targetSearch.setHint(Component.literal(ModLanguage.tr(\"Поиск цели...\", \"Search target...\")));\n        targetSearch.setValue(preservedTargetQuery);\n""")
replace_once(screen,
    """        caseSearch.setMaxLength(48);\n        caseSearch.setBordered(false);\n        caseSearch.setHint(Component.literal(ModLanguage.tr(\"Поиск кейса...\", \"Search cases...\")));\n        caseSearch.setResponder(v -> rebuildCaseFilter());\n""",
    """        caseSearch.setMaxLength(48);\n        caseSearch.setBordered(false);\n        caseSearch.setHint(Component.literal(ModLanguage.tr(\"Поиск кейса...\", \"Search cases...\")));\n        caseSearch.setValue(preservedCaseQuery);\n        caseSearch.setResponder(v -> rebuildCaseFilter());\n""")
replace_once(screen,
    """    private void buildTargetCatalog() {\n        targetCatalog.clear();\n        for (Item item : ForgeRegistries.ITEMS.getValues()) {\n            try {\n                if (!ItemPolicyService.isUpgradeTargetEligible(item)) continue;\n                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);\n                if (id != null) targetCatalog.add(id);\n            } catch (RuntimeException ex) {\n                Upgrader.LOGGER.debug(\"Skipping invalid third-party target item\", ex);\n            }\n        }\n        targetCatalog.sort(Comparator.comparing(ResourceLocation::toString));\n        int diamond = targetCatalog.indexOf(new ResourceLocation(\"minecraft\", \"diamond\"));\n        targetCursor = Math.max(0, diamond);\n        if (!targetCatalog.isEmpty()) selectedTarget = targetCatalog.get(targetCursor);\n    }\n""",
    """    private void buildTargetCatalog() {\n        ResourceLocation previousTarget = selectedTarget;\n        if (!targetCatalogCached) {\n            synchronized (UpgraderScreen.class) {\n                if (!targetCatalogCached) {\n                    List<ResourceLocation> built = new ArrayList<>();\n                    for (Item item : ForgeRegistries.ITEMS.getValues()) {\n                        try {\n                            if (!ItemPolicyService.isUpgradeTargetEligible(item)) continue;\n                            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);\n                            if (id != null) built.add(id);\n                        } catch (RuntimeException ex) {\n                            Upgrader.LOGGER.debug(\"Skipping invalid third-party target item\", ex);\n                        }\n                    }\n                    built.sort(Comparator.comparing(ResourceLocation::toString));\n                    cachedTargetCatalog = List.copyOf(built);\n                    targetCatalogCached = true;\n                }\n            }\n        }\n\n        targetCatalog.clear();\n        targetCatalog.addAll(cachedTargetCatalog);\n        if (targetCatalog.isEmpty()) return;\n\n        int previous = targetCatalog.indexOf(previousTarget);\n        int diamond = targetCatalog.indexOf(new ResourceLocation(\"minecraft\", \"diamond\"));\n        targetCursor = previous >= 0 ? previous : Math.max(0, diamond);\n        selectedTarget = targetCatalog.get(targetCursor);\n    }\n""")

# Strict postconditions: if any of these fail, abort before compilation rather than producing a partial release.
network_text = network.read_text(encoding="utf-8")
pending_text = pending.read_text(encoding="utf-8")
screen_text = screen.read_text(encoding="utf-8")
assert network_text.count("NetworkDirection.PLAY_TO_SERVER") == 2
assert network_text.count("NetworkDirection.PLAY_TO_CLIENT") == 1
assert "MAX_PENDING" not in pending_text and "WARN_PENDING" in pending_text
assert "player.drop(stack.copy(), false)" not in pending_text.split("public static void queue", 1)[1].split("public static void flush", 1)[0]
assert "preservedTargetQuery" in screen_text and "preservedCaseQuery" in screen_text
assert "cachedTargetCatalog" in screen_text and "previousTarget" in screen_text
print("MUSOR_501_RUN9_PATCH_OK")
