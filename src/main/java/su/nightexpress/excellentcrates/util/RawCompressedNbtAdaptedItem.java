package su.nightexpress.excellentcrates.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.hooks.NexoHook;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.integration.item.adapter.impl.VanillaItemAdapter;
import su.nightexpress.nightcore.util.ItemNbt;

import java.util.Optional;

/**
 * Stores an item as {@link ItemNbt#compress(ItemStack)} (binary NBT via NbtIo), so the stack round-trips
 * without YAML ItemTag / component re-encoding that changes JSON shape.
 * <p>
 * The compressed blob is always a <strong>single item</strong> (amount {@code 1}) so its components match
 * any other singleton of the same item (e.g. Nexo “base” item). How many the player receives is stored
 * separately as {@link #CONFIG_KEY_AMOUNT}. When {@link #CONFIG_KEY_NEXO} is present, stacks are rebuilt
 * with Nexo's {@code itemFromId} first so rewarded items match Nexo-generated ones (better stacking).</p>
 */
public final class RawCompressedNbtAdaptedItem implements AdaptedItem, Writeable {

    public static final String CONFIG_KEY         = "RawCompressedNbt";
    public static final String CONFIG_KEY_AMOUNT  = "RawCompressedNbtAmount";
    public static final String CONFIG_KEY_NEXO    = "NexoRebuildId";

    private final String  compressed;
    private final int     giveAmount;
    private final String  nexoRebuildId;

    public RawCompressedNbtAdaptedItem(@NotNull String compressed, int giveAmount) {
        this(compressed, giveAmount, null);
    }

    public RawCompressedNbtAdaptedItem(@NotNull String compressed, int giveAmount, @Nullable String nexoRebuildId) {
        this.compressed = compressed;
        this.giveAmount = Math.max(1, giveAmount);
        String trim = nexoRebuildId == null ? null : nexoRebuildId.trim();
        this.nexoRebuildId = trim != null && !trim.isEmpty() ? trim : null;
    }

    public @NotNull String compressed() {
        return compressed;
    }

    public int giveAmount() {
        return giveAmount;
    }

    public @Nullable String nexoRebuildId() {
        return nexoRebuildId;
    }

    public static @NotNull Optional<AdaptedItem> tryRead(@NotNull FileConfig config, @NotNull String path) {
        if (!config.contains(path + "." + CONFIG_KEY)) {
            return Optional.empty();
        }
        String raw = config.getString(path + "." + CONFIG_KEY);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        int amount = config.getInt(path + "." + CONFIG_KEY_AMOUNT, -1);
        if (amount < 1) {
            ItemStack legacy = ItemNbt.decompress(raw);
            amount = legacy != null ? Math.max(1, legacy.getAmount()) : 1;
        }
        String nexoId = config.getString(path + "." + CONFIG_KEY_NEXO, null);
        return Optional.of(new RawCompressedNbtAdaptedItem(raw, amount, nexoId));
    }

    @Override
    public @NotNull ItemAdapter<?> getAdapter() {
        return VanillaItemAdapter.INSTANCE;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        if (nexoRebuildId != null) {
            ItemStack canonical = NexoHook.buildSingleton(nexoRebuildId);
            if (canonical != null && !canonical.getType().isAir()) {
                canonical.setAmount(giveAmount);
                return canonical.clone();
            }
        }
        ItemStack stack = ItemNbt.decompress(compressed);
        if (stack == null || stack.getType().isAir()) {
            return CrateUtils.getQuestionStack();
        }
        stack.setAmount(giveAmount);
        return stack.clone();
    }

    @Override
    public @NotNull Optional<ItemStack> itemStack() {
        return Optional.of(getItemStack());
    }

    @Override
    public boolean isSimilar(ItemStack other) {
        if (other == null || other.getType().isAir()) {
            return false;
        }
        if (nexoRebuildId != null && NexoHook.isReflectReady()) {
            ItemStack canon = NexoHook.buildSingleton(nexoRebuildId);
            if (canon != null) {
                ItemStack oneOther = other.clone();
                oneOther.setAmount(1);
                return canon.isSimilar(oneOther);
            }
        }
        ItemStack self = ItemNbt.decompress(compressed);
        if (self == null) {
            return false;
        }
        ItemStack oneOther = other.clone();
        oneOther.setAmount(1);
        self.setAmount(1);
        return self.isSimilar(oneOther);
    }

    @Override
    public boolean isValid() {
        if (nexoRebuildId != null) {
            ItemStack nx = NexoHook.buildSingleton(nexoRebuildId);
            if (nx != null && !nx.getType().isAir()) {
                return true;
            }
        }
        ItemStack stack = ItemNbt.decompress(compressed);
        return stack != null && !stack.getType().isAir();
    }

    @Override
    public int getAmount() {
        return giveAmount;
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + "." + CONFIG_KEY, compressed);
        if (giveAmount != 1) {
            config.set(path + "." + CONFIG_KEY_AMOUNT, giveAmount);
        }
        else {
            config.remove(path + "." + CONFIG_KEY_AMOUNT);
        }
        if (nexoRebuildId != null) {
            config.set(path + "." + CONFIG_KEY_NEXO, nexoRebuildId);
        }
        else {
            config.remove(path + "." + CONFIG_KEY_NEXO);
        }
    }
}
