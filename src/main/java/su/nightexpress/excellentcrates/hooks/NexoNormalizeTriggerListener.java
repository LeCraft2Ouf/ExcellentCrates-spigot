package su.nightexpress.excellentcrates.hooks;

import org.bukkit.Bukkit;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.Placeholders;
import su.nightexpress.excellentcrates.config.Config;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.nightcore.manager.AbstractListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs {@link NexoInventoryNormalizer} on join, container use, and inventory close (virtual GUIs).
 */
public final class NexoNormalizeTriggerListener extends AbstractListener<CratesPlugin> {

    /** Debounce rapid inventory close/reopen (Skript menus) to a single delayed normalize. */
    private final Map<UUID, BukkitTask> pendingCloseNormalize = new ConcurrentHashMap<>();

    public NexoNormalizeTriggerListener(@NotNull CratesPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        BukkitTask t = this.pendingCloseNormalize.remove(event.getPlayer().getUniqueId());
        if (t != null) {
            t.cancel();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!Config.NEXO_NORMALIZE_LEGACY_STACKS_ON_JOIN.get()) {
            return;
        }
        Player player = event.getPlayer();
        long delay = Math.max(0L, Config.NEXO_NORMALIZE_LEGACY_STACKS_DELAY_TICKS.get().longValue());
        scheduleNormalize(player, delay);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!Config.NEXO_NORMALIZE_LEGACY_STACKS_ON_CONTAINER_OPEN.get()) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!isContainerViewForNormalize(event.getView())) {
            return;
        }
        scheduleNormalize(player, interactDelayTicks());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!Config.NEXO_NORMALIZE_LEGACY_STACKS_AFTER_CONTAINER_CLICK.get()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getView().getBottomInventory() != player.getInventory()) {
            return;
        }
        if (!isContainerViewForNormalize(event.getView())) {
            return;
        }
        scheduleNormalize(player, interactDelayTicks());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!Config.NEXO_NORMALIZE_LEGACY_STACKS_ON_INVENTORY_CLOSE.get()) {
            return;
        }
        HumanEntity viewer = event.getPlayer();
        if (!(viewer instanceof Player player)) {
            return;
        }
        UUID id = player.getUniqueId();
        BukkitTask previous = this.pendingCloseNormalize.remove(id);
        if (previous != null) {
            previous.cancel();
        }
        long delay = interactDelayTicks();
        BukkitTask[] slot = new BukkitTask[1];
        slot[0] = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.pendingCloseNormalize.remove(id, slot[0]);
            runNormalizeNow(player);
        }, delay);
        this.pendingCloseNormalize.put(id, slot[0]);
    }

    private static long interactDelayTicks() {
        return Math.max(0L, Config.NEXO_NORMALIZE_LEGACY_STACKS_INTERACT_DELAY_TICKS.get().longValue());
    }

    /**
     * Block-backed storage UIs and ender chest (holder is often the player, not {@link org.bukkit.inventory.BlockInventoryHolder}).
     */
    static boolean isContainerViewForNormalize(@NotNull InventoryView view) {
        Inventory top = view.getTopInventory();
        if (top.getType() == InventoryType.ENDER_CHEST) {
            return true;
        }
        return isPhysicalStorageHolder(top.getHolder());
    }

    private static boolean isPhysicalStorageHolder(@Nullable InventoryHolder holder) {
        if (holder == null) {
            return false;
        }
        if (holder instanceof DoubleChest) {
            return true;
        }
        return holder instanceof BlockInventoryHolder;
    }

    private void scheduleNormalize(@NotNull Player player, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> runNormalizeNow(player), delayTicks);
    }

    private void runNormalizeNow(@NotNull Player player) {
        if (!player.isOnline()) {
            return;
        }
        int n = NexoInventoryNormalizer.normalize(player, this.plugin.getKeyManager());
        if (n > 0 && Config.NEXO_NORMALIZE_LEGACY_STACKS_NOTIFY_PLAYER.get()) {
            Lang.COMMAND_NEXO_NORMALIZE_PLAYER_NOTIFY.message().send(player, replacer -> replacer
                .replace(Placeholders.GENERIC_AMOUNT, n));
        }
    }
}
