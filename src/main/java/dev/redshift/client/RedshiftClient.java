package dev.redshift.client;

import dev.redshift.client.core.ModuleManager;
import dev.redshift.client.hud.RedshiftHud;
import dev.redshift.client.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class RedshiftClient implements ClientModInitializer {
    private boolean rightShiftWasDown;

    @Override
    public void onInitializeClient() {
        ModuleManager.initialize();

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("redshift", "client_hud"),
                RedshiftHud::render
        );

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ModuleManager.save());

        System.out.println("[REDSHIFT] Client initialized for Minecraft 1.21.11");
    }

    private void onClientTick(Minecraft client) {
        ModuleManager.tick(client);

        boolean rightShiftDown = GLFW.glfwGetKey(
                client.getWindow().handle(),
                GLFW.GLFW_KEY_RIGHT_SHIFT
        ) == GLFW.GLFW_PRESS;

        if (rightShiftDown && !rightShiftWasDown) {
            if (client.screen instanceof ClickGuiScreen clickGui) {
                clickGui.closeGui();
            } else if (client.screen == null) {
                client.setScreen(new ClickGuiScreen());
            }
        }

        rightShiftWasDown = rightShiftDown;
    }
}
