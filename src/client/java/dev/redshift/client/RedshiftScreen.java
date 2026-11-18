package dev.redshift.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class RedshiftScreen extends Screen {
    private static final int CRIMSON = 0xFFFF4058;
    private static final List<String> CATEGORIES = List.of(
            "Combat", "Movement", "Visuals", "Player", "World", "Render", "HUD", "Misc"
    );
    private static final List<String> MODULES = List.of(
            "Module Manager", "HUD Editor", "Theme Studio", "Profiles"
    );

    private int selectedCategory = 2;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public RedshiftScreen() {
        super(Component.literal("REDSHIFT"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(760, width - 36);
        panelHeight = Math.min(430, height - 36);
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        // Lightweight translucent composition: layered fills and edge highlights.
        // No framebuffer blur is used, keeping the first UI milestone inexpensive.
        g.fill(0, 0, width, height, 0x9905000B);
        glass(g, panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight,
                0xE6100B19, 0xFF713044);
        glass(g, panelLeft + 8, panelTop + 8, panelLeft + 158, panelTop + panelHeight - 8,
                0xD90D0916, CRIMSON);
        glass(g, panelLeft + 166, panelTop + 8, panelLeft + panelWidth - 8, panelTop + 58,
                0xC51B1225, CRIMSON);

        // Header identity and status.
        g.drawString(font, "REDSHIFT", panelLeft + 22, panelTop + 22, 0xFFFFF3F6, true);
        g.drawString(font, "INJECT CLIENT  /  1.21.11", panelLeft + 22, panelTop + 37,
                0xFFB9A6B7, false);
        g.drawString(font, "LIQUID GLASS", panelLeft + panelWidth - 112, panelTop + 28,
                0xFFFF8292, true);

        // Sidebar categories.
        int categoryY = panelTop + 78;
        for (int i = 0; i < CATEGORIES.size(); i++) {
            int y = categoryY + i * 36;
            boolean active = i == selectedCategory;
            if (active) {
                glass(g, panelLeft + 16, y - 5, panelLeft + 150, y + 24,
                        0xB63B1727, CRIMSON);
                g.fill(panelLeft + 16, y - 5, panelLeft + 18, y + 24, CRIMSON);
            }
            g.drawString(font, CATEGORIES.get(i), panelLeft + 28, y + 4,
                    active ? 0xFFFFE8ED : 0xFFB7AABD, active);
        }

        // Main content.
        int contentX = panelLeft + 184;
        int contentY = panelTop + 82;
        String category = CATEGORIES.get(selectedCategory);
        g.drawString(font, category, contentX, contentY, 0xFFFFF3F6, true);
        g.drawString(font, "A clean foundation for your custom client.", contentX,
                contentY + 16, 0xFFAA9BAD, false);

        int cardY = contentY + 48;
        int cardWidth = Math.max(120, (panelWidth - 222) / 2);
        for (int i = 0; i < MODULES.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int x = contentX + col * (cardWidth + 12);
            int y = cardY + row * 76;
            glass(g, x, y, x + cardWidth, y + 62, 0xB51B1427, 0xFF9B3B58);
            g.fill(x + 10, y + 10, x + 12, y + 52, 0xCCFF526B);
            g.drawString(font, MODULES.get(i), x + 20, y + 15, 0xFFF8EEF5, true);
            g.drawString(font, "Coming in the next milestone", x + 20, y + 34,
                    0xFFAA9BAD, false);
        }

        // Footer.
        g.drawString(font, "RIGHT SHIFT  •  CLOSE MENU", panelLeft + 20,
                panelTop + panelHeight - 24, 0xFF95869B, false);
        g.drawString(font, "BUILD 0.1.0", panelLeft + panelWidth - 82,
                panelTop + panelHeight - 24, 0xFF95869B, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private static void glass(GuiGraphics g, int left, int top, int right, int bottom,
                              int fill, int accent) {
        g.fill(left, top, right, bottom, fill);
        g.fill(left + 1, top + 1, right - 1, top + 2, 0x24FFFFFF);
        g.fill(left + 1, top + 1, left + 2, bottom - 1, 0x18FFFFFF);
        g.fill(left, top, right, top + 1, 0x38FFFFFF);
        g.fill(left, bottom - 1, right, bottom, 0x553A243D);
        g.fill(left, top, left + 1, bottom, 0x30FFFFFF);
        g.fill(right - 1, top, right, bottom, 0x335E425E);
        g.fill(left + 2, top + 3, left + 3, bottom - 3,
                (0x44 << 24) | (accent & 0x00FFFFFF));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int categoryY = panelTop + 78;
            for (int i = 0; i < CATEGORIES.size(); i++) {
                int y = categoryY + i * 36;
                if (mouseX >= panelLeft + 12 && mouseX <= panelLeft + 154
                        && mouseY >= y - 7 && mouseY <= y + 26) {
                    selectedCategory = i;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
