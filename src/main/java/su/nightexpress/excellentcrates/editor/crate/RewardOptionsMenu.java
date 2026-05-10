package su.nightexpress.excellentcrates.editor.crate;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.editor.EditorReturnButton;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.api.crate.Reward;
import su.nightexpress.excellentcrates.api.crate.RewardType;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.crate.reward.impl.CommandReward;
import su.nightexpress.excellentcrates.crate.reward.impl.ItemReward;
import su.nightexpress.excellentcrates.dialog.DialogRegistry;
import su.nightexpress.excellentcrates.crate.reward.RewardDialogs;
import su.nightexpress.excellentcrates.dialog.reward.RewardPreviewDialog;
import su.nightexpress.excellentcrates.util.ItemHelper;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.stream.IntStream;

import static su.nightexpress.excellentcrates.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardOptionsMenu extends LinkedMenu<CratesPlugin, Reward> implements LangContainer {

    private static final IconLocale LOCALE_ITEMS = LangEntry.iconBuilder("Editor.Button.Reward.Items").name("Objets à donner")
        .appendCurrent("Statut", GENERIC_INSPECTION)
        .appendCurrent("Objets", GENERIC_AMOUNT).br()
        .appendInfo("Donne les objets listés lorsqu'elle est remportée.").br()
        .appendClick("Cliquer pour ouvrir")
        .build();

    private static final IconLocale LOCALE_COMMANDS = LangEntry.iconBuilder("Editor.Button.Reward.Commands").name("Commandes à exécuter")
        .appendCurrent("Statut", GENERIC_INSPECTION)
        .appendCurrent("Commandes", GENERIC_AMOUNT).br()
        .appendInfo("Exécute les commandes listées lorsqu'elle est remportée.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private static final IconLocale LOCALE_PREVIEW_NORMAL = LangEntry.iconBuilder("Editor.Button.Reward.PreviewNormal")
        .name("Aperçu")
        .appendCurrent("Statut", GENERIC_INSPECTION).br()
        .appendInfo("Posez un objet sur " + SOFT_YELLOW.wrap("ce") + " bouton", "pour remplacer l'aperçu de la récompense.")
        .build();

    private static final IconLocale LOCALE_PREVIEW_CUSTOM = LangEntry.iconBuilder("Editor.Button.Reward.PreviewCustom")
        .name("Aperçu")
        .appendCurrent("Statut", GENERIC_INSPECTION)
        .appendCurrent("Personnalisé", GENERIC_STATE).br()
        .appendInfo("Posez un objet sur " + SOFT_YELLOW.wrap("ce") + " bouton", "pour remplacer l'aperçu de la récompense.").br()
        .appendClick("Cliquer pour basculer l'aperçu personnalisé")
        .build();

    public static final IconLocale LOCALE_RARIRY_WEIGHT = LangEntry.iconBuilder("Editor.Button.Reward.RarityWeight")
        .name("Chance du tirage")
        .appendCurrent("Chance affichée", SOFT_GREEN.wrap(REWARD_ROLL_CHANCE + "%"))
        .appendCurrent("Poids (fichier)", REWARD_WEIGHT).br()
        .appendClick("Cliquer pour modifier")
        .build();

    public static final IconLocale LOCALE_NAME = LangEntry.iconBuilder("Editor.Button.Reward.Name")
        .name("Nom affiché")
        .appendCurrent("Actuel", REWARD_NAME).br()
        .appendClick("Cliquer pour modifier")
        .build();

    public static final IconLocale LOCALE_DESCRIPTION = LangEntry.iconBuilder("Editor.Button.Reward.Description")
        .name("Description")
        .rawLore(REWARD_DESCRIPTION, EMPTY_IF_ABOVE)
        .appendClick("Cliquer pour modifier")
        .build();

    public static final IconLocale LOCALE_BROADCAST = LangEntry.iconBuilder("Editor.Button.Reward.Broadcast")
        .name("Annonce du gain")
        .appendCurrent("État", GENERIC_STATE).br()
        .appendInfo("Diffuse un message lorsque cette récompense", "est remportée.").br()
        .appendClick("Cliquer pour basculer")
        .build();

    public static final IconLocale LOCALE_PERMISSIONS = LangEntry.iconBuilder("Editor.Button.Reward.Permissions")
        .name("Permissions")
        .appendCurrent("Permissions au total", GENERIC_AMOUNT).br()
        .appendInfo("Restreint l'accès à la récompense selon", "les permissions du joueur.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    public static final IconLocale LOCALE_LIMITS = LangEntry.iconBuilder("Editor.Button.Reward.Limits")
        .name("Limites")
        .appendCurrent("État", GENERIC_STATE).br()
        .appendInfo("Contrôle à quelle fréquence et combien", "de fois cette récompense peut être gagnée.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    public static final IconLocale LOCALE_DELETE = LangEntry.iconBuilder("Editor.Button.Reward.Delete").accentColor(SOFT_RED)
        .name("Supprimer la récompense")
        .appendInfo("Supprime définitivement la récompense.").br()
        .appendClick("Maintenir " + TagWrappers.KEY.apply("key.drop") + " pour supprimer")
        .build();

    private final DialogRegistry dialogs;

    public RewardOptionsMenu(@NotNull CratesPlugin plugin, @NotNull DialogRegistry dialogs) {
        super(plugin, MenuType.GENERIC_9X6, Lang.EDITOR_TITLE_REWARD_SETTINGS.text());
        this.dialogs = dialogs;
        this.plugin.injectLang(this);

        this.addItem(EditorReturnButton.menuItem(49, (viewer, event) -> {
            this.runNextTick(() -> plugin.getEditorManager().openRewardList(viewer.getPlayer(), this.getLink(viewer).getCrate()));
        }).build());

        this.addItem(MenuItem.background(Material.GLASS_PANE, 19,20,21,22,23,24,25));
        this.addItem(MenuItem.background(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(45, 54).toArray()));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        Reward reward = this.getLink(player);
        Crate crate = reward.getCrate();
        Runnable flush = () -> this.flush(player);

        viewer.addItem(NightItem.fromItemStack(reward.getPreviewItem())
            .localized(reward.getType() == RewardType.ITEM ? LOCALE_PREVIEW_CUSTOM : LOCALE_PREVIEW_NORMAL)
            .replacement(replacer -> replacer
                .replace(GENERIC_INSPECTION, () -> Lang.inspection(Lang.INSPECTIONS_REWARD_PREVIEW, reward.getPreview().isValid()))
                .replace(GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(reward instanceof ItemReward itemReward && itemReward.isCustomPreview()))
            )
            .toMenuItem().setSlots(11).setHandler((viewer1, event) -> {
                ItemStack cursor = event.getCursor();
                if (cursor == null || cursor.getType().isAir()) {
                    if (reward instanceof ItemReward itemReward) {
                        itemReward.setCustomPreview(!itemReward.isCustomPreview());
                        crate.markDirty();
                        this.runNextTick(flush);
                    }
                    return;
                }

                ItemStack copy = new ItemStack(cursor);
                Players.addItem(player, copy);
                event.getView().setCursor(null);

                if (!ItemHelper.isCustom(copy)) {
                    reward.setPreview(ItemHelper.vanilla(copy));
                    crate.markDirty();
                    this.runNextTick(flush);
                }
                else {
                    this.dialogs.show(player, RewardDialogs.PREVIEW, new RewardPreviewDialog.Data(reward, copy), flush);
                }
            }).build()
        );

        if (reward instanceof ItemReward itemReward) {
            viewer.addItem(NightItem.fromType(Material.BUNDLE)
                .hideAllComponents()
                .localized(LOCALE_ITEMS)
                .replacement(replacer -> replacer
                    .replace(GENERIC_INSPECTION, () -> Lang.inspection(Lang.INSPECTIONS_REWARD_ITEMS, itemReward.hasContent() && !itemReward.hasInvalidItems()))
                    .replace(GENERIC_AMOUNT, () -> itemReward.hasContent() ? CoreLang.goodEntry(String.valueOf(itemReward.countItems())) : CoreLang.badEntry(Lang.INSPECTIONS_REWARD_NO_ITEMS.text()))
                )
                .toMenuItem().setSlots(10).setHandler((viewer1, event) -> {
                    this.runNextTick(() -> plugin.getEditorManager().openRewardContent(viewer.getPlayer(), itemReward));
                }).build()
            );
        }
        else if (reward instanceof CommandReward commandReward) {
            viewer.addItem(NightItem.fromType(Material.COMMAND_BLOCK)
                .hideAllComponents()
                .localized(LOCALE_COMMANDS)
                .replacement(replacer -> replacer
                    .replace(GENERIC_INSPECTION, () -> Lang.inspection(Lang.INSPECTIONS_GENERIC_COMMANDS, commandReward.hasContent() && !commandReward.hasInvalidCommands()))
                    .replace(GENERIC_AMOUNT, () -> commandReward.hasContent() ? CoreLang.goodEntry(String.valueOf(commandReward.countCommands())) : CoreLang.badEntry(Lang.INSPECTIONS_REWARD_NO_COMMANDS.text()))
                )
                .toMenuItem().setSlots(10).setHandler((viewer1, event) -> {
                    this.dialogs.show(player, RewardDialogs.COMMANDS, commandReward, flush);
                }).build()
            );

            viewer.addItem(NightItem.fromType(Material.NAME_TAG).localized(LOCALE_NAME)
                .replacement(replacer -> replacer.replace(reward.replacePlaceholders()))
                .toMenuItem().setSlots(30).setHandler((viewer1, event) -> {
                    this.dialogs.show(player, RewardDialogs.NAME, commandReward, flush);
                }).build()
            );

            viewer.addItem(NightItem.fromType(Material.WRITABLE_BOOK).localized(LOCALE_DESCRIPTION)
                .replacement(replacer -> replacer.replace(reward.replacePlaceholders()))
                .toMenuItem().setSlots(32).setHandler((viewer1, event) -> {
                    this.dialogs.show(player, RewardDialogs.DESCRIPTION, commandReward, flush);
                }).build()
            );
        }

        viewer.addItem(NightItem.fromType(Material.GLISTERING_MELON_SLICE).localized(LOCALE_RARIRY_WEIGHT)
            .replacement(replacer -> replacer.replace(reward.replacePlaceholders()))
            .toMenuItem().setSlots(12).setHandler((viewer1, event) -> {
                this.dialogs.show(player, RewardDialogs.WEIGHT, reward, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.ENDER_PEARL).localized(LOCALE_BROADCAST)
            .replacement(replacer -> replacer.replace(GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(reward.isBroadcast())))
            .toMenuItem().setSlots(13).setHandler((viewer1, event) -> {
                reward.setBroadcast(!reward.isBroadcast());
                crate.markDirty();
                this.runNextTick(flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.REDSTONE).localized(LOCALE_PERMISSIONS)
            .replacement(replacer -> replacer.replace(GENERIC_AMOUNT, () -> String.valueOf(reward.getIgnoredPermissions().size() + reward.getRequiredPermissions().size())))
            .toMenuItem().setSlots(14).setHandler((viewer1, event) -> {
                this.dialogs.show(player, RewardDialogs.PERMISSIONS, reward, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.COMPARATOR).localized(LOCALE_LIMITS)
            .replacement(replacer -> replacer.replace(GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(reward.getLimits().isEnabled())))
            .toMenuItem().setSlots(15).setHandler((viewer1, event) -> {
                this.dialogs.show(player, RewardDialogs.LIMITS, reward, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.BARRIER).localized(LOCALE_DELETE)
            .toMenuItem().setSlots(53).setHandler((viewer1, event) -> {
                if (event.getClick() != ClickType.DROP) return;

                crate.removeReward(reward);
                crate.markDirty();
                this.runNextTick(() -> this.plugin.getEditorManager().openRewardList(player, crate));
            }).build()
        );
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);
        if (result.isInventory()) {
            event.setCancelled(false);
        }
    }
}
