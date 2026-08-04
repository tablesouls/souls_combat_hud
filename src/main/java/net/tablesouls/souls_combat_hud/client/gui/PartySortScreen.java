package net.tablesouls.souls_combat_hud.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.gui.widget.ScrollPanel;
import net.tablesouls.souls_combat_hud.client.gui.widget.TexturedStateButton;
import net.tablesouls.souls_combat_hud.client.gui.widget.TexturedToggleButton;
import net.tablesouls.souls_combat_hud.client.util.PlayerFacePreviewRenderer;
import net.tablesouls.souls_combat_hud.compat.TeamProviderRegistry;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.party.client.PartyDisplayPreferences;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberProfileCache;

import java.util.*;

public class PartySortScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    SoulsCombatHUD.MODID,
                    "textures/gui/party_sort.png");

    private static final int TEX_WIDTH = 256;
    private static final int TEX_HEIGHT = 256;

    private static final int BACKGROUND_WIDTH = 256;
    private static final int BACKGROUND_HEIGHT = 168;

    private static final int ROW_BACKGROUND_TEX_U = 0;
    private static final int ROW_BACKGROUND_TEX_V = 168;

    private static final int LIST_SIDE_PADDING = 8;
    private static final int LIST_TOP_PADDING = 18;
    private static final int LIST_BOTTOM_PADDING = 8;

    private static final int ROW_HEIGHT = 22;
    private static final int ROW_WIDTH = 233;

    private static final int ROW_GAP = 2;
    private static final int ROW_STEP = ROW_HEIGHT + ROW_GAP;

    private static final int SCROLLBAR_GAP = 8;
    private static final double SCROLL_PER_WHEEL_NOTCH = ROW_HEIGHT;

    private static final int SCROLLBAR_TEX_U = 244;
    private static final int SCROLLBAR_TEX_V = 168;

    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_HEIGHT = 27;

    private static final int UP_TEX_WIDTH = 15;
    private static final int UP_TEX_HEIGHT = 8;
    private static final int UP_TEX_U = 224;
    private static final int UP_TEX_V = 224;

    private static final int DOWN_TEX_WIDTH = 15;
    private static final int DOWN_TEX_HEIGHT = 8;
    private static final int DOWN_TEX_U = 240;
    private static final int DOWN_TEX_V = 224;

    private static final int PIN_TEX_SIZE = 20;
    private static final int PIN_TEX_U = 0;
    private static final int PIN_TEX_V = 216;

    private static final int HIDE_TEX_SIZE = 20;
    private static final int HIDE_TEX_U = 40;
    private static final int HIDE_TEX_V = 216;

    private static final ScrollPanel.ScrollbarStyle SCROLLBAR_STYLE = new ScrollPanel.ScrollbarStyle(
            TEXTURE,
            SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT,
            TEX_WIDTH, TEX_HEIGHT,
            SCROLLBAR_TEX_U, SCROLLBAR_TEX_V,
            0x40000000);

    private int leftPos;
    private int topPos;

    private int listY0, listY1;
    private int rowLeft;

    private final ScrollPanel scrollPanel = new ScrollPanel(SCROLLBAR_STYLE, SCROLL_PER_WHEEL_NOTCH);

    private final List<UUID> rosterOrder = new ArrayList<>();
    private final List<RowWidget> rows = new ArrayList<>();

    public PartySortScreen() {
        super(resolveTitle());
    }

    private static Component resolveTitle() {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Optional<Component> teamName = TeamProviderRegistry.resolveActiveTeamName(player);
            if (teamName.isPresent()) {
                return Component.translatable("gui.souls_combat_hud.party_sort.title_with_team", teamName.get());
            }
        }
        return Component.translatable("gui.souls_combat_hud.party_sort.title");
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
        this.topPos = (this.height - BACKGROUND_HEIGHT) / 2;

        int listX0 = this.leftPos + LIST_SIDE_PADDING;
        int listX1 = this.leftPos + BACKGROUND_WIDTH - LIST_SIDE_PADDING;
        this.listY0 = this.topPos + LIST_TOP_PADDING;
        this.listY1 = this.topPos + BACKGROUND_HEIGHT - LIST_BOTTOM_PADDING;

        this.rowLeft = listX0;
        int scrollbarX = this.leftPos + BACKGROUND_WIDTH - SCROLLBAR_WIDTH - SCROLLBAR_GAP;

        this.scrollPanel.setViewport(listX0, this.listY0, listX1, this.listY1, scrollbarX);
        refresh();
    }

    private void refresh() {
        this.clearWidgets();
        this.rows.clear();
        this.rosterOrder.clear();

        boolean hideOffline = !SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.showOfflineMembers.get();

        Button hideOfflineBtn = Button.builder(
                Component.translatable(hideOffline
                        ? "gui.souls_combat_hud.party_sort.show_offline"
                        : "gui.souls_combat_hud.party_sort.hide_offline"),
                btn -> {
                    SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.showOfflineMembers.set(hideOffline);
                    PartySortScreen.this.refresh();
                }
        ).bounds(this.leftPos + BACKGROUND_WIDTH - LIST_SIDE_PADDING - 90, this.topPos + 2, 90, 14).build();
        this.addRenderableWidget(hideOfflineBtn);

        if (this.minecraft != null && this.minecraft.player != null) {
            ClientPacketListener connection = this.minecraft.getConnection();

            List<UUID> base = TeamProviderRegistry.resolveTeammateIds(this.minecraft.player);
            List<UUID> sorted = new ArrayList<>(base);

            Map<UUID, Integer> fallbackIndex = new HashMap<>();
            for (int i = 0; i < sorted.size(); i++) {
                fallbackIndex.put(sorted.get(i), i);
            }

            Comparator<UUID> byPinnedFirst = Comparator.comparing(
                    (UUID id) -> PartyDisplayPreferences.isPinned(id) ? 0 : 1
            );
            Comparator<UUID> byCustomThenFallback = Comparator.comparing((UUID id) -> {
                Integer custom = PartyDisplayPreferences.getCustomOrder(id);
                return custom != null ? custom : fallbackIndex.get(id) + sorted.size();
            });
            sorted.sort(byPinnedFirst.thenComparing(byCustomThenFallback));

            if (hideOffline) {
                sorted.removeIf(id -> connection == null || connection.getPlayerInfo(id) == null);
            }

            this.rosterOrder.addAll(sorted);

            for (int i = 0; i < sorted.size(); i++) {
                RowWidget row = new RowWidget(sorted.get(i), i, i == 0, i == sorted.size() - 1);
                this.rows.add(row);
                this.addWidget(row.up);
                this.addWidget(row.down);
                this.addWidget(row.pinBtn);
                this.addWidget(row.hideBtn);
            }
        }

        int rowCount = this.rows.size();
        int contentHeight = rowCount == 0 ? 0 : rowCount * ROW_HEIGHT + (rowCount - 1) * ROW_GAP;
        this.scrollPanel.setContentHeight(contentHeight);
        layoutRows();
    }

    private void layoutRows() {
        int offset = this.scrollPanel.getOffset();
        for (int i = 0; i < this.rows.size(); i++) {
            RowWidget row = this.rows.get(i);
            int rowTop = this.listY0 + i * ROW_STEP - offset;
            int rowBottom = rowTop + ROW_HEIGHT;

            boolean overlapsViewport = rowBottom > this.listY0 && rowTop < this.listY1;
            boolean fullyInViewport = rowTop >= this.listY0 && rowBottom <= this.listY1;

            row.layout(this.rowLeft, rowTop, overlapsViewport, fullyInViewport);
        }
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        for (RowWidget row : this.rows) {
            row.makeFocusable();
        }

        ComponentPath path = super.nextFocusPath(event);

        RowWidget target = rowContaining(path);
        if (target != null) {
            this.scrollPanel.scrollIntoView(
                    target.index * ROW_STEP,
                    target.index * ROW_STEP + ROW_HEIGHT
            );
        }

        layoutRows();
        return path;
    }

    private RowWidget rowContaining(ComponentPath path) {
        if (path == null) return null;
        GuiEventListener target = path.component();
        for (RowWidget row : this.rows) {
            if (row.up == target || row.down == target || row.pinBtn == target || row.hideBtn == target) {
                return row;
            }
        }
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.blit(
                TEXTURE,
                this.leftPos,
                this.topPos,
                0, 0,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
                TEX_WIDTH, TEX_HEIGHT);

        graphics.drawString(
                this.font,
                this.title,
                this.leftPos + LIST_SIDE_PADDING,
                this.topPos + 6,
                0xFFFFFF
        );

        this.scrollPanel.renderContent(graphics, () -> renderRows(graphics, mouseX, mouseY, partialTick));
        this.scrollPanel.renderScrollbar(graphics);

        if (this.rows.isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.translatable("gui.souls_combat_hud.party_sort.empty"),
                    this.width / 2, this.topPos + LIST_TOP_PADDING + 10, 0x707070
            );
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderRows(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (RowWidget row : this.rows) {
            if (row.up.visible) {
                row.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.scrollPanel.mouseScrolled(delta)) {
            layoutRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.scrollPanel.mouseClicked(mouseX, mouseY, button)) {
            layoutRows();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrollPanel.mouseDragged(mouseY)) {
            layoutRows();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.scrollPanel.mouseReleased()) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class RowWidget {
        final UUID id;
        final int index;
        final boolean first;
        final boolean last;

        final TexturedToggleButton up;
        final TexturedToggleButton down;
        final TexturedStateButton pinBtn;
        final TexturedStateButton hideBtn;

        int left;
        int top;

        RowWidget(UUID id, int index, boolean first, boolean last) {
            this.id = id;
            this.index = index;
            this.first = first;
            this.last = last;

            Component upMessage = Component.translatable("gui.souls_combat_hud.party_sort.move_up");
            this.up = new TexturedToggleButton(
                    0, 0,
                    UP_TEX_WIDTH, UP_TEX_HEIGHT,
                    upMessage,
                    TEXTURE, TEX_WIDTH, TEX_HEIGHT, UP_TEX_U, UP_TEX_V,
                    btn -> {
                        PartyDisplayPreferences.move(PartySortScreen.this.rosterOrder, id, -1);
                        PartySortScreen.this.refresh();
                    }
            );

            Component downMessage = Component.translatable("gui.souls_combat_hud.party_sort.move_down");
            this.down = new TexturedToggleButton(
                    0, 0,
                    DOWN_TEX_WIDTH, DOWN_TEX_HEIGHT,
                    downMessage,
                    TEXTURE, TEX_WIDTH, TEX_HEIGHT, DOWN_TEX_U, DOWN_TEX_V,
                    btn -> {
                        PartyDisplayPreferences.move(PartySortScreen.this.rosterOrder, id, 1);
                        PartySortScreen.this.refresh();
                    }
            );

            boolean pinned = PartyDisplayPreferences.isPinned(id);
            Component pinMessage = Component.translatable(pinned
                    ? "gui.souls_combat_hud.party_sort.unpin"
                    : "gui.souls_combat_hud.party_sort.pin");
            this.pinBtn = new TexturedStateButton(
                    0, 0,
                    PIN_TEX_SIZE, PIN_TEX_SIZE,
                    pinMessage,
                    TEXTURE,
                    TEX_WIDTH, TEX_HEIGHT,
                    PIN_TEX_U, PIN_TEX_V,
                    () -> PartyDisplayPreferences.isPinned(id),
                    btn -> {
                        PartyDisplayPreferences.setPinned(id, !PartyDisplayPreferences.isPinned(id));
                        PartySortScreen.this.refresh();
                    }
            );
            this.pinBtn.setTooltip(Tooltip.create(pinMessage));

            boolean hidden = PartyDisplayPreferences.isHidden(id);
            Component hideMessage = Component.translatable(hidden
                    ? "gui.souls_combat_hud.party_sort.unhide"
                    : "gui.souls_combat_hud.party_sort.hide");

            this.hideBtn = new TexturedStateButton(
                    0, 0,
                    HIDE_TEX_SIZE, HIDE_TEX_SIZE,
                    hideMessage,
                    TEXTURE,
                    TEX_WIDTH, TEX_HEIGHT,
                    HIDE_TEX_U, HIDE_TEX_V,
                    () -> PartyDisplayPreferences.isHidden(id),
                    btn -> {
                        PartyDisplayPreferences.setHidden(id, !PartyDisplayPreferences.isHidden(id));
                        PartySortScreen.this.refresh();
                    }
            );
            this.hideBtn.setTooltip(Tooltip.create(hideMessage));
        }

        void layout(int left, int top, boolean visible, boolean fullyVisible) {
            this.left = left;
            this.top = top;

            int sideMargin = 4;

            int arrowX = left + sideMargin;
            int stackGap = 1;
            int stackHeight = UP_TEX_HEIGHT + stackGap + DOWN_TEX_HEIGHT;
            int stackTop = top + (ROW_HEIGHT - stackHeight) / 2;

            boolean canMoveUp = !this.first;   // topmost row can't move further up
            boolean canMoveDown = !this.last;  // bottommost row can't move further down

            up.setPosition(arrowX, stackTop);
            up.visible = visible;
            up.active = visible && fullyVisible && canMoveUp;

            int downY = stackTop + UP_TEX_HEIGHT + stackGap;
            down.setPosition(arrowX, downY);
            down.visible = visible;
            down.active = visible && fullyVisible && canMoveDown;

            int rightEdge = left + ROW_WIDTH;
            int buttonGap = 2;

            int hideX = rightEdge - sideMargin - HIDE_TEX_SIZE;
            int hideY = top + (ROW_HEIGHT - HIDE_TEX_SIZE) / 2;
            hideBtn.setPosition(hideX, hideY);
            hideBtn.visible = visible;
            hideBtn.active = visible && fullyVisible;

            int pinX = hideX - buttonGap - PIN_TEX_SIZE;
            int pinY = top + (ROW_HEIGHT - PIN_TEX_SIZE) / 2;
            pinBtn.setPosition(pinX, pinY);
            pinBtn.visible = visible;
            pinBtn.active = visible && fullyVisible;
        }

        void makeFocusable() {
            up.visible = true;
            down.visible = true;
            pinBtn.visible = true;
            hideBtn.visible = true;

            up.active = !first;
            down.active = !last;
            pinBtn.active = true;
            hideBtn.active = true;
        }

        void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            PlayerInfo info = connection != null ? connection.getPlayerInfo(id) : null;
            ResourceLocation skinTexture = info != null ? info.getSkinLocation() : null;

            graphics.blit(
                    TEXTURE,
                    left, top,
                    ROW_BACKGROUND_TEX_U, ROW_BACKGROUND_TEX_V,
                    ROW_WIDTH, ROW_HEIGHT,
                    TEX_WIDTH, TEX_HEIGHT);

            int faceSize = ROW_HEIGHT - 4;
            int faceX = left + 24;
            int faceY = top + (ROW_HEIGHT - faceSize) / 2;

            PlayerFacePreviewRenderer.render(
                    graphics,
                    skinTexture,
                    faceX,
                    faceY,
                    faceSize
            );

            Component name = PartyMemberProfileCache.getDisplayName(id);
            if (name == null) {
                name = Component.literal(id.toString().substring(0, 8));
            }
            if (info == null) {
                name = Component.translatable("gui.souls_combat_hud.party_sort.offline_name", name)
                        .withStyle(ChatFormatting.ITALIC);
            }
            int nameX = info != null ? faceX + faceSize + 4 : faceX;
            graphics.drawString(PartySortScreen.this.font, name, nameX, top + (ROW_HEIGHT - 8) / 2, 0xFFFFFF);

            up.render(graphics, mouseX, mouseY, partialTick);
            down.render(graphics, mouseX, mouseY, partialTick);
            pinBtn.render(graphics, mouseX, mouseY, partialTick);
            hideBtn.render(graphics, mouseX, mouseY, partialTick);
        }
    }
}