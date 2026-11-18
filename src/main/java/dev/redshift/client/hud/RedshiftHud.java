package dev.redshift.client.hud;

import dev.redshift.client.core.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;

public final class RedshiftHud {
    private RedshiftHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }

        int x = 10;
        int y = 10;

        if (ModuleManager.isEnabled("watermark")) {
            graphics.fill(x, y, x + 124, y + 25, 0xCC0B0812);
            graphics.fill(x, y, x + 3, y + 25, 0xFFFF355D);
            graphics.drawString(client.font, "REDSHIFT", x + 10, y + 8, 0xFFFFFFFF);
            y += 31;
        }

        if (ModuleManager.isEnabled("fps_counter")) {
            String fps = "FPS  " + client.getFps();
            drawInfoCard(graphics, client, x, y, fps);
            y += 23;
        }

        if (ModuleManager.isEnabled("coordinates")) {
            String coordinates = String.format(
                    java.util.Locale.ROOT,
                    "XYZ  %d  %d  %d",
                    client.player.getBlockX(),
                    client.player.getBlockY(),
                    client.player.getBlockZ()
            );
            drawInfoCard(graphics, client, x, y, coordinates);
        }

        if (ModuleManager.isEnabled("attack_cooldown")) {
            float progress = client.player.getAttackStrengthScale(0.0F);
            int barX = graphics.guiWidth() / 2 - 36;
            int barY = graphics.guiHeight() / 2 + 18;
            graphics.fill(barX, barY, barX + 72, barY + 5, 0xA9000000);
            graphics.fill(barX, barY, barX + Math.round(72 * progress), barY + 5, 0xFFFF355D);
        }

        if (ModuleManager.isEnabled("crimson_crosshair")) {
            int centerX = graphics.guiWidth() / 2;
            int centerY = graphics.guiHeight() / 2;
            graphics.fill(centerX - 1, centerY - 7, centerX + 1, centerY - 3, 0xFFFF355D);
            graphics.fill(centerX - 1, centerY + 3, centerX + 1, centerY + 7, 0xFFFF355D);
            graphics.fill(centerX - 7, centerY - 1, centerX - 3, centerY + 1, 0xFFFF355D);
            graphics.fill(centerX + 3, centerY - 1, centerX + 7, centerY + 1, 0xFFFF355D);
        }

        if (ModuleManager.isEnabled("world_clock") && client.level != null) {
            long dayTime = Math.floorMod(client.level.getDayTime(), 24000L);
            long hours = (dayTime / 1000L + 6L) % 24L;
            long minutes = (dayTime % 1000L) * 60L / 1000L;
            drawInfoCard(
                    graphics,
                    client,
                    graphics.guiWidth() - 98,
                    10,
                    String.format(java.util.Locale.ROOT, "TIME  %02d:%02d", hours, minutes)
            );
        }
    }

    private static void drawInfoCard(
            GuiGraphics graphics,
            Minecraft client,
            int x,
            int y,
            String text
    ) {
        int width = client.font.width(text) + 16;
        graphics.fill(x, y, x + width, y + 20, 0xCC0B0812);
        graphics.fill(x, y, x + 2, y + 20, 0xFFFF355D);
        graphics.drawString(client.font, text, x + 8, y + 6, 0xFFF2EAF0);
    }
}
