package su.nightexpress.excellentcrates.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.hooks.NexoHook;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.item.ItemBridge;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;
import su.nightexpress.nightcore.integration.item.impl.AdaptedCustomStack;
import su.nightexpress.nightcore.integration.item.impl.AdaptedItemStack;
import su.nightexpress.nightcore.integration.item.impl.AdaptedVanillaStack;
import su.nightexpress.nightcore.util.ItemNbt;
import su.nightexpress.nightcore.util.ItemTag;
import su.nightexpress.nightcore.util.Version;

import java.util.Optional;

public class ItemHelper {

    @NotNull
    public static AdaptedItem readOrPlaceholder(@NotNull FileConfig config, @NotNull String path) {
        return read(config, path).orElse(vanilla(CrateUtils.getQuestionStack()));
    }

    @NotNull
    public static Optional<AdaptedItem> read(@NotNull FileConfig config, @NotNull String path) {
        Optional<AdaptedItem> exactNbt = RawCompressedNbtAdaptedItem.tryRead(config, path);
        if (exactNbt.isPresent()) {
            return exactNbt;
        }

        Optional<AdaptedItem> nexoOnly = NexoIdOnlyAdaptedItem.tryRead(config, path);
        if (nexoOnly.isPresent()) {
            return nexoOnly;
        }

        String oldType = config.getString(path + ".Type");
        if (oldType != null) {
            AdaptedItem adaptedItem = null;

            if (oldType.equalsIgnoreCase("vanilla")) {
                String itemTag = config.getString(path + ".ItemTag");
                if (itemTag != null) {
                    ItemTag tag = new ItemTag(itemTag, Version.MC_1_21.getDataVersion());
                    config.set(path + ".Tag", tag);
                    config.remove(path + ".ItemTag");
                }

                if (config.contains(path + ".Tag")) {
                    ItemTag tag = ItemTag.read(config, path + ".Tag");
                    adaptedItem = new AdaptedVanillaStack(tag);
                    config.remove(path + ".Tag");
                }
            }
            else if (oldType.equalsIgnoreCase("custom")) {
                String handlerName = config.getString(path + ".Handler", "null");
                String itemId = config.getString(path + ".ItemId", "null");
                int amount = config.getInt(path + ".Amount");

                config.remove(path + ".Handler");
                config.remove(path + ".ItemId");
                config.remove(path + ".Amount");

                ItemAdapter<?> adapter = ItemBridge.getAdapter(handlerName);
                if (adapter instanceof IdentifiableItemAdapter identifiableItemAdapter) {
                    adaptedItem = new AdaptedCustomStack(identifiableItemAdapter, new ItemIdData(itemId, amount));
                }
                else if (adapter == null) {
                    config.set(path + ".Provider", handlerName);
                    config.set(path + ".Data.ID", itemId);
                    config.set(path + ".Data.Amount", amount);
                }
            }

            config.remove(path + ".Type");

            if (adaptedItem != null) {
                config.set(path, adaptedItem);
            }
        }

        return Optional.ofNullable(AdaptedItemStack.read(config, path));
    }

    /**
     * Same as {@link #vanilla(ItemStack)} data-wise, but persists via {@link ItemNbt#compress(ItemStack)}
     * so item NBT is not re-serialized through {@link ItemTag} / config (which normalizes component JSON).
     */
    @NotNull
    public static AdaptedItem exactVanillaNbt(@NotNull ItemStack itemStack) {
        ItemStack unit = itemStack.clone();
        int giveAmount = Math.max(1, unit.getAmount());
        unit.setAmount(1);

        String compressed = ItemNbt.compress(unit);
        if (compressed == null || compressed.isBlank()) {
            return vanilla(itemStack);
        }
        String nexoId = NexoHook.resolveNexoRebuildId(itemStack).orElse(null);
        return new RawCompressedNbtAdaptedItem(compressed, giveAmount, nexoId);
    }

    /**
     * Nexo-only save: id + quantity, no YAML NBT blob. Returns null when the stack has no Nexo id.
     */
    @Nullable
    public static NexoIdOnlyAdaptedItem nexoIdOnly(@NotNull ItemStack itemStack) {
        return NexoHook.resolveNexoRebuildId(itemStack)
            .map(id -> new NexoIdOnlyAdaptedItem(id, Math.max(1, itemStack.getAmount())))
            .orElse(null);
    }

    @NotNull
    public static ItemStack toItemStack(@NotNull AdaptedItem item) {
        return item.itemStack().orElse(CrateUtils.getQuestionStack());
    }

    public static boolean isCustom(@NotNull ItemStack itemStack) {
        ItemAdapter<?> adapter = ItemBridge.getAdapter(itemStack);
        return adapter != null && !adapter.isVanilla();
    }

    @NotNull
    public static AdaptedItem vanilla(@NotNull ItemStack itemStack) {
        return AdaptedVanillaStack.of(itemStack);
    }

    @NotNull
    public static AdaptedItem adapt(@NotNull ItemStack itemStack) {
        ItemAdapter<?> adapter = ItemBridge.getAdapterOrVanilla(itemStack);
        AdaptedItem item = adapter.adapt(itemStack).orElse(null);
        return item == null ? vanilla(itemStack) : item;
    }

    @NotNull
    public static AdaptedItem adapt(@NotNull ItemStack itemStack, boolean allowCustoms) {
        return allowCustoms ? adapt(itemStack) : exactVanillaNbt(itemStack);
    }
}
