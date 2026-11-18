package dev.redshift.client.modules;

import dev.redshift.client.core.BooleanSetting;
import dev.redshift.client.core.ClientModule;
import dev.redshift.client.core.ModuleCategory;
import net.minecraft.client.Minecraft;

public final class ToggleSprintModule extends ClientModule {
    private final BooleanSetting pauseWhileUsingItem;

    public ToggleSprintModule() {
        super(
                "sprint_assist",
                "Sprint Assist",
                "Keeps sprint enabled while moving forward, unless sneaking.",
                ModuleCategory.MOVEMENT
        );
        pauseWhileUsingItem = addBooleanSetting(
                "pause_while_using_item",
                "Pause while using items",
                "Do not force sprint while eating, drawing a bow, or using another item.",
                true
        );
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.player == null || client.screen != null) {
            return;
        }

        if (pauseWhileUsingItem.isEnabled() && client.player.isUsingItem()) {
            return;
        }

        if (client.options.keyUp.isDown()
                && !client.options.keyShift.isDown()
                && !client.player.isPassenger()) {
            client.player.setSprinting(true);
        }
    }
}
