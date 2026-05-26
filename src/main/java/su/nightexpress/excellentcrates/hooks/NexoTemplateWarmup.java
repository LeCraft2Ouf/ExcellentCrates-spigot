package su.nightexpress.excellentcrates.hooks;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.api.crate.Reward;
import su.nightexpress.excellentcrates.crate.CrateManager;
import su.nightexpress.excellentcrates.crate.cost.Cost;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.crate.reward.impl.ItemReward;
import su.nightexpress.excellentcrates.key.CrateKey;
import su.nightexpress.excellentcrates.key.KeyManager;
import su.nightexpress.excellentcrates.util.NexoIdOnlyAdaptedItem;
import su.nightexpress.excellentcrates.util.RawCompressedNbtAdaptedItem;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Pre-builds {@link NexoHook} singleton templates for every Nexo id referenced by loaded crates and keys so
 * GUIs and rewards do not hit Nexo cold after deleting {@code nexo_template_cache} or on first tick before Nexo registers items.
 */
public final class NexoTemplateWarmup implements Listener {

    private static final long[] RETRY_DELAYS_TICKS = { 1L, 20L, 60L, 100L };

    private final CratesPlugin plugin;

    /** Avoid spamming the same full-success line on every retry tick. */
    private boolean loggedFullWarmup;

    public NexoTemplateWarmup(@NotNull CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers delayed warmup passes and re-runs when Nexo enables (late load / nexo reload).
     */
    public static void setup(@NotNull CratesPlugin plugin) {
        NexoTemplateWarmup listener = new NexoTemplateWarmup(plugin);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        listener.scheduleDelayedPasses();
    }

    private void scheduleDelayedPasses() {
        for (long delay : RETRY_DELAYS_TICKS) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> runOnce(false), delay);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginEnable(@NotNull PluginEnableEvent event) {
        if (!event.getPlugin().getName().equalsIgnoreCase("Nexo")) {
            return;
        }
        NexoHook.clearSingletonTemplateCache();
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> runOnce(true), 1L);
    }

    private void runOnce(@SuppressWarnings("unused") boolean nexoJustEnabled) {
        if (!NexoHook.isReflectReady()) {
            return;
        }
        LinkedHashSet<String> ids = collectReferencedNexoIds();
        if (ids.isEmpty()) {
            return;
        }
        int warmed = NexoHook.warmSingletonTemplates(ids);
        if (warmed > 0 && warmed == ids.size() && !this.loggedFullWarmup) {
            this.loggedFullWarmup = true;
            this.plugin.getLogger().info("[Nexo] ExcellentCrates warmed " + warmed + "/" + ids.size() + " template stack(s) for crate/key previews.");
        }
    }

    static @NotNull LinkedHashSet<String> collectReferencedNexoIds(@NotNull CratesPlugin plugin) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        CrateManager crates = plugin.getCrateManager();
        if (crates != null) {
            for (Crate crate : crates.getCrates()) {
                collectFromAdapted(crate.getItem(), ids);
                for (Cost cost : crate.getCosts()) {
                    collectFromAdapted(cost.getIcon(), ids);
                }
                for (Reward reward : crate.getRewards()) {
                    collectFromAdapted(reward.getPreview(), ids);
                    if (reward instanceof ItemReward itemReward) {
                        for (AdaptedItem adapted : itemReward.getItems()) {
                            collectFromAdapted(adapted, ids);
                        }
                    }
                }
            }
        }
        KeyManager keys = plugin.getKeyManager();
        if (keys != null) {
            for (CrateKey key : keys.getKeys()) {
                collectFromAdapted(key.getItem(), ids);
                ItemStack stack = key.getItemStack(false);
                NexoHook.resolveNexoRebuildId(stack).ifPresent(ids::add);
            }
        }
        return ids;
    }

    private @NotNull LinkedHashSet<String> collectReferencedNexoIds() {
        return collectReferencedNexoIds(this.plugin);
    }

    static void collectFromAdapted(@NotNull AdaptedItem adapted, @NotNull Set<String> ids) {
        if (adapted instanceof NexoIdOnlyAdaptedItem nexoOnly) {
            ids.add(nexoOnly.nexoItemId());
            return;
        }
        if (adapted instanceof RawCompressedNbtAdaptedItem raw) {
            String nid = raw.nexoRebuildId();
            if (nid != null && !nid.isBlank()) {
                ids.add(nid.trim());
            }
            return;
        }
        adapted.itemStack().ifPresent(stack -> NexoHook.resolveNexoRebuildId(stack).ifPresent(ids::add));
    }
}
