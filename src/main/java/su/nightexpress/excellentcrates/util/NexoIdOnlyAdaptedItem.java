package su.nightexpress.excellentcrates.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.hooks.NexoHook;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.integration.item.adapter.impl.VanillaItemAdapter;

import java.util.Optional;

/**
 * Reward payload that stores only Nexo's logical item id — built at giveaway time via
 * {@link NexoHook#buildSingleton(String)} so the stack matches Nexo-generated items.
 */
public final class NexoIdOnlyAdaptedItem implements AdaptedItem, Writeable {

    public static final String CONFIG_KEY        = "NexoIdOnlyRebuildId";
    public static final String CONFIG_KEY_AMOUNT = "NexoIdOnlyGiveAmount";

    private final @NotNull String nexoItemId;
    private final int           giveAmount;

    public NexoIdOnlyAdaptedItem(@NotNull String nexoItemId, int giveAmount) {
        String t = nexoItemId.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("nexoItemId");
        }
        this.nexoItemId = t;
        this.giveAmount = Math.max(1, giveAmount);
    }

    public static @NotNull Optional<AdaptedItem> tryRead(@NotNull FileConfig config, @NotNull String path) {
        if (!config.contains(path + "." + CONFIG_KEY)) {
            return Optional.empty();
        }
        String id = config.getString(path + "." + CONFIG_KEY);
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        int amt = Math.max(1, config.getInt(path + "." + CONFIG_KEY_AMOUNT, 1));
        return Optional.of(new NexoIdOnlyAdaptedItem(id, amt));
    }

    public @NotNull String nexoItemId() {
        return nexoItemId;
    }

    @Override
    public @NotNull ItemAdapter<?> getAdapter() {
        return VanillaItemAdapter.INSTANCE;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        ItemStack built = NexoHook.buildSingleton(nexoItemId);
        if (built == null || built.getType().isAir()) {
            return CrateUtils.getQuestionStack();
        }
        built.setAmount(giveAmount);
        return built.clone();
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
        ItemStack canon = NexoHook.buildSingleton(nexoItemId);
        if (canon == null) {
            return false;
        }
        ItemStack one = other.clone();
        one.setAmount(1);
        return canon.isSimilar(one);
    }

    @Override
    public boolean isValid() {
        ItemStack built = NexoHook.buildSingleton(nexoItemId);
        return built != null && !built.getType().isAir();
    }

    @Override
    public int getAmount() {
        return giveAmount;
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + "." + CONFIG_KEY, nexoItemId);
        if (giveAmount != 1) {
            config.set(path + "." + CONFIG_KEY_AMOUNT, giveAmount);
        }
        else {
            config.remove(path + "." + CONFIG_KEY_AMOUNT);
        }
    }
}
