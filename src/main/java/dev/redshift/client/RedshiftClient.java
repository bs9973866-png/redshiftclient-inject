package dev.redshift.client;

import dev.redshift.client.core.ClientConfig;
import dev.redshift.client.core.ModuleManager;
import dev.redshift.client.hud.HudLayout;
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
    private boolean guiKeyWasDown;

    @Override
    public void onInitializeClient() {
        HudLayout.load();
        ModuleManager.initialize();

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("redshift", "client_hud"),
                RedshiftHud::render
        );

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ModuleManager.save();
            HudLayout.save();
        });

        System.out.println("[REDSHIFT] Client initialized for Minecraft 1.21.11");
    }

    private void onClientTick(Minecraft client) {
        ModuleManager.tick(client);

        if (client.screen instanceof ClickGuiScreen clickGui && clickGui.isCapturingGuiKey()) {
            guiKeyWasDown = false;
            return;
        }

        int guiKey = ClientConfig.guiKey();
        boolean guiKeyDown = GLFW.glfwGetKey(
                client.getWindow().handle(),
                guiKey
        ) == GLFW.GLFW_PRESS;

        if (guiKeyDown && !guiKeyWasDown) {
            if (client.screen instanceof ClickGuiScreen clickGui) {
                clickGui.closeGui();
            } else if (client.screen == null) {
                client.setScreen(new ClickGuiScreen());
            }
        }

        guiKeyWasDown = guiKeyDown;
    }
}
