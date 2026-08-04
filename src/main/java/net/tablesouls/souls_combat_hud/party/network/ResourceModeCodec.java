package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;

/** Shared read/write logic for the small per-kind "active mode" sync packets. */
final class ResourceModeCodec {
    private ResourceModeCodec() {}

    static <M extends Enum<M>> void encode(M mode, FriendlyByteBuf buf) {
        buf.writeBoolean(mode != null);
        if (mode != null) {
            buf.writeEnum(mode);
        }
    }

    static <M extends Enum<M>> M decode(FriendlyByteBuf buf, Class<M> modeClass) {
        return buf.readBoolean() ? buf.readEnum(modeClass) : null;
    }
}
