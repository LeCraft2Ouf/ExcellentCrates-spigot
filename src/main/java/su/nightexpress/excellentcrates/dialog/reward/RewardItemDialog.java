package su.nightexpress.excellentcrates.dialog.reward;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.crate.reward.impl.ItemReward;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.dialog.Dialog;
import su.nightexpress.excellentcrates.util.ItemHelper;
import su.nightexpress.excellentcrates.util.NexoIdOnlyAdaptedItem;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.BukkitThing;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardItemDialog extends Dialog<RewardItemDialog.Data> {

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Reward.Item.Title").text(title("Récompense", "Objet"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Reward.Item.Body").dialogElement(400,
        "Choisissez comment enregistrer l'objet.",
        "",
        "Pour les objets Nexo (" + SOFT_AQUA.wrap("nexo:id") + "), préférez " + SOFT_GREEN.wrap("Sauver en ID Nexo") + " — la récompense est reconstruite par Nexo et s'empile comme un objet Nexo.",
        "",
        SOFT_RED.and(BOLD).wrap("NOTE IMPORTANTE :"),
        "Si l'objet ci-dessus ne correspond pas au vôtre, utilisez " + SOFT_RED.wrap("Sauver en NBT") + ".",
        GRAY.wrap("Les données exactes seront alors enregistrées correctement.")
    );

    private static final ButtonLocale BUTTON_NBT       = LangEntry.builder("Dialog.Reward.Item.Button.NBT").button(SPRITE_NO_ATLAS.apply("item/" + BukkitThing.getValue(Material.CREEPER_SPAWN_EGG)) + " Sauver en " + SOFT_GREEN.wrap("NBT"));
    private static final ButtonLocale BUTTON_REFERENCE = LangEntry.builder("Dialog.Reward.Item.Button.Reference").button(SPRITE_NO_ATLAS.apply("item/" + BukkitThing.getValue(Material.KNOWLEDGE_BOOK)) + " Sauver en " + SOFT_GREEN.wrap("Référence"));
    private static final ButtonLocale BUTTON_NEXO      = LangEntry.builder("Dialog.Reward.Item.Button.Nexo").button(SPRITE_NO_ATLAS.apply("item/" + BukkitThing.getValue(Material.AMETHYST_SHARD)) + " Sauver en " + SOFT_GREEN.wrap("ID Nexo"));

    private static final String JSON_NBT       = "nbt";
    private static final String JSON_REFERENCE = "reference";
    private static final String JSON_NEXO      = "nexo_id";

    public record Data(@NotNull ItemReward reward, @NotNull ItemStack itemStack){}

    public void show(@NotNull Player player, @NotNull ItemReward reward, @NotNull ItemStack itemStack, @Nullable Runnable callback) {
        this.show(player, new Data(reward, itemStack), callback);
    }

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull Data data) {
        ItemReward reward = data.reward;
        Crate crate = reward.getCrate();
        ItemStack itemStack = data.itemStack;
        AdaptedItem adaptedItem = ItemHelper.adapt(itemStack);

        List<WrappedDialogInput> inputs = new ArrayList<>();

        return Dialogs.create(builder -> {

            builder.base(DialogBases.builder(TITLE)
                .body(
                    // Re-adapt custom item for more accurate preview (useful for mixed (e.g. Nexo + ExecutableItems) items only)
                    DialogBodies.item(ItemHelper.toItemStack(adaptedItem)).build(),
                    DialogBodies.plainMessage(BODY)
                )
                .inputs(inputs)
                .build()
            );

            builder.type(DialogTypes.multiAction(
                DialogButtons.action(BUTTON_NBT).action(DialogActions.customClick(JSON_NBT)).build(),
                DialogButtons.action(BUTTON_REFERENCE).action(DialogActions.customClick(JSON_REFERENCE)).build(),
                DialogButtons.action(BUTTON_NEXO).action(DialogActions.customClick(JSON_NEXO)).build()
            ).exitAction(DialogButtons.back()).build());

            builder.handleResponse(JSON_NBT, (viewer, identifier, nbtHolder) -> {
                AdaptedItem adapt = ItemHelper.adaptStoredReward(itemStack, false);
                reward.addItem(adapt);
                crate.markDirty();
                viewer.callback();
            });

            builder.handleResponse(JSON_REFERENCE, (viewer, identifier, nbtHolder) -> {
                AdaptedItem adapt = ItemHelper.adaptStoredReward(itemStack, true);
                reward.addItem(adapt);
                crate.markDirty();
                viewer.callback();
            });

            builder.handleResponse(JSON_NEXO, (viewer, identifier, nbtHolder) -> {
                NexoIdOnlyAdaptedItem nexoItem = ItemHelper.nexoIdOnly(itemStack);
                if (nexoItem == null) {
                    Lang.DIALOG_REWARD_ITEM_NO_NEXO_ID.message().send(player);
                    return;
                }
                reward.addItem(nexoItem);
                crate.markDirty();
                viewer.callback();
            });
        });
    }
}
