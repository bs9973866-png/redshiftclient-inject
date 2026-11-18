package dev.redshift.client.ui;

import dev.redshift.client.hud.HudLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class HudEditorScreen extends Screen {
    private String selectedWidget;

    public HudEditorScreen() {
        super(Component.literal("REDSHIFT HUD EDITOR"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB8040008);

        graphics.fill(18, 18, width - 18, 72, 0xF20C0913);
        graphics.fill(18, 18, width - 18, 21, 0xFFFF355D);
        graphics.drawString(font, "REDSHIFT  /  HUD EDITOR", 34, 34, 0xFFFFFFFF);
        graphics.drawString(font, "DRAG ELEMENTS TO MOVE  •  ESC TO SAVE AND EXIT",
                34, 50, 0xFFAA9CAE);

        for (HudLayout.Widget widget : HudLayout.widgets()) {
            int x = HudLayout.x(widget.id(), width);
            int y = HudLayout.y(widget.id(), height);
            boolean selected = widget.id().equals(selectedWidget);
            boolean hovered = mouseX >= x && mouseX <= x + widget.width()
                    && mouseY >= y && mouseY <= y + widget.height();

            int fill = selected ? 0xEE3B1727 : 0xCC120D19;
            int border = selected || hovered ? 0xFFFF4568 : 0xAA5B4056;
            graphics.fill(x, y, x + widget.width(), y + widget.height(), fill);
            graphics.fill(x, y, x + widget.width(), y + 2, border);
            graphics.fill(x, y, x + 2, y + widget.height(), border);

            String label = widget.label();
            int labelX = x + Math.max(6, (widget.width() - font.width(label)) / 2);
            int labelY = y + Math.max(6, (widget.height() - 9) / 2);
            graphics.drawString(font, label, labelX, labelY, 0xFFF8F1F5);
        }

        graphics.fill(18, height - 46, width - 18, height - 18, 0xF20C0913);
        graphics.drawString(font, "SELECTED  /  " +
                        (selectedWidget == null ? "NONE" : selectedWidget.toUpperCase()),
                32, height - 36, 0xFFFF8BA2);
        graphics.drawString(font, "RESET POSITIONS  /  R",
                Math.max(250, width - 180), height - 36, 0xFFAA9CAE);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(click, doubled);
        }

        for (HudLayout.Widget widget : HudLayout.widgets()) {
            int x = HudLayout.x(widget.id(), width);
            int y = HudLayout.y(widget.id(), height);
            if (inside(click.x(), click.y(), x, y, x + widget.width(), y + widget.height())) {
                selectedWidget = widget.id();
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (selectedWidget == null || click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseDragged(click, deltaX, deltaY);
        }

        HudLayout.Widget widget = HudLayout.widgets().stream()
                .filter(candidate -> candidate.id().equals(selectedWidget))
                .findFirst()
                .orElse(null);
        if (widget == null) {
            return false;
        }

        int x = HudLayout.x(selectedWidget, width) + (int) Math.round(deltaX);
        int y = HudLayout.y(selectedWidget, height) + (int) Math.round(deltaY);
        HudLayout.move(selectedWidget, x, y, width, height);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && selectedWidget != null) {
            HudLayout.save();
            selectedWidget = null;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == GLFW.GLFW_KEY_R) {
            HudLayout.reset();
            selectedWidget = null;
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        HudLayout.save();
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static boolean inside(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }
}
