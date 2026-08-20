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
import net.tablesouls.souls_combat_hud.client.util.animation.SymmetricFractionAnimator;

public class OxygenBarOverlay implements IGuiOverlay {

    private final BarElement bar = BarElement.withFade(0L, 500L);
    private final SymmetricFractionAnimator fractionAnim = new SymmetricFractionAnimator(250L);

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

        float fraction = maxAirSupply > 0
                ? net.minecraft.util.Mth.clamp((float) airSupply / maxAirSupply, 0.0f, 1.0f)
                : 0.0f;

        float visualFraction = fractionAnim.update(fraction);

        boolean shouldShow = submerged || visualFraction < 0.999f;
        bar.setVisible(shouldShow);

        if (bar.isHidden()) return;

        int barWidth = SoulsCombatHUDConfig.OXYGEN_BAR.width.get();
        int barHeight = 4;

        ElementAnchor anchor = SoulsCombatHUDConfig.OXYGEN_BAR.anchor.get();
        int anchorX = anchor.resolveX(screenWidth, SoulsCombatHUDConfig.OXYGEN_BAR.x.get(), 0);
        int anchorY = anchor.resolveY(screenHeight, SoulsCombatHUDConfig.OXYGEN_BAR.y.get(), 0);
        float scale = SoulsCombatHUDConfig.OXYGEN_BAR.scale.get().floatValue();

        int localX = -barWidth * (anchor.dx() + 1) / 2;
        int localY = -barHeight * (anchor.dy() + 1) / 2;

        OxygenBarStyleDefinition styleDef = OxygenBarStyleRegistry.get();
        bar.withDecoration(styleDef.toBarDecoration());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        graphics.pose().pushPose();
        graphics.pose().translate(anchorX, anchorY, 0);
        graphics.pose().scale(scale, scale, 1.0f);

        bar.renderPreSmoothed(
                graphics,
                styleDef.toBarStyle(),
                localX, localY,
                barWidth, barHeight,
                visualFraction,
                false);

        graphics.pose().popPose();

        RenderSystem.disableBlend();
    }
}