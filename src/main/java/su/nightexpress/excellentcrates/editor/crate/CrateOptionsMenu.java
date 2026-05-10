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
import su.nightexpress.excellentcrates.config.Config;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.crate.CrateDialogs;
import su.nightexpress.excellentcrates.dialog.DialogRegistry;
import su.nightexpress.excellentcrates.dialog.crate.CrateItemDialog;
import su.nightexpress.excellentcrates.util.CrateUtils;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;
import su.nightexpress.nightcore.util.time.TimeFormats;

import java.util.stream.IntStream;

import static su.nightexpress.excellentcrates.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class CrateOptionsMenu extends LinkedMenu<CratesPlugin, Crate> implements LangContainer {

    private static final IconLocale LOCALE_DELETE = LangEntry.iconBuilder("Editor.Button.Crate.Delete").accentColor(RED).name("Supprimer la caisse")
        .appendInfo("Supprime définitivement la caisse.").br()
        .appendClick("Maintenez [" + TagWrappers.KEY.apply("key.drop") + "] pour supprimer")
        .build();

    private static final IconLocale LOCALE_NAME = LangEntry.iconBuilder("Editor.Button.Crate.DisplayName").name("Nom affiché")
        .appendCurrent("Actuel", CRATE_NAME).br()
        .appendInfo("Définit le nom affiché de la caisse.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private static final IconLocale LOCALE_DESCRIPTION = LangEntry.iconBuilder("Editor.Button.Crate.Description").name("Description")
        .rawLore(CRATE_DESCRIPTION, EMPTY_IF_ABOVE)
        .appendInfo("Définit la description de la caisse.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private static final IconLocale LOCALE_ITEM = LangEntry.iconBuilder("Editor.Button.Crate.Item").name("Item de caisse")
        .appendCurrent("Statut", GENERIC_INSPECTION)
        .appendCurrent("Empilable", GENERIC_STATE).br()
        .appendInfo("Déposez un item sur " + SOFT_YELLOW.wrap("ce") + " bouton pour", "remplacer l'item de la caisse.").br()
        .appendClick("Cliquer pour basculer l'empilage")
        .build();

    private static final IconLocale LOCALE_PREVIEW_SET = LangEntry.iconBuilder("Editor.Button.Crate.Preview.Set").name("GUI d'aperçu")
        .appendCurrent("Statut", GENERIC_INSPECTION)
        .appendCurrent("ID d'aperçu", GENERIC_VALUE).br()
        .appendInfo("Définit l'interface d'aperçu de la caisse.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private static final IconLocale LOCALE_PREVIEW_UNSET = LangEntry.iconBuilder("Editor.Button.Crate.Preview.Unset").name("GUI d'aperçu")
        .appendCurrent("Statut", RED.wrap("Désactivé")).br()
        .appendInfo("Définit l'interface d'aperçu pour cette caisse.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private static final IconLocale LOCALE_OPENING_SET = LangEntry.iconBuilder("Editor.Button.Crate.Opening.Set").name("Animation d'ouverture")
        .appendCurrent("Statut", GENERIC_INSPECTION)
        .appendCurrent("Configuration", GENERIC_VALUE).br()
        .appendInfo("Définit l'animation d'ouverture de la caisse.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private static final IconLocale LOCALE_OPENING_UNSET = LangEntry.iconBuilder("Editor.Button.Crate.Opening.Unset").name("Animation d'ouverture")
        .appendCurrent("Statut", RED.wrap("Désactivé")).br()
        .appendInfo("Définit l'animation d'ouverture pour cette caisse.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private static final IconLocale LOCALE_LINKED_BLOCKS = LangEntry.iconBuilder("Editor.Button.Crate.LinkedBlocks")
        .name("Bloc lié")
        .rawLore(DARK_GRAY.wrap("Touche " + GOLD.wrap("[" + TagWrappers.KEY.apply("key.drop") + "]") + " pour dissocier.")).br()
        .appendCurrent("Lié", GENERIC_STATE).br()
        .appendInfo("Liez la caisse à un bloc avec", "l'outil de liaison.").br()
        .appendInfo("Interagir avec le bloc lié permet", "de prévisualiser et d'ouvrir la caisse.").br()
        .appendClick("Cliquer pour obtenir l'outil de liaison")
        .build();

    private static final IconLocale LOCALE_BLOCK_PUSHBACK = LangEntry.iconBuilder("Editor.Button.Crate.BlockPushback")
        .name("Refoulement du bloc")
        .appendCurrent("Statut", GENERIC_STATE).br()
        .appendInfo("Éloigne le joueur du bloc de caisse", "s'il ne remplit pas les conditions.").br()
        .appendClick("Cliquer pour basculer")
        .build();

    private static final IconLocale LOCALE_COST_OPTIONS = LangEntry.iconBuilder("Editor.Button.Crate.CostOptions").name("Options de coût")
        .appendInfo("Définissez le " + SOFT_YELLOW.wrap("« coût »") + " pour ouvrir la caisse.", "Ce peut être des " + SOFT_YELLOW.wrap("clés") + ", de la " + SOFT_YELLOW.wrap("monnaie") + ", ou les deux.").br()
        .appendInfo("Ajoutez plusieurs options de coût", "pour laisser les joueurs choisir", "comment ils ouvrent la caisse.").br()
        .appendClick("Cliquer pour ouvrir")
        .build();

    private static final IconLocale LOCALE_PERMISSION_REQUIREMENT = LangEntry.iconBuilder("Editor.Button.Crate.Permission").name("Permission requise")
        .appendCurrent("Statut", GENERIC_STATE)
        .appendCurrent("Permission", GENERIC_VALUE).br()
        .appendInfo("Définit si une permission est requise", "pour ouvrir la caisse.").br()
        .appendClick("Cliquer pour basculer")
        .build();

    private static final IconLocale LOCALE_OPEN_LIMITS = LangEntry.iconBuilder("Editor.Button.Crate.OpeningCooldown").name("Limites d'ouverture")
        .appendCurrent("Statut", GENERIC_STATE)
        .appendCurrent("Temps de recharge", GENERIC_VALUE)
        .appendCurrent("Nombre max.", GENERIC_AMOUNT).br()
        .appendInfo("Nombre d'ouvertures par joueur", "sur une période donnée.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private static final IconLocale LOCALE_EFFECT = LangEntry.iconBuilder("Editor.Button.Crate.Effect").name("Effets de bloc")
        .appendCurrent("Modèle", GENERIC_TYPE)
        .appendCurrent("Particule", GENERIC_VALUE).br()
        .appendInfo("Effet de particules autour du bloc de caisse.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    public static final IconLocale LOCALE_HOLOGRAM = LangEntry.iconBuilder("Editor.Button.Crate.Hologram").name("Hologramme de caisse")
        .appendCurrent("Statut", GENERIC_INSPECTION)
        .appendCurrent("État", GENERIC_STATE)
        .appendCurrent("Modèle", GENERIC_VALUE).br()
        .appendInfo("Hologramme automatique au-dessus du", "bloc de caisse lié.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private static final IconLocale LOCALE_REWARDS = LangEntry.iconBuilder("Editor.Button.Crate.Rewards").name("Récompenses")
        .appendCurrent("Statut", GENERIC_INSPECTION)
        .appendCurrent("Récompenses", GENERIC_AMOUNT).br()
        .appendInfo("Ajoutez et gérez les récompenses.").br()
        .appendClick("Cliquer pour ouvrir")
        .build();

    private static final IconLocale LOCALE_MILESTONES = LangEntry.iconBuilder("Editor.Button.Crate.Milestones").name("Paliers")
        .appendCurrent("Paliers", GENERIC_AMOUNT).br()
        .appendInfo("Paliers personnalisés avec", "récompenses pour cette caisse.").br()
        .appendClick("Cliquer pour ouvrir")
        .build();

    private static final IconLocale LOCALE_POST_OPEN_COMMANDS = LangEntry.iconBuilder("Editor.Button.Crate.Post-Open-Commands").name("Commandes post-ouverture")
        .appendCurrent("Statut", GENERIC_INSPECTION)
        .appendCurrent("Commandes", GENERIC_AMOUNT).br()
        .appendInfo("Exécute les commandes listées", "à chaque ouverture de caisse.").br()
        .appendClick("Cliquer pour modifier")
        .build();

    private final DialogRegistry dialogs;

    public CrateOptionsMenu(@NotNull CratesPlugin plugin, @NotNull DialogRegistry dialogs) {
        super(plugin, MenuType.GENERIC_9X6, Lang.EDITOR_TITLE_CRATE_SETTINGS.text());
        this.dialogs = dialogs;
        this.plugin.injectLang(this);

        this.addItem(EditorReturnButton.menuItem(49, (viewer, event) -> {
            this.runNextTick(() -> this.plugin.getEditorManager().openCrateList(viewer.getPlayer()));
        }).build());

        this.addItem(MenuItem.background(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(45, 54).toArray()));
        this.addItem(MenuItem.background(Material.GLASS_PANE, IntStream.range(19, 26).toArray()));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        Crate crate = this.getLink(player);
        Runnable flush = () -> this.flush(player);

        viewer.addItem(NightItem.fromType(Material.NAME_TAG)
            .localized(LOCALE_NAME)
            .replacement(replacer -> replacer.replace(crate.replacePlaceholders()))
            .toMenuItem().setSlots(10).setHandler((viewer1, event) -> {
                this.dialogs.show(player, CrateDialogs.CRATE_NAME, crate, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.WRITABLE_BOOK)
            .localized(LOCALE_DESCRIPTION)
            .replacement(replacer -> replacer.replace(crate.replacePlaceholders()))
            .toMenuItem().setSlots(11).setHandler((viewer1, event) -> {
                this.dialogs.show(player, CrateDialogs.CRATE_DESCRIPTION, crate, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromItemStack(crate.getItemStack())
            .localized(LOCALE_ITEM)
            .replacement(replacer -> replacer
                .replace(GENERIC_INSPECTION, () -> Lang.inspection(Lang.INSPECTIONS_GENERIC_ITEM, crate.getItem().isValid()))
                .replace(GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(crate.isItemStackable()))
            )
            .toMenuItem().setSlots(12).setHandler((viewer1, event) -> {
                ItemStack cursor = event.getCursor();
                if (cursor == null || cursor.getType().isAir()) {
                    if (event.isLeftClick()) {
                        crate.setItemStackable(!crate.isItemStackable());
                        crate.markDirty();
                        this.runNextTick(flush);
                    }
                    return;
                }

                // Remove crate tags to avoid infinite recursion in ItemProvider.
                ItemStack clean = CrateUtils.removeCrateTags(new ItemStack(cursor));
                Players.addItem(player, cursor);
                event.getView().setCursor(null);
                this.dialogs.show(player, CrateDialogs.CRATE_ITEM, new CrateItemDialog.Data<>(crate, clean), flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.PAINTING)
            .localized(crate.isPreviewEnabled() ? LOCALE_PREVIEW_SET : LOCALE_PREVIEW_UNSET)
            .replacement(replacer -> replacer
                .replace(GENERIC_INSPECTION, () -> Lang.inspection(Lang.INSPECTIONS_CRATE_PREVIEW, crate.isPreviewValid()))
                .replace(GENERIC_VALUE, crate::getPreviewId)
            )
            .toMenuItem().setSlots(13).setHandler((viewer1, event) -> {
                this.dialogs.show(player, CrateDialogs.CRATE_PREVIEW, crate, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.GLOW_ITEM_FRAME)
            .localized(crate.isOpeningEnabled() ? LOCALE_OPENING_SET : LOCALE_OPENING_UNSET)
            .replacement(replacer -> replacer
                .replace(GENERIC_INSPECTION, () -> Lang.inspection(Lang.INSPECTIONS_CRATE_OPENING, crate.isOpeningValid()))
                .replace(GENERIC_VALUE, crate::getOpeningId)
            )
            .toMenuItem().setSlots(14).setHandler((viewer1, event) -> {
                this.dialogs.show(player, CrateDialogs.CRATE_OPENING, crate, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.BEACON)
            .localized(LOCALE_LINKED_BLOCKS)
            .replacement(replacer -> replacer.replace(GENERIC_STATE, () -> CoreLang.STATE_YES_NO.get(!crate.getBlockPositions().isEmpty())))
            .toMenuItem().setSlots(15).setHandler((viewer1, event) -> {
                if (event.getClick() == ClickType.DROP) {
                    crate.removeHologram();
                    crate.clearBlockPositions();
                    crate.markDirty();
                    this.runNextTick(flush);
                    return;
                }

                this.plugin.getCrateManager().giveLinkTool(player, crate);
                this.runNextTick(player::closeInventory);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.SLIME_BLOCK)
            .localized(LOCALE_BLOCK_PUSHBACK)
            .replacement(replacer -> replacer.replace(GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(crate.isPushbackEnabled())))
            .toMenuItem().setSlots(16).setHandler((viewer1, event) -> {
                crate.setPushbackEnabled(!crate.isPushbackEnabled());
                crate.markDirty();
                this.runNextTick(flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.TRIAL_KEY)
            .localized(LOCALE_COST_OPTIONS)
            .toMenuItem().setSlots(28).setHandler((viewer1, event) -> {
                this.runNextTick(() -> plugin.getEditorManager().openCosts(viewer.getPlayer(), crate));
            }).build()
        );

        viewer.addItem(NightItem.fromType(crate.isPermissionRequired() ? Material.REDSTONE : Material.GUNPOWDER)
            .localized(LOCALE_PERMISSION_REQUIREMENT)
            .replacement(replacer -> replacer
                .replace(GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(crate.isPermissionRequired()))
                .replace(GENERIC_VALUE, crate::getPermission)
            )
            .toMenuItem().setSlots(29).setHandler((viewer1, event) -> {
                crate.setPermissionRequired(!crate.isPermissionRequired());
                crate.markDirty();
                this.runNextTick(flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.CLOCK)
            .localized(LOCALE_OPEN_LIMITS)
            .replacement(replacer -> replacer
                .replace(GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(crate.isOpeningCooldownEnabled()))
                .replace(GENERIC_VALUE, () -> {
                    if (crate.getOpeningCooldownTime() < 0L) return CoreLang.OTHER_ONE_TIMED.text();

                    return TimeFormats.toLiteral(crate.getOpeningCooldownTime() * 1000L);
                })
                .replace(GENERIC_AMOUNT, () -> String.valueOf(crate.getOpeningLimitAmount()))
            )
            .toMenuItem().setSlots(30).setHandler((viewer1, event) -> {
                this.dialogs.show(player, CrateDialogs.CRATE_OPENING_LIMITS, crate, flush);
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.BLAZE_POWDER)
            .localized(LOCALE_EFFECT)
            .replacement(replacer -> replacer
                .replace(GENERIC_TYPE, () -> StringUtil.capitalizeUnderscored(crate.getEffectType()))
                .replace(GENERIC_VALUE, () -> Lang.PARTICLE.getLocalized(crate.getEffectParticle().getParticle())))
            .toMenuItem().setSlots(31).setHandler((viewer1, event) -> {
                this.dialogs.show(player, CrateDialogs.CRATE_EFFECT, crate, flush);
            }).build()
        );

        if (this.plugin.hasHolograms()) {
            viewer.addItem(NightItem.fromType(Material.ARMOR_STAND)
                .localized(LOCALE_HOLOGRAM)
                .replacement(replacer -> replacer
                    .replace(GENERIC_INSPECTION, () -> Lang.inspection(Lang.INSPECTIONS_CRATE_HOLOGRAM, crate.isHologramTemplateValid()))
                    .replace(GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(crate.isHologramEnabled()))
                    .replace(GENERIC_VALUE, crate::getHologramTemplateId)
                )
                .toMenuItem().setSlots(32).setHandler((viewer1, event) -> {
                    this.dialogs.show(player, CrateDialogs.CRATE_HOLOGRAM, crate, flush);
                }).build()
            );
        }

        viewer.addItem(NightItem.fromType(Material.VAULT)
            .localized(LOCALE_REWARDS)
            .replacement(replacer -> replacer
                .replace(GENERIC_INSPECTION, () -> Lang.inspection(Lang.INSPECTIONS_GENERIC_OVERVIEW, crate.getRewards().stream().noneMatch(Reward::hasProblems)))
                .replace(GENERIC_AMOUNT, () -> CoreLang.formatEntry(String.valueOf(crate.countRewards()), crate.countRewards() > 0))
            )
            .toMenuItem().setSlots(33).setHandler((viewer1, event) -> {
                this.runNextTick(() -> this.plugin.getEditorManager().openRewardList(viewer.getPlayer(), crate));
            }).build()
        );

        if (Config.isMilestonesEnabled()) {
            viewer.addItem(NightItem.fromType(Material.CAMPFIRE)
                .localized(LOCALE_MILESTONES)
                .replacement(replacer -> replacer
                    .replace(GENERIC_AMOUNT, () -> String.valueOf(crate.countMilestones()))
                )
                .toMenuItem().setSlots(34).setHandler((viewer1, event) -> {
                    // TODO crate.setMilestonesRepeatable(!crate.isMilestonesRepeatable());
                    this.runNextTick(() -> this.plugin.getEditorManager().openMilestones(viewer.getPlayer(), crate));
                }).build()
            );
        }

        viewer.addItem(NightItem.fromType(Material.BARRIER)
            .localized(LOCALE_DELETE)
            .toMenuItem().setSlots(53).setHandler((viewer1, event) -> {
                if (event.getClick() != ClickType.DROP) return;

                this.plugin.getCrateManager().delete(crate);
                this.runNextTick(() -> this.plugin.getEditorManager().openCrateList(player));
            }).build()
        );

        viewer.addItem(NightItem.fromType(Material.COMMAND_BLOCK)
            .localized(LOCALE_POST_OPEN_COMMANDS)
            .replacement(replacer -> replacer
                .replace(GENERIC_INSPECTION, () -> Lang.inspection(Lang.INSPECTIONS_GENERIC_COMMANDS, crate.getPostOpenCommands().stream().allMatch(CrateUtils::isValidCommand)))
                .replace(GENERIC_AMOUNT, () -> String.valueOf(crate.getPostOpenCommands().size()))
            )
            .toMenuItem().setSlots(40).setHandler((viewer1, event) -> {
                this.dialogs.show(player, CrateDialogs.CRATE_POST_OPEN_COMMANDS, crate, flush);
            }).build()
        );
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);
        if (result.isInventory() && !event.isShiftClick()) {
            event.setCancelled(false);
        }
    }
}
