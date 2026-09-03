from pathlib import Path
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else "musor-drop")
screen = root / "src/main/java/net/execheinz/upgrader/client/screen/UpgraderScreen.java"
text = screen.read_text(encoding="utf-8")


def once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"UI 4.8.1 polish anchor {label!r} expected once, found {count}")
    text = text.replace(old, new, 1)


def replace_method(signature: str, replacement: str) -> None:
    global text
    start = text.find(signature)
    if start < 0:
        raise SystemExit(f"UI 4.8.1 polish method not found: {signature}")
    brace = text.find("{", start)
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
        raise SystemExit(f"UI 4.8.1 polish unbalanced method: {signature}")
    text = text[:start] + replacement.rstrip() + text[end:]


once(
    '        upgradeButton = addRenderableWidget(button(110, 119, 100, 18, ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE"),\n',
    '        upgradeButton = addRenderableWidget(button(110, 126, 100, 18, ModLanguage.tr("УЛУЧШИТЬ", "UPGRADE"),\n',
    "upgrade action vertical separation",
)
once(
    '        centeredScaled(g, ModLanguage.tr("сервер решает результат", "server resolves result"), leftPos + 160, y + 69, 0.42F, MusorTheme.MUTED);\n',
    '',
    "remove text behind upgrade action",
)

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
        CaseLayout.Rect indicator = CaseLayout.selectedPanel();
        MusorTheme.inset(g, leftPos + indicator.x(), topPos + indicator.y(), indicator.w(), indicator.h());
        centeredScaled(g, (page + 1) + "/" + pages,
            leftPos + indicator.x() + indicator.w() / 2, topPos + indicator.y() + 5,
            0.48F, MusorTheme.MUTED);

        MusorTheme.separator(g, leftPos + 12, topPos + 160, leftPos + imageWidth - 12);
    }''')

screen.write_text(text, encoding="utf-8")
print("UI_481_POLISH_OK", screen)
