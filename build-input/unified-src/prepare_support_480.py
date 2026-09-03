from pathlib import Path
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else "musor-drop")
screen = root / "src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java"
text = screen.read_text(encoding="utf-8")

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
print("SUPPORT_480_PREP_OK", screen)
