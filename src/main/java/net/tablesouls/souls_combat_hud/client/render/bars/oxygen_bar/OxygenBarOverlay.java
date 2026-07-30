package net.tablesouls.souls_combat_hud.client.render.bars.oxygen_bar;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.client.render.bars.BarElement;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;

public class OxygenBarOverlay implements IGuiOverlay {

    private final BarElement bar = BarElement.withFade(0L, 500L);

    @Override
    public void render(
            ForgeGui gui,
            net.minecraft.client.gui.GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        if (!SoulsCombatHUDConfig.OXYGEN_BAR.enabled.get()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (player.isCreative()) return;

        int airSupply = player.getAirSupply();
        int maxAirSupply = player.getMaxAirSupply();

        boolean submerged = player.isEyeInFluid(FluidTags.WATER);
        boolean shouldShow = submerged || airSupply < maxAirSupply;
        bar.setVisible(shouldShow);

        if (bar.isHidden()) return;

        int barWidth = SoulsCombatHUDConfig.OXYGEN_BAR.width.get();
        int barHeight = 4;

        ElementAnchor anchor = SoulsCombatHUDConfig.OXYGEN_BAR.anchor.get();
        int x = anchor.resolveX(screenWidth, SoulsCombatHUDConfig.OXYGEN_BAR.x.get(), barWidth);
        int y = anchor.resolveY(screenHeight, SoulsCombatHUDConfig.OXYGEN_BAR.y.get(), barHeight);

        float fraction = maxAirSupply > 0
                ? net.minecraft.util.Mth.clamp((float) airSupply / maxAirSupply, 0.0f, 1.0f)
                : 0.0f;

        OxygenBarStyleDefinition styleDef = OxygenBarStyleRegistry.get();
        bar.withDecoration(styleDef.toBarDecoration());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        bar.render(
                graphics,
                styleDef.toBarStyle(),
                x, y,
                barWidth, barHeight,
                fraction,
                null,
                false,
                false);

        RenderSystem.disableBlend();
    }
}