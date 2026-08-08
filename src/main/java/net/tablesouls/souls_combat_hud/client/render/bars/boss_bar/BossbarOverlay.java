package net.tablesouls.souls_combat_hud.client.render.bars.boss_bar;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.client.render.bars.BarElement;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.client.util.BossBarState;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BossbarOverlay implements IGuiOverlay {
    private final Map<UUID, BarElement> bars = new HashMap<>();

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!SoulsCombatHUDConfig.CUSTOM_BOSSBAR.enabled.get()) {
            return;
        }

        int barW = SoulsCombatHUDConfig.CUSTOM_BOSSBAR.width.get();
        int barH = 5;

        int maxVisibleBossbars = SoulsCombatHUDConfig.CUSTOM_BOSSBAR.maxVisible.get();
        ElementAnchor anchor = SoulsCombatHUDConfig.CUSTOM_BOSSBAR.anchor.get();

        int x = anchor.resolveX(
                screenWidth,
                SoulsCombatHUDConfig.CUSTOM_BOSSBAR.x.get(),
                barW
        );

        int baseY = anchor.resolveY(
                screenHeight,
                SoulsCombatHUDConfig.CUSTOM_BOSSBAR.y.get(),
                barH
        );

        int rowStep = anchor.isBottom() ? -18 : 18;

        Map<UUID, BossBarState.Entry> active = BossBarState.getActive();

        int row = 0;
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, BossBarState.Entry> mapEntry : active.entrySet()) {

            UUID id = mapEntry.getKey();
            BossBarState.Entry entry = mapEntry.getValue();

            if (entry.name == null) {
                continue;
            }

            BossBarStyleDefinition styleDef = BossBarStyleRegistry.resolve(entry.name);

            boolean isActive = BossBarState.isActiveThisFrame(id);

            BarElement bar = bars.computeIfAbsent(
                    id, key ->
                            BarElement.withFade(
                                    250L,
                                    400L,
                                    styleDef.disappearDelay() + SoulsCombatHUDConfig.CUSTOM_BOSSBAR.disappear_delay.get()
                            )
            );
            bar.setVisible(isActive);

            if (bar.isHidden()) {
                if (!isActive) {
                    toRemove.add(id);
                }
                continue;
            }

            if (row >= maxVisibleBossbars) {
                continue;
            }

            int y = baseY + row * rowStep;

            bar.withDecoration(styleDef.toBarDecoration());

            bar.render(
                    graphics,
                    styleDef.toBarStyle(),
                    x,
                    y,
                    barW,
                    barH,
                    entry.progress,
                    entry.name,
                    null,
                    false,
                    true,
                    1.0f,
                    entry.maxHealth > 0 ? entry.maxHealth : -1f,
                    entry.currentHealth,
                    SoulsCombatHUDConfig.CUSTOM_BOSSBAR.reductionValueText.get()
            );

            row++;
        }

        for (UUID id : toRemove) {
            active.remove(id);
            bars.remove(id);
        }
    }
}