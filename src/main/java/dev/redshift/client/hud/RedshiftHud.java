package dev.redshift.client.hud;

import dev.redshift.client.core.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

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

        if (ModuleManager.isEnabled("keystrokes")) {
            renderKeystrokes(graphics, client);
        }

        if (ModuleManager.isEnabled("armor_status")) {
            renderArmor(graphics, client);
        }

        if (ModuleManager.isEnabled("held_item")) {
            renderHeldItem(graphics, client);
        }
    }

    private static void renderKeystrokes(GuiGraphics graphics, Minecraft client) {
        int baseX = graphics.guiWidth() - 82;
        int baseY = graphics.guiHeight() - 104;
        drawKey(graphics, client, "W", baseX + 24, baseY, client.options.keyUp.isDown());
        drawKey(graphics, client, "A", baseX, baseY + 24, client.options.keyLeft.isDown());
        drawKey(graphics, client, "S", baseX + 24, baseY + 24, client.options.keyDown.isDown());
        drawKey(graphics, client, "D", baseX + 48, baseY + 24, client.options.keyRight.isDown());
        drawKey(graphics, client, "SH", baseX, baseY + 48, client.options.keyShift.isDown());
        drawKey(graphics, client, "SP", baseX + 24, baseY + 48, client.options.keyJump.isDown());
    }

    private static void drawKey(
            GuiGraphics graphics,
            Minecraft client,
            String label,
            int x,
            int y,
            boolean pressed
    ) {
        int background = pressed ? 0xE84D152B : 0xCC0B0812;
        int border = pressed ? 0xFFFF4568 : 0x663D2D44;
        graphics.fill(x, y, x + 22, y + 20, background);
        graphics.fill(x, y, x + 22, y + 1, border);
        int textX = x + (22 - client.font.width(label)) / 2;
        graphics.drawString(client.font, label, textX, y + 6, 0xFFF8F1F5);
    }

    private static void renderHeldItem(GuiGraphics graphics, Minecraft client) {
        ItemStack stack = client.player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }

        int x = 10;
        int y = graphics.guiHeight() - 42;
        int width = Math.max(116, client.font.width(stack.getHoverName()) + 42);
        graphics.fill(x, y, x + width, y + 30, 0xCC0B0812);
        graphics.fill(x, y, x + 2, y + 30, 0xFFFF4568);
        graphics.renderItem(stack, x + 6, y + 6);
        graphics.drawString(client.font, stack.getHoverName(), x + 29, y + 7, 0xFFF8F1F5);
        if (stack.isDamageableItem()) {
            int maxDamage = stack.getMaxDamage();
            int remaining = Math.max(0, maxDamage - stack.getDamageValue());
            String durability = remaining + " / " + maxDamage;
            graphics.drawString(client.font, durability, x + 29, y + 18, 0xFFFF8BA2);
        }
    }

    private static void renderArmor(GuiGraphics graphics, Minecraft client) {
        ItemStack[] armor = {
                client.player.getItemBySlot(EquipmentSlot.HEAD),
                client.player.getItemBySlot(EquipmentSlot.CHEST),
                client.player.getItemBySlot(EquipmentSlot.LEGS),
                client.player.getItemBySlot(EquipmentSlot.FEET)
        };

        boolean hasArmor = false;
        for (ItemStack stack : armor) {
            if (!stack.isEmpty()) {
                hasArmor = true;
                break;
            }
        }
        if (!hasArmor) {
            return;
        }

        int slotSize = 20;
        int totalWidth = armor.length * slotSize + 8;
        int startX = (graphics.guiWidth() - totalWidth) / 2;
        int y = graphics.guiHeight() - 48;
        graphics.fill(startX - 4, y - 4, startX + totalWidth - 4, y + 22, 0xCC0B0812);

        for (int index = 0; index < armor.length; index++) {
            ItemStack stack = armor[index];
            int x = startX + index * slotSize;
            graphics.fill(x + 1, y + 1, x + 19, y + 19, 0x77302431);
            if (stack.isEmpty()) {
                continue;
            }

            graphics.renderItem(stack, x + 1, y + 1);
            if (stack.isDamageableItem()) {
                int maxDamage = stack.getMaxDamage();
                int remaining = Math.max(0, maxDamage - stack.getDamageValue());
                int width = maxDamage == 0 ? 0 : Math.round(13.0F * remaining / maxDamage);
                graphics.fill(x + 3, y + 17, x + 16, y + 19, 0xFF39232B);
                if (width > 0) {
                    graphics.fill(x + 3, y + 17, x + 3 + width, y + 19, 0xFFFF4568);
                }
            }
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
