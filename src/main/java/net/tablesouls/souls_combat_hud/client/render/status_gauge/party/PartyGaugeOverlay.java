package net.tablesouls.souls_combat_hud.client.render.status_gauge.party;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeLayout;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeStyleRegistry;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeSubject;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeOverlay;
import net.tablesouls.souls_combat_hud.compat.TeamProviderRegistry;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberClientCache;
import net.tablesouls.souls_combat_hud.party.network.PartyNetwork;
import net.tablesouls.souls_combat_hud.config.CrestDisplayMode;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;

import java.util.*;

public class PartyGaugeOverlay implements IGuiOverlay {
    private final Map<UUID, GaugeOverlay> slotsByPlayer = new HashMap<>();
    private final Map<UUID, PartyMemberGaugeSubject> subjectsByPlayer = new HashMap<>();

    private static final int SLOT_HEIGHT = 40;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        if (!SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.enabled.get()) return;

        if (!PartyNetwork.serverSupportsParty()) {
            slotsByPlayer.clear();
            subjectsByPlayer.clear();
            return;
        }

        AbstractClientPlayer localPlayer = mc.player;
        if (localPlayer == null) return;

        List<UUID> teammates = TeamProviderRegistry.resolveDisplayedTeammateIds(localPlayer);
        teammates = teammates.stream().filter(id -> !PartyMemberClientCache.isHidden(id)).toList();
        if (teammates.isEmpty()) {
            slotsByPlayer.clear();
            subjectsByPlayer.clear();
            return;
        }

        if (!SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.showOfflineMembers.get()) {
            ClientPacketListener connection = mc.getConnection();
            teammates = teammates.stream()
                    .filter(id -> connection != null && connection.getPlayerInfo(id) != null)
                    .toList();
            if (teammates.isEmpty()) {
                slotsByPlayer.clear();
                subjectsByPlayer.clear();
                return;
            }
        }

        int maxDisplayed = SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.maxDisplayedPartyMembers.get();
        if (maxDisplayed > 0 && teammates.size() > maxDisplayed) {
            teammates = teammates.subList(0, maxDisplayed);
        }

        final List<UUID> visibleTeammates = teammates;
        slotsByPlayer.keySet().removeIf(id -> !visibleTeammates.contains(id));
        subjectsByPlayer.keySet().removeIf(id -> !visibleTeammates.contains(id));

        ElementAnchor overlayAnchor = SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.anchor.get();
        boolean mirrored = overlayAnchor.horizontal() == ElementAnchor.Horizontal.RIGHT;
        int overlayX = overlayAnchor.resolveX(screenWidth, SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.x.get(), 0);
        int overlayY = overlayAnchor.resolveY(screenHeight, SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.y.get(), 0);

        OptionalInt teamColor = SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.crestTeamOutline.get()
                ? TeamProviderRegistry.resolveActiveTeamColor(localPlayer)
                : OptionalInt.empty();

        int nextSlotY = overlayY;
        for (UUID teammateId : teammates) {
            GaugeOverlay slot = slotsByPlayer.computeIfAbsent(
                    teammateId,
                    id -> new GaugeOverlay(GaugeStyleRegistry.PARTY)
            );
            GaugeSubject subject = subjectsByPlayer.computeIfAbsent(
                    teammateId,
                    PartyMemberGaugeSubject::new
            );

            nextSlotY = renderSlot(graphics, font, slot, subject, overlayX, nextSlotY, mirrored, teamColor);
        }
    }

    private static CrestDisplayMode resolveCrestDisplayMode(GaugeSubject subject) {
        if (SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.crestDisplayMode.get()
                != CrestDisplayMode.MODEL) return CrestDisplayMode.FACE;

        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer localPlayer = mc.player;
        Optional<AbstractClientPlayer> target = subject.asRenderableEntity();
        if (
                localPlayer == null
                        || target.isEmpty()
                        || mc.level == null
        ) {
            return CrestDisplayMode.FACE;
        }

        double renderDistanceBlocks = mc.options.renderDistance().get() * 16.0;
        double maxModelDistance = renderDistanceBlocks * 0.9;

        return localPlayer.distanceTo(target.get()) <= maxModelDistance
                ? CrestDisplayMode.MODEL
                : CrestDisplayMode.FACE;
    }

    private int renderSlot(GuiGraphics graphics, Font font, GaugeOverlay slot, GaugeSubject subject, int x, int y, boolean mirrored, OptionalInt teamColor) {
        GaugeLayout crestLayout = GaugeStyleRegistry.PARTY.getLayout("crest", GaugeLayout.DEFAULT);
        GaugeLayout playerNameLayout = GaugeStyleRegistry.PARTY.getLayout("player_name", GaugeLayout.DEFAULT);
        GaugeLayout gaugesLayout = GaugeStyleRegistry.PARTY.getLayout("gauges", GaugeLayout.DEFAULT);

        int crestSize = crestLayout.size();
        int crestX = mirrored ? x - crestLayout.x() - crestSize : x + crestLayout.x();
        int contentBottom = y + SLOT_HEIGHT; // never shrink below the configured baseline slot height

        if (crestLayout.enabled()) {
            CrestDisplayMode displayMode = resolveCrestDisplayMode(subject);
            slot.renderCrest(graphics, subject, font, crestX, y + crestLayout.y(), crestSize, !mirrored, displayMode, teamColor);
            contentBottom = Math.max(contentBottom, y + crestLayout.y() + crestSize);
        }

        int playerNameX = mirrored ? x - playerNameLayout.x() : x + playerNameLayout.x();
        if (playerNameLayout.enabled()) {
            slot.renderPlayerName(graphics, subject, font, playerNameX, y + playerNameLayout.y(), mirrored);
        }

        int gaugesX = mirrored ? x - gaugesLayout.x() : x + gaugesLayout.x();
        if (gaugesLayout.enabled() && subject.isOnline()) {
            int rowsBottom = slot.renderGaugeRows(graphics, subject, font, gaugesX, y + gaugesLayout.y(), mirrored);
            contentBottom = Math.max(contentBottom, rowsBottom);
        }

        return contentBottom + SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.memberRowGap.get();
    }
}