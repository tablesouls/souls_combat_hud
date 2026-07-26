package net.tablesouls.souls_combat_hud.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class RegexItemList {
    private final Supplier<List<? extends String>> configSupplier;
    private List<Pattern> compiled = List.of();
    private List<? extends String> lastRaw = null;

    public RegexItemList(Supplier<List<? extends String>> configSupplier) {
        this.configSupplier = configSupplier;
    }

    public boolean matches(ItemStack stack) {
        List<? extends String> raw = configSupplier.get();
        if (raw != lastRaw) {
            recompile(raw);
        }
        if (compiled.isEmpty()) {
            return false;
        }
        String id = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        for (Pattern pattern : compiled) {
            if (pattern.matcher(id).matches()) {
                return true;
            }
        }
        return false;
    }

    private void recompile(List<? extends String> raw) {
        List<Pattern> patterns = new ArrayList<>(raw.size());
        for (String entry : raw) {
            try {
                patterns.add(Pattern.compile(entry));
            } catch (PatternSyntaxException e) {
                System.err.println("[souls_combat_hud] Invalid regex: " + entry);
            }
        }
        compiled = patterns;
        lastRaw = raw;
    }
}