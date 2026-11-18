package dev.redshift.client.modules;

import dev.redshift.client.core.ClientModule;
import dev.redshift.client.core.ModuleCategory;
import net.minecraft.client.Minecraft;

public final class ToggleSprintModule extends ClientModule {
    public ToggleSprintModule() {
        super(
                "sprint_assist",
                "Sprint Assist",
                "Keeps sprint enabled while moving forward, unless sneaking.",
                ModuleCategory.MOVEMENT
        );
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.player == null || client.screen != null) {
            return;
        }

        if (client.options.keyUp.isDown()
                && !client.options.keyShift.isDown()
                && !client.player.isPassenger()) {
            client.player.setSprinting(true);
        }
    }
}
