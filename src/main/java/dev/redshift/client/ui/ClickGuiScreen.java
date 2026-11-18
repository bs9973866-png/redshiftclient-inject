package dev.redshift.client.ui;

import dev.redshift.client.core.ClientModule;
import dev.redshift.client.core.ModuleCategory;
import dev.redshift.client.core.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 720;
    private static final int PANEL_HEIGHT = 410;

    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;

    public ClickGuiScreen() {
        super(Component.literal("REDSHIFT CLIENT"));
    }

    private int panelX() {
        return Math.max(12, (this.width - PANEL_WIDTH) / 2);
    }

    private int panelY() {
        return Math.max(12, (this.height - PANEL_HEIGHT) / 2);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0x99040008);

        int x = panelX();
        int y = panelY();

        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xF20C0913);
        graphics.fill(x, y, x + PANEL_WIDTH, y + 3, 0xFFFF355D);
        graphics.fill(x, y + 3, x + PANEL_WIDTH, y + 58, 0xF8171020);
        graphics.fill(x + 1, y + 58, x + PANEL_WIDTH - 1, y + 59, 0x55FF5578);

        graphics.drawString(this.font, "REDSHIFT", x + 22, y + 17, 0xFFFFFFFF);
        graphics.drawString(this.font, "CLIENT  /  1.21.11", x + 111, y + 19, 0xFFFF5578);
        graphics.drawString(this.font, "RIGHT SHIFT  •  CLOSE", x + PANEL_WIDTH - 171, y + 19, 0xFF9C899F);

        int closeX = x + PANEL_WIDTH - 30;
        if (inside(mouseX, mouseY, closeX, y + 9, closeX + 18, y + 31)) {
            graphics.fill(closeX - 5, y + 5, closeX + 22, y + 36, 0x66FF355D);
        }
        graphics.drawString(this.font, "×", closeX + 5, y + 14, 0xFFFFFFFF);

        graphics.fill(x + 12, y + 72, x + 160, y + PANEL_HEIGHT - 18, 0xAA100B18);
        int navY = y + 88;
        for (ModuleCategory category : ModuleCategory.values()) {
            boolean selected = category == selectedCategory;
            int rowY = navY + category.ordinal() * 48;
            int color = selected ? 0xFFFF4568 : 0xFFAA9CAE;

            if (selected) {
                graphics.fill(x + 12, rowY - 7, x + 160, rowY + 29, 0x66FF355D);
                graphics.fill(x + 12, rowY - 7, x + 15, rowY + 29, 0xFFFF355D);
            } else if (inside(mouseX, mouseY, x + 12, rowY - 7, x + 160, rowY + 29)) {
                graphics.fill(x + 12, rowY - 7, x + 160, rowY + 29, 0x33FFFFFF);
            }

            graphics.drawString(this.font, category.title(), x + 28, rowY + 5, color);
        }

        int contentX = x + 180;
        graphics.drawString(this.font, selectedCategory.title().toUpperCase(), contentX, y + 77, 0xFFFFFFFF);
        graphics.drawString(this.font, "MODULES  /  CLICK A CARD TO TOGGLE", contentX, y + 94, 0xFF9C899F);

        List<ClientModule> modules = ModuleManager.byCategory(selectedCategory);
        if (modules.isEmpty()) {
            graphics.drawString(this.font, "No modules in this category yet.", contentX, y + 137, 0xFF9C899F);
        }

        int cardWidth = 245;
        int cardHeight = 66;
        int gapX = 12;
        int gapY = 12;
        int cardsStartY = y + 124;

        for (int index = 0; index < modules.size(); index++) {
            ClientModule module = modules.get(index);
            int column = index % 2;
            int row = index / 2;
            int cardX = contentX + column * (cardWidth + gapX);
            int cardY = cardsStartY + row * (cardHeight + gapY);
            boolean hovered = inside(mouseX, mouseY, cardX, cardY, cardX + cardWidth, cardY + cardHeight);
            int fill = module.isEnabled() ? 0xDD21111F : 0xCC15101C;

            graphics.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, fill);
            graphics.fill(cardX, cardY, cardX + cardWidth, cardY + 1, module.isEnabled() ? 0xFFFF4568 : 0x553D2D44);
            if (hovered) {
                graphics.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, 0x22FFFFFF);
            }

            graphics.drawString(this.font, module.name(), cardX + 11, cardY + 11, 0xFFF8F1F5);
            graphics.drawString(this.font, module.description(), cardX + 11, cardY + 29, 0xFFAA9CAE);

            String status = module.isEnabled() ? "ON" : "OFF";
            int statusColor = module.isEnabled() ? 0xFFFF5578 : 0xFF766B7A;
            graphics.fill(cardX + cardWidth - 43, cardY + 45, cardX + cardWidth - 11, cardY + 59,
                    module.isEnabled() ? 0x66FF355D : 0x553D3544);
            graphics.drawString(this.font, status, cardX + cardWidth - 36, cardY + 48, statusColor);
        }

        graphics.fill(x + 12, y + PANEL_HEIGHT - 34, x + PANEL_WIDTH - 12, y + PANEL_HEIGHT - 33, 0x33FF5578);
        graphics.drawString(this.font, "REDSHIFT  •  FABRIC CLIENT  •  SETTINGS SAVE AUTOMATICALLY",
                x + 20, y + PANEL_HEIGHT - 23, 0xFF877A8D);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int x = panelX();
        int y = panelY();
        int closeX = x + PANEL_WIDTH - 30;

        if (inside(mouseX, mouseY, closeX - 5, y + 5, closeX + 22, y + 36)) {
            closeGui();
            return true;
        }

        int navY = y + 88;
        for (ModuleCategory category : ModuleCategory.values()) {
            int rowY = navY + category.ordinal() * 48;
            if (inside(mouseX, mouseY, x + 12, rowY - 7, x + 160, rowY + 29)) {
                selectedCategory = category;
                return true;
            }
        }

        int contentX = x + 180;
        int cardWidth = 245;
        int cardHeight = 66;
        int gapX = 12;
        int gapY = 12;
        int cardsStartY = y + 124;
        List<ClientModule> modules = ModuleManager.byCategory(selectedCategory);

        for (int index = 0; index < modules.size(); index++) {
            int column = index % 2;
            int row = index / 2;
            int cardX = contentX + column * (cardWidth + gapX);
            int cardY = cardsStartY + row * (cardHeight + gapY);

            if (inside(mouseX, mouseY, cardX, cardY, cardX + cardWidth, cardY + cardHeight)) {
                modules.get(index).toggle();
                ModuleManager.save();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean inside(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    public void closeGui() {
        onClose();
    }

    @Override
    public void onClose() {
        ModuleManager.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
