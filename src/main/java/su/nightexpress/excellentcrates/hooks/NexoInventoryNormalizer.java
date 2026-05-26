package su.nightexpress.excellentcrates.hooks;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.key.KeyManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Replaces Nexo items that no longer match {@link NexoHook#buildSingleton(String)} (component drift after
 * updates / plugins) so they stack again with freshly built Nexo stacks.
 */
public final class NexoInventoryNormalizer {

    private NexoInventoryNormalizer() {}

    /**
     * Do not wipe intentionally customized text (for example recolteur lore showing stored amounts).
     * Bukkit exposes equivalent JSON component trees as the same visible strings, so JSON-only drift can still be fixed.
     */
    private static boolean hasVisibleTextDifferingFromCanon(@NotNull ItemStack stack, @NotNull ItemStack canon) {
        ItemMeta stackMeta = stack.getItemMeta();
        ItemMeta canonMeta = canon.getItemMeta();
        return !Objects.equals(itemName(stackMeta), itemName(canonMeta))
            || !Objects.equals(displayName(stackMeta), displayName(canonMeta))
            || !Objects.equals(lore(stackMeta), lore(canonMeta));
    }

    private static @Nullable String itemName(@Nullable ItemMeta meta) {
        return meta != null && meta.hasItemName() ? plainText(meta.getItemName()) : null;
    }

    private static @Nullable String displayName(@Nullable ItemMeta meta) {
        return meta != null && meta.hasDisplayName() ? plainText(meta.getDisplayName()) : null;
    }

    private static @NotNull List<String> lore(@Nullable ItemMeta meta) {
        return meta != null && meta.hasLore() ? meta.getLore().stream().map(NexoInventoryNormalizer::plainText).toList() : List.of();
    }

    private static @NotNull String plainText(@Nullable String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String stripped = ChatColor.stripColor(text);
        return stripped == null ? "" : stripped.trim();
    }

    /**
     * Scans the player's main inventory (storage, armor, offhand) and replaces non-canonical Nexo stacks.
     *
     * @return how many slots were rewritten
     */
    public static int normalize(@NotNull Player player, @NotNull KeyManager keyManager) {
        if (!NexoHook.isReflectReady()) {
            return 0;
        }
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        if (contents.length == 0) {
            return 0;
        }
        Map<String, ItemStack> canonById = new HashMap<>();
        int replaced = 0;
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            Optional<String> idOpt = NexoHook.resolveNexoRebuildId(stack);
            if (idOpt.isEmpty()) {
                continue;
            }
            String id = idOpt.get();
            ItemStack canon = canonById.computeIfAbsent(id, NexoHook::buildSingleton);
            if (canon == null || canon.getType().isAir()) {
                continue;
            }
            ItemStack probe = stack.clone();
            probe.setAmount(1);
            if (canon.isSimilar(probe)) {
                continue;
            }
            if (hasVisibleTextDifferingFromCanon(probe, canon)) {
                continue;
            }
            ItemStack fixed = canon.clone();
            fixed.setAmount(stack.getAmount());
            keyManager.applyRegisteredKeyTagIfNexoMatch(fixed, id);
            inv.setItem(i, fixed);
            replaced++;
        }
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && !cursor.getType().isAir()) {
            Optional<String> cursorId = NexoHook.resolveNexoRebuildId(cursor);
            if (cursorId.isPresent()) {
                String id = cursorId.get();
                ItemStack canon = canonById.computeIfAbsent(id, NexoHook::buildSingleton);
                if (canon != null && !canon.getType().isAir()) {
                    ItemStack probe = cursor.clone();
                    probe.setAmount(1);
                    if (!canon.isSimilar(probe) && !hasVisibleTextDifferingFromCanon(probe, canon)) {
                        ItemStack fixed = canon.clone();
                        fixed.setAmount(cursor.getAmount());
                        keyManager.applyRegisteredKeyTagIfNexoMatch(fixed, id);
                        player.setItemOnCursor(fixed);
                        replaced++;
                    }
                }
            }
        }
        return replaced;
    }
}
