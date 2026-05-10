package su.nightexpress.excellentcrates.dialog.reward;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.Placeholders;
import su.nightexpress.excellentcrates.api.crate.Reward;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.crate.impl.Rarity;
import su.nightexpress.excellentcrates.dialog.Dialog;
import su.nightexpress.excellentcrates.util.RewardRollMath;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardWeightDialog extends Dialog<Reward> {

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Reward.Weight.Title").text(title("Récompense", "Chance du tirage"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Reward.Weight.Body").dialogElement(400,
        "La chance affichée est dans les " + SOFT_YELLOW.wrap("menus éditeur") + " (liste des récompenses, pastèque), pas dans le chat.",
        "",
        "Dans une même " + SOFT_YELLOW.wrap("catégorie") + ", les % sont des parts d’un total (souvent 100 % si une seule catégorie). Changer la « chance » recalcule le " + SOFT_YELLOW.wrap("poids") + " de cette ligne seule : les autres " + SOFT_YELLOW.wrap("poids fichier") + " restent, donc leurs pourcentages à l’écran bougent.",
        "",
        "Pour régler plusieurs lignes sans cet effet : remplissez " + SOFT_GREEN.wrap("Poids brut") + " (valeur YAML), ou éditez le fichier.",
        "",
        "Plus d’infos : " + OPEN_URL.with(Placeholders.WIKI_WEIGHTS).wrap(SOFT_GREEN.and(UNDERLINED).wrap("voir la doc")) + "."
    );

    private static final TextLocale INTPUT_RARITY = LangEntry.builder("Dialog.Reward.Weight.Input.Rarity").text(SOFT_YELLOW.wrap("Catégorie (animations)"));
    private static final TextLocale INTPUT_CHANCE = LangEntry.builder("Dialog.Reward.Weight.Input.Chance").text(SOFT_YELLOW.wrap("Chance (%)"));
    private static final TextLocale INTPUT_WEIGHT_RAW = LangEntry.builder("Dialog.Reward.Weight.Input.WeightRaw").text(SOFT_GREEN.wrap("Poids brut (optionnel)") + GRAY.wrap(" — laissez vide pour utiliser la chance"));

    private static final String JSON_CHANCE = "roll_chance_pct";
    private static final String JSON_RARITY = "rarity";
    private static final String JSON_WEIGHT_RAW = "weight_raw";

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
                    DialogInputs.text(JSON_CHANCE, INTPUT_CHANCE).initial(NumberUtil.format(reward.getRollChance())).maxLength(10).build(),
                    DialogInputs.text(JSON_WEIGHT_RAW, INTPUT_WEIGHT_RAW).initial("").maxLength(24).build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                Rarity rarity = nbtHolder.getText(JSON_RARITY).map(id -> plugin.getCrateManager().getRarity(id)).orElse(reward.getRarity());
                reward.setRarity(rarity);

                Optional<String> rawWeightText = nbtHolder.getText(JSON_WEIGHT_RAW).map(String::trim).filter(s -> !s.isEmpty());
                if (rawWeightText.isPresent()) {
                    double w = NumberUtil.getAnyDouble(rawWeightText.get(), -1D);
                    if (w >= 0D) {
                        reward.setWeight(w);
                        reward.getCrate().markDirty();
                        viewer.callback();
                        return;
                    }
                }

                double requestedChance = nbtHolder.getDouble(JSON_CHANCE, reward.getRollChance());

                double cap = rarity.getRollChance(reward.getCrate());
                double computed = RewardRollMath.weightFromTargetRollChance(reward.getCrate(), reward, rarity, requestedChance);

                if (Double.isNaN(computed)) {
                    if (cap > 0D && Math.abs(requestedChance - cap) > 0.08D && requestedChance < cap - 0.08D) {
                        Lang.DIALOG_REWARD_WEIGHT_SINGLE_CATEGORY.message().send(player);
                    }
                }
                else {
                    reward.setWeight(computed);
                }

                reward.getCrate().markDirty();
                viewer.callback();
            });
        });
    }
}
