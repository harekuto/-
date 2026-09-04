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

# 5.0.2 persistence/modded-item compatibility hardening:
# Never destroy an already-paid pending reward merely because its ItemStack cannot be decoded
# during this login. This can happen when a third-party mod is temporarily removed, disabled,
# renamed, or fails to register its item. Preserve the original opaque NBT so a later login can
# recover the reward when the registry becomes available again.
pending = root / "src/main/java/net/execheinz/upgrader/economy/PendingRewardService.java"
replace_once(
    pending,
    """        for (int i = 0; i < list.size(); i++) {\n            ItemStack stack;\n            try { stack = ItemStack.of(list.getCompound(i)); }\n            catch (RuntimeException ex) {\n                Upgrader.LOGGER.error(\"Unreadable pending Musor Drop reward entry was discarded\", ex);\n                continue;\n            }\n            if (stack.isEmpty()) continue;\n            if (!deliver(player, stack)) {\n                CompoundTag encoded = new CompoundTag();\n                stack.save(encoded);\n                keep.add(encoded);\n            }\n        }\n""",
    """        for (int i = 0; i < list.size(); i++) {\n            // Keep the exact encoded entry until we have positively reconstructed a usable stack.\n            // ItemStack.of may return EMPTY when a modded registry id is temporarily unavailable;\n            // deleting that entry would permanently destroy an already-paid reward.\n            CompoundTag original = list.getCompound(i).copy();\n            ItemStack stack;\n            try { stack = ItemStack.of(original); }\n            catch (RuntimeException ex) {\n                keep.add(original);\n                Upgrader.LOGGER.error(\"Unreadable pending Musor Drop reward entry was preserved for a later retry\", ex);\n                continue;\n            }\n            if (stack.isEmpty()) {\n                keep.add(original);\n                Upgrader.LOGGER.warn(\"Pending Musor Drop reward could not be resolved and was preserved for a later retry\");\n                continue;\n            }\n            if (!deliver(player, stack)) {\n                CompoundTag encoded = new CompoundTag();\n                stack.save(encoded);\n                keep.add(encoded);\n            }\n        }\n"""
)

text = pending.read_text(encoding="utf-8")
flush = text.split("public static void flush", 1)[1].split("/** Mutates stack", 1)[0]
assert "Unreadable pending Musor Drop reward entry was discarded" not in flush
assert "CompoundTag original = list.getCompound(i).copy();" in flush
assert "keep.add(original);" in flush
assert "stack.isEmpty()" in flush and "preserved for a later retry" in flush
assert flush.count("keep.add(original);") == 2
print("MUSOR_502_RUN10_PATCH_OK")
