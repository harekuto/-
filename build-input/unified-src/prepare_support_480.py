from pathlib import Path
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else "musor-drop")
screen = root / "src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java"
text = screen.read_text(encoding="utf-8")

# apply_support_page.py was authored against the pre-4.7.1 visibility method. Remove the
# later validity refresh only in the temporary build workspace; apply_ui_480 restores the
# complete final method after Support has been injected.
sig = "    private void updateWidgetVisibility()"
start = text.find(sig)
if start < 0:
    raise SystemExit("updateWidgetVisibility not found")
brace = text.find("{", start)
depth = 0
end = None
for i in range(brace, len(text)):
    if text[i] == "{":
        depth += 1
    elif text[i] == "}":
        depth -= 1
        if depth == 0:
            end = i + 1
            break
if end is None:
    raise SystemExit("updateWidgetVisibility unbalanced")
method = text[start:end]
needle = "        updateUpgradeButtonState();\n"
count = method.count(needle)
if count == 1:
    method = method.replace(needle, "", 1)
elif count != 0:
    raise SystemExit(f"unexpected updateUpgradeButtonState count in visibility: {count}")
text = text[:start] + method + text[end:]
screen.write_text(text, encoding="utf-8")

# Keep the production workflow deterministic without duplicating the entire reconstruction
# job for a one-file polish pass. The normal 4.8 finalizer executes first; this explicit,
# versioned finalizer runs immediately afterwards and validates its own anchors.
ui480 = Path(__file__).with_name("apply_ui_480.py")
ui481 = Path(__file__).with_name("apply_ui_481.py")
if not ui481.is_file():
    raise SystemExit("apply_ui_481.py missing")
ui_text = ui480.read_text(encoding="utf-8")
hook_marker = "# UI_481_FINALIZER_HOOK"
if hook_marker not in ui_text:
    hook = '''\n\n# UI_481_FINALIZER_HOOK\n_finalizer = Path(__file__).with_name("apply_ui_481.py")\nexec(compile(_finalizer.read_text(encoding="utf-8"), str(_finalizer), "exec"))\n'''
    ui480.write_text(ui_text.rstrip() + hook, encoding="utf-8")

print("SUPPORT_480_PREP_OK", screen)
print("UI_481_FINALIZER_WIRED", ui480)
