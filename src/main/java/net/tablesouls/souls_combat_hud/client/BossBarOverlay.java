package net.tablesouls.souls_combat_hud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.util.BossBarState;
import net.tablesouls.souls_combat_hud.util.DamageRevealAnimator;
import net.tablesouls.souls_combat_hud.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.util.FadeAnimator;
import org.joml.Matrix4f;

public class BossBarOverlay implements IGuiOverlay {
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private static final int CAP_WIDTH = 1;
    private static final int CAP_COLOR = 0xFFFFFFFF;

    private static final int GLOW_WIDTH = 4;
    private static final int GLOW_COLOR = 0xFFFFFFFF;
    private static final int GLOW_ALPHA = 153;

    private static final int BAR_COLOR = 0xFF8A1A1A;
    private static final int BAR_BG_COLOR = 0xC8101010;

    private static final int DAMAGE_REVEAL_COLOR = 0xFFC9A054;
    private static final int BAR_GAP = 18;

    private static int withAlpha(int argb, float alpha) {
        int a = Mth.clamp(Math.round(((argb >>> 24) & 0xFF) * alpha), 0, 255);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    private final Map<UUID, FadeAnimator> fades = new HashMap<>();
    private final Map<UUID, DamageRevealAnimator> damageReveals = new HashMap<>();

    @Override
    public void render(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        if (!SoulsCombatHUDConfig.CUSTOM_BOSSBAR.enabled.get()) {
            return;
        }

        int barW = SoulsCombatHUDConfig.CUSTOM_BOSSBAR.width.get();
        int barH = 5;
        int maxVisibleBossbars = SoulsCombatHUDConfig.CUSTOM_BOSSBAR.maxVisible.get();

        ElementAnchor anchor = SoulsCombatHUDConfig.CUSTOM_BOSSBAR.anchor.get();
        int offsetX = SoulsCombatHUDConfig.CUSTOM_BOSSBAR.x.get();
        int offsetY = SoulsCombatHUDConfig.CUSTOM_BOSSBAR.y.get();

        int x = anchor.resolveX(screenWidth, offsetX, barW);
        int baseY = anchor.resolveY(screenHeight, offsetY, barH);
        int rowStep = anchor.isBottom() ? -BAR_GAP : BAR_GAP;

        Map<UUID, BossBarState.Entry> active = BossBarState.getActive();

        int row = 0;
        for (Map.Entry<UUID, BossBarState.Entry> mapEntry : active.entrySet()) {
            UUID id = mapEntry.getKey();
            BossBarState.Entry entry = mapEntry.getValue();
            boolean isActive = BossBarState.isActiveThisFrame(id);

            FadeAnimator fade = fades.computeIfAbsent(id, k -> new FadeAnimator(250L, 400L));
            fade.setVisible(isActive);
            float alpha = fade.tick();

            if (fade.isHidden()) {
                if (!isActive) {
                    active.remove(id);
                    fades.remove(id);
                    damageReveals.remove(id);
                }
                continue;
            }

            DamageRevealAnimator revealTracker = damageReveals.computeIfAbsent(id, k -> new DamageRevealAnimator());
            float displayedProgress = revealTracker.update(entry.progress);

            if (row >= maxVisibleBossbars) {
                continue;
            }

            int y = baseY + row * rowStep;

            graphics.fill(x, y, x + barW, y + barH, withAlpha(BAR_BG_COLOR, alpha));

            int filled = Mth.floor(entry.progress * barW);
            int displayedFilled = Mth.floor(displayedProgress * barW);
            if (displayedFilled > filled) {
                graphics.fill(x + filled, y, x + displayedFilled, y + barH, withAlpha(DAMAGE_REVEAL_COLOR, alpha));
            }

            if (filled > 0) {
                graphics.fill(x, y, x + filled, y + barH, withAlpha(BAR_COLOR, alpha));

                int edgeX = x + filled;
                int glowAlpha = Math.round(GLOW_ALPHA * alpha);
                drawGlowLeft(graphics, edgeX, y, barH, GLOW_WIDTH, GLOW_COLOR, glowAlpha, x);

                int capL = edgeX;
                int capR = edgeX + CAP_WIDTH;
                graphics.fill(capL, y - 1, capR, y + barH + 1, withAlpha(CAP_COLOR, alpha));
            }

            if (entry.name != null) {
                int nameY = y - 10;
                graphics.drawString(Minecraft.getInstance().font, entry.name, x, nameY, withAlpha(TEXT_COLOR, alpha), true);
            }

            row++;
        }
    }

    public static void drawGlowLeft(GuiGraphics graphics, int edgeX, int y, int height, int glowWidth, int rgb, int maxAlpha, int minX) {
        int leftX = Math.max(minX, edgeX - glowWidth);
        if (leftX >= edgeX) {
            return;
        }

        int colorBright = maxAlpha << 24 | rgb & 0xFFFFFF;
        int colorTransparent = rgb & 0xFFFFFF;

        Matrix4f pose = graphics.pose().last().pose();
        VertexConsumer vc = graphics.bufferSource().getBuffer(RenderType.gui());

        vc.vertex(pose, edgeX, y, 0).color(colorBright).endVertex();
        vc.vertex(pose, leftX, y, 0).color(colorTransparent).endVertex();
        vc.vertex(pose, leftX, y + height, 0).color(colorTransparent).endVertex();
        vc.vertex(pose, edgeX, y + height, 0).color(colorBright).endVertex();
    }
}