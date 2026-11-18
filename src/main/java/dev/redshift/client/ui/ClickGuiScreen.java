package dev.redshift.client.ui;

import dev.redshift.client.core.BooleanSetting;
import dev.redshift.client.core.ClientConfig;
import dev.redshift.client.core.ClientModule;
import dev.redshift.client.core.ModuleCategory;
import dev.redshift.client.core.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 720;
    private static final int PANEL_HEIGHT = 410;

    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    private boolean settingsOpen;
    private boolean moduleSettingsOpen;
    private String settingsModuleId;
    private boolean waitingForGuiKey;
    private String waitingForModuleKey;
    private String searchQuery = "";
    private boolean searchFocused;

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

        int settingsRowY = navY + ModuleCategory.values().length * 48;
        if (settingsOpen) {
            graphics.fill(x + 12, settingsRowY - 7, x + 160, settingsRowY + 29, 0x66FF355D);
            graphics.fill(x + 12, settingsRowY - 7, x + 15, settingsRowY + 29, 0xFFFF355D);
        } else if (inside(mouseX, mouseY, x + 12, settingsRowY - 7, x + 160, settingsRowY + 29)) {
            graphics.fill(x + 12, settingsRowY - 7, x + 160, settingsRowY + 29, 0x33FFFFFF);
        }
        graphics.drawString(this.font, "Settings", x + 28, settingsRowY + 5,
                settingsOpen ? 0xFFFF4568 : 0xFFAA9CAE);

        int contentX = x + 180;
        if (settingsOpen) {
            renderSettings(graphics, mouseX, mouseY, x, y, contentX);
        } else if (moduleSettingsOpen) {
            renderModuleSettings(graphics, mouseX, mouseY, y, contentX);
        } else {
            graphics.drawString(this.font, selectedCategory.title().toUpperCase(), contentX, y + 77, 0xFFFFFFFF);
            graphics.drawString(this.font, "LEFT CLICK  /  TOGGLE   MIDDLE  /  OPTIONS   RIGHT  /  BIND", contentX, y + 94, 0xFF9C899F);

            int searchY = y + 108;
            graphics.fill(contentX, searchY, contentX + 506, searchY + 22, searchFocused ? 0xEE21121F : 0xCC15101C);
            graphics.fill(contentX, searchY, contentX + 506, searchY + 1, searchFocused ? 0xFFFF4568 : 0x553D2D44);
            String searchLabel = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
            graphics.drawString(this.font, searchLabel, contentX + 9, searchY + 7,
                    searchQuery.isEmpty() ? 0xFF766B7A : 0xFFF8F1F5);
            if (searchFocused) {
                int caretX = contentX + 9 + this.font.width(searchQuery);
                graphics.fill(caretX, searchY + 6, caretX + 1, searchY + 17, 0xFFFF5578);
            }

            List<ClientModule> modules = filteredModules();
            if (modules.isEmpty()) {
                graphics.drawString(this.font, searchQuery.isEmpty()
                                ? "No modules in this category yet."
                                : "No modules match your search.",
                        contentX, y + 153, 0xFF9C899F);
            }

            int cardWidth = 245;
            int cardHeight = 66;
            int gapX = 12;
            int gapY = 12;
            int cardsStartY = y + 140;

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
            String bindLabel = moduleKeyName(module);
            graphics.drawString(this.font, "KEY  " + bindLabel, cardX + 11, cardY + 49, 0xFFFF8BA2);

            String status = module.isEnabled() ? "ON" : "OFF";
            int statusColor = module.isEnabled() ? 0xFFFF5578 : 0xFF766B7A;
            graphics.fill(cardX + cardWidth - 43, cardY + 45, cardX + cardWidth - 11, cardY + 59,
                    module.isEnabled() ? 0x66FF355D : 0x553D3544);
            graphics.drawString(this.font, status, cardX + cardWidth - 36, cardY + 48, statusColor);
        }

        }

        graphics.fill(x + 12, y + PANEL_HEIGHT - 34, x + PANEL_WIDTH - 12, y + PANEL_HEIGHT - 33, 0x33FF5578);
        graphics.drawString(this.font, "REDSHIFT  •  FABRIC CLIENT  •  GUI KEY: " + guiKeyName(),
                x + 20, y + PANEL_HEIGHT - 23, 0xFF877A8D);
    }

    private void renderSettings(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY,
            int contentX
    ) {
        graphics.drawString(this.font, "CLIENT SETTINGS", contentX, panelY + 77, 0xFFFFFFFF);
        graphics.drawString(this.font, "INPUT  /  INTERFACE", contentX, panelY + 94, 0xFF9C899F);

        int cardX = contentX;
        int cardY = panelY + 124;
        int cardRight = panelX + PANEL_WIDTH - 22;
        graphics.fill(cardX, cardY, cardRight, cardY + 86, 0xCC15101C);
        graphics.fill(cardX, cardY, cardRight, cardY + 1, 0x66FF5578);
        graphics.drawString(this.font, "CLICKGUI KEYBIND", cardX + 14, cardY + 14, 0xFFF8F1F5);
        graphics.drawString(this.font, "Open or close the REDSHIFT interface.", cardX + 14, cardY + 31, 0xFFAA9CAE);
        graphics.drawString(this.font, "CURRENT  " + guiKeyName(), cardX + 14, cardY + 52, 0xFFFF8BA2);

        int buttonX = cardRight - 146;
        int buttonY = cardY + 25;
        boolean hovered = inside(mouseX, mouseY, buttonX, buttonY, buttonX + 132, buttonY + 32);
        graphics.fill(buttonX, buttonY, buttonX + 132, buttonY + 32,
                waitingForGuiKey ? 0xDD70233E : (hovered ? 0xCC542039 : 0xAA281522));
        graphics.fill(buttonX, buttonY, buttonX + 132, buttonY + 1, 0xFFFF4568);
        String buttonText = waitingForGuiKey ? "PRESS A KEY" : "CHANGE KEY";
        int textX = buttonX + (132 - this.font.width(buttonText)) / 2;
        graphics.drawString(this.font, buttonText, textX, buttonY + 12, 0xFFFFFFFF);

        if (waitingForGuiKey) {
            graphics.drawString(this.font, "Press any key to assign it. ESC cancels.",
                    contentX + 2, cardY + 103, 0xFFFF8BA2);
        } else {
            graphics.drawString(this.font, "Your keybind is stored in config/redshift-client.json.",
                    contentX + 2, cardY + 103, 0xFF9C899F);
        }
    }

    private void renderModuleSettings(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int panelY,
            int contentX
    ) {
        ClientModule module = ModuleManager.get(settingsModuleId);
        if (module == null) {
            graphics.drawString(this.font, "Module not found.", contentX, panelY + 90, 0xFF9C899F);
            return;
        }

        graphics.drawString(this.font, module.name().toUpperCase(), contentX, panelY + 77, 0xFFFFFFFF);
        graphics.drawString(this.font, "MODULE OPTIONS", contentX, panelY + 94, 0xFF9C899F);

        int backY = panelY + 108;
        boolean backHovered = inside(mouseX, mouseY, contentX, backY, contentX + 92, backY + 23);
        graphics.fill(contentX, backY, contentX + 92, backY + 23,
                backHovered ? 0xCC542039 : 0xAA281522);
        graphics.fill(contentX, backY, contentX + 92, backY + 1, 0xFFFF4568);
        graphics.drawString(this.font, "<  BACK", contentX + 22, backY + 8, 0xFFF8F1F5);

        if (module.booleanSettings().isEmpty()) {
            graphics.drawString(this.font, "This module has no configurable options yet.",
                    contentX, panelY + 155, 0xFF9C899F);
            return;
        }

        int rowY = panelY + 145;
        for (BooleanSetting setting : module.booleanSettings()) {
            int rowBottom = rowY + 54;
            graphics.fill(contentX, rowY, contentX + 506, rowBottom, 0xCC15101C);
            graphics.fill(contentX, rowY, contentX + 506, rowY + 1, 0x553D2D44);

            graphics.drawString(this.font, setting.name(), contentX + 12, rowY + 10, 0xFFF8F1F5);
            graphics.drawString(this.font, setting.description(), contentX + 12, rowY + 29, 0xFFAA9CAE);

            String value = setting.isEnabled() ? "ON" : "OFF";
            int valueColor = setting.isEnabled() ? 0xFFFF5578 : 0xFF766B7A;
            int toggleX = contentX + 450;
            graphics.fill(toggleX, rowY + 15, toggleX + 42, rowY + 38,
                    setting.isEnabled() ? 0xAA542039 : 0xAA302A35);
            graphics.fill(toggleX, rowY + 15, toggleX + 42, rowY + 16, 0xFFFF4568);
            graphics.drawString(this.font, value, toggleX + 10, rowY + 23, valueColor);
            rowY += 66;
        }
    }

    private static String guiKeyName() {
        int key = ClientConfig.guiKey();
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) return "RIGHT SHIFT";
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT) return "LEFT SHIFT";
        if (key == GLFW.GLFW_KEY_ESCAPE) return "ESCAPE";
        if (key == GLFW.GLFW_KEY_SPACE) return "SPACE";
        if (key == GLFW.GLFW_KEY_ENTER) return "ENTER";
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) ('A' + key - GLFW.GLFW_KEY_A));
        }
        String name = GLFW.glfwGetKeyName(key, 0);
        return name == null ? "KEY " + key : name.toUpperCase(java.util.Locale.ROOT);
    }

    public boolean isCapturingGuiKey() {
        return waitingForGuiKey || waitingForModuleKey != null || searchFocused;
    }

    private static String moduleKeyName(ClientModule module) {
        int key = module.keybind();
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) return "RSHIFT";
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT) return "LSHIFT";
        if (key == GLFW.GLFW_KEY_SPACE) return "SPACE";
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) ('A' + key - GLFW.GLFW_KEY_A));
        }
        String name = GLFW.glfwGetKeyName(key, 0);
        return name == null ? "KEY " + key : name.toUpperCase(java.util.Locale.ROOT);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (searchFocused) {
            if (input.key() == GLFW.GLFW_KEY_ESCAPE || input.key() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
            if (input.key() == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                int end = searchQuery.offsetByCodePoints(searchQuery.length(), -1);
                searchQuery = searchQuery.substring(0, end);
                return true;
            }
            return true;
        }

        if (waitingForModuleKey != null) {
            int key = input.key();
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                waitingForModuleKey = null;
                return true;
            }
            if (key != GLFW.GLFW_KEY_UNKNOWN) {
                ClientModule module = ModuleManager.get(waitingForModuleKey);
                if (module != null) {
                    module.setKeybind(key);
                    ModuleManager.save();
                }
            }
            waitingForModuleKey = null;
            return true;
        }

        if (waitingForGuiKey) {
            int key = input.key();
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                waitingForGuiKey = false;
                return true;
            }
            if (key != GLFW.GLFW_KEY_UNKNOWN) {
                ClientConfig.setGuiKey(key);
            }
            waitingForGuiKey = false;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (!searchFocused) {
            return super.charTyped(input);
        }

        String typed = input.codepointAsString();
        if (typed != null && !typed.isEmpty() && input.isAllowedChatCharacter()
                && searchQuery.length() < 32) {
            searchQuery += typed;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() != 0 && click.button() != 1 && click.button() != 2) {
            return super.mouseClicked(click, doubled);
        }

        double mouseX = click.x();
        double mouseY = click.y();
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
                settingsOpen = false;
                moduleSettingsOpen = false;
                searchFocused = false;
                return true;
            }
        }

        int settingsRowY = navY + ModuleCategory.values().length * 48;
        if (inside(mouseX, mouseY, x + 12, settingsRowY - 7, x + 160, settingsRowY + 29)) {
            settingsOpen = true;
            moduleSettingsOpen = false;
            searchFocused = false;
            waitingForGuiKey = false;
            return true;
        }

        int contentX = x + 180;
        if (settingsOpen) {
            searchFocused = false;
            int cardRight = x + PANEL_WIDTH - 22;
            int buttonX = cardRight - 146;
            int buttonY = y + 124 + 25;
            if (inside(mouseX, mouseY, buttonX, buttonY, buttonX + 132, buttonY + 32)) {
                waitingForGuiKey = true;
            }
            return true;
        }

        if (moduleSettingsOpen) {
            if (inside(mouseX, mouseY, contentX, y + 108, contentX + 92, y + 131)) {
                moduleSettingsOpen = false;
                return true;
            }

            ClientModule module = ModuleManager.get(settingsModuleId);
            if (module != null) {
                int rowY = y + 145;
                for (BooleanSetting setting : module.booleanSettings()) {
                    if (inside(mouseX, mouseY, contentX, rowY, contentX + 506, rowY + 54)) {
                        setting.toggle();
                        ModuleManager.save();
                        return true;
                    }
                    rowY += 66;
                }
            }
            return true;
        }

        int searchY = y + 108;
        if (inside(mouseX, mouseY, contentX, searchY, contentX + 506, searchY + 22)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        int cardWidth = 245;
        int cardHeight = 66;
        int gapX = 12;
        int gapY = 12;
        int cardsStartY = y + 140;
        List<ClientModule> modules = filteredModules();

        if (click.button() == 2) {
            for (int index = 0; index < modules.size(); index++) {
                int column = index % 2;
                int row = index / 2;
                int cardX = contentX + column * (cardWidth + gapX);
                int cardY = cardsStartY + row * (cardHeight + gapY);
                if (inside(mouseX, mouseY, cardX, cardY, cardX + cardWidth, cardY + cardHeight)) {
                    settingsModuleId = modules.get(index).id();
                    moduleSettingsOpen = true;
                    return true;
                }
            }
            return true;
        }

        if (click.button() == 1) {
            for (int index = 0; index < modules.size(); index++) {
                int column = index % 2;
                int row = index / 2;
                int cardX = contentX + column * (cardWidth + gapX);
                int cardY = cardsStartY + row * (cardHeight + gapY);
                if (inside(mouseX, mouseY, cardX, cardY, cardX + cardWidth, cardY + cardHeight)) {
                    waitingForGuiKey = false;
                    waitingForModuleKey = modules.get(index).id();
                    return true;
                }
            }
            return super.mouseClicked(click, doubled);
        }

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

        return super.mouseClicked(click, doubled);
    }

    private List<ClientModule> filteredModules() {
        String query = searchQuery.trim().toLowerCase(java.util.Locale.ROOT);
        return ModuleManager.byCategory(selectedCategory).stream()
                .filter(module -> query.isEmpty()
                        || module.name().toLowerCase(java.util.Locale.ROOT).contains(query)
                        || module.description().toLowerCase(java.util.Locale.ROOT).contains(query)
                        || module.id().toLowerCase(java.util.Locale.ROOT).contains(query))
                .toList();
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
