package su.nightexpress.excellentcrates.dialog.reward;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.Placeholders;
import su.nightexpress.excellentcrates.api.crate.Reward;
import su.nightexpress.excellentcrates.crate.impl.Rarity;
import su.nightexpress.excellentcrates.dialog.Dialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardWeightDialog extends Dialog<Reward> {

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Reward.Weight.Title").text(title("Récompense", "Poids du tirage"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Reward.Weight.Body").dialogElement(400,
        "Réglez surtout le " + SOFT_YELLOW.wrap("poids") + " : c’est lui qui fixe la " + SOFT_YELLOW.wrap("chance") + " affichée dans les menus.",
        "",
        "Le champ " + SOFT_YELLOW.wrap("catégorie") + " sert encore à certaines animations de caisse ; une seule catégorie partout suffit pour s’en passer.",
        "",
        "Plus d’infos : " + OPEN_URL.with(Placeholders.WIKI_WEIGHTS).wrap(SOFT_GREEN.and(UNDERLINED).wrap("voir la doc")) + "."
    );

    private static final TextLocale INTPUT_RARITY = LangEntry.builder("Dialog.Reward.Weight.Input.Rarity").text(SOFT_YELLOW.wrap("Catégorie (animations)"));
    private static final TextLocale INTPUT_WEIGHT = LangEntry.builder("Dialog.Reward.Weight.Input.Weight").text(SOFT_YELLOW.wrap("Poids"));

    private static final String JSON_WEIGHT = "weight";
    private static final String JSON_RARITY = "rarity";

    private final CratesPlugin plugin;

    public RewardWeightDialog(@NotNull CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull Reward reward) {
        List<WrappedSingleOptionEntry> rarities = new ArrayList<>();

        plugin.getCrateManager().getRarities().forEach(rarity -> {
            rarities.add(new WrappedSingleOptionEntry(rarity.getId(), rarity.getName(), reward.getRarity() == rarity));
        });

        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.singleOption(JSON_RARITY, INTPUT_RARITY, rarities).build(),
                    DialogInputs.text(JSON_WEIGHT, INTPUT_WEIGHT).initial(String.valueOf(reward.getWeight())).maxLength(6).build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                Rarity rarity = nbtHolder.getText(JSON_RARITY).map(id -> plugin.getCrateManager().getRarity(id)).orElse(reward.getRarity());
                double weight = nbtHolder.getDouble(JSON_WEIGHT, reward.getWeight());

                reward.setRarity(rarity);
                reward.setWeight(weight);
                viewer.callback();
            });
        });
    }
}
