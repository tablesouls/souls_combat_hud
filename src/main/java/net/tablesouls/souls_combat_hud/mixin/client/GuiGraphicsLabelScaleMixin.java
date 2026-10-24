package net.tablesouls.souls_combat_hud.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.tablesouls.souls_combat_hud.client.render.SkillLabelScale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsLabelScaleMixin {

    @Unique
    private boolean souls_combat_hud$scalingLabel;

    @Inject(
            method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFIZ)I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void souls_combat_hud$scaleSkillLabel(
            Font font,
            String text,
            float x,
            float y,
            int color,
            boolean shadow,
            CallbackInfoReturnable<Integer> cir
    ) {
        float scale = SkillLabelScale.get();
        if (scale == 1.0f || this.souls_combat_hud$scalingLabel) {
            return;
        }

        GuiGraphics self = (GuiGraphics) (Object) this;
        float centerX = x + font.width(text) / 2.0f;
        float centerY = y + font.lineHeight / 2.0f;

        PoseStack pose = self.pose();
        this.souls_combat_hud$scalingLabel = true;
        pose.pushPose();
        try {
            pose.translate(centerX, centerY, 0.0f);
            pose.scale(scale, scale, 1.0f);
            pose.translate(-centerX, -centerY, 0.0f);
            int result = self.drawString(font, text, x, y, color, shadow);
            cir.setReturnValue(result);
        } finally {
            pose.popPose();
            this.souls_combat_hud$scalingLabel = false;
        }
    }
}