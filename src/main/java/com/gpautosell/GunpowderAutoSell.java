package com.gpautosell;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class GunpowderAutoSell implements ClientModInitializer {

    private static final String MENU_TITLE = "скупщик";
    private static final int FALLBACK_SLOT = -1;
    private static final int CLICK_DELAY = 5;

    private int lastGunpowder = -1;
    private int cooldown = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            lastGunpowder = -1;
            return;
        }
        if (cooldown > 0) cooldown--;

        int count = countGunpowder(client);
        boolean received = lastGunpowder != -1 && count > lastGunpowder;
        lastGunpowder = count;

        if (!received || cooldown > 0) return;

        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return;
        if (!screen.getTitle().getString().toLowerCase().contains(MENU_TITLE)) return;

        ScreenHandler handler = screen.getScreenHandler();
        int target = -1;
        for (Slot slot : handler.slots) {
            if (slot.inventory == client.player.getInventory()) continue;
            if (slot.getStack().isOf(Items.GUNPOWDER)) {
                target = slot.id;
                break;
            }
        }
        if (target == -1) target = FALLBACK_SLOT;
        if (target == -1) return;

        client.interactionManager.clickSlot(
                handler.syncId, target, 0, SlotActionType.PICKUP, client.player);
        cooldown = CLICK_DELAY;
    }

    private int countGunpowder(MinecraftClient client) {
        int total = 0;
        var inv = client.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getStack(i);
            if (stack.isOf(Items.GUNPOWDER)) total += stack.getCount();
        }
        return total;
    }
}
