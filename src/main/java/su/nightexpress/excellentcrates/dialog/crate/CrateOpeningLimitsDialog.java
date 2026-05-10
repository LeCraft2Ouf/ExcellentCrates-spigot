package su.nightexpress.excellentcrates.dialog.crate;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.dialog.Dialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class CrateOpeningLimitsDialog extends Dialog<Crate> {

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Crate.OpeningCooldown.Title").text(title("Caisse", "Limites d'ouverture"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Crate.OpeningCooldown.Body").dialogElement(400,
        "Limite d'ouvertures de la caisse par joueur.",
        "",
        "Le " + SOFT_YELLOW.wrap("cooldown") + " est la fenêtre en secondes pendant laquelle le joueur peut ouvrir la caisse jusqu'au nombre indiqué dans " + SOFT_YELLOW.wrap("Quantité") + ".",
        "",
        "À expiration, le compteur est réinitialisé et la caisse redevient disponible selon les mêmes règles.",
        "",
        "Le décompte commence à la première ouverture après la réinitialisation.",
        "",
        SOFT_YELLOW.wrap("→") + " Pour une ouverture unique définitive, mettez le " + SOFT_YELLOW.wrap("cooldown") + " à " + SOFT_YELLOW.wrap("-1") + ".",
        "",
        SOFT_YELLOW.wrap("→") + " Décochez " + SOFT_YELLOW.wrap("Activé") + " pour désactiver cette limite."
    );

    private static final TextLocale INPUT_ENABLED  = LangEntry.builder("Dialog.Crate.Preview.Input.Enabled").text("Activé");
    private static final TextLocale INPUT_COOLDOWN = LangEntry.builder("Dialog.Crate.OpeningCooldown.Input.Cooldown").text("Cooldown " + GRAY.wrap("(secondes)"));
    private static final TextLocale INPUT_AMOUNT   = LangEntry.builder("Dialog.Crate.OpeningCooldown.Input.Amount").text("Quantité " + GRAY.wrap("(min. 1)"));

    private static final String JSON_ENABLED  = "enabled";
    private static final String JSON_COOLDOWN = "cooldown";
    private static final String JSON_AMOUNT   = "amount";

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull Crate crate) {
        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.bool(JSON_ENABLED, INPUT_ENABLED).initial(crate.isOpeningCooldownEnabled()).build(),
                    DialogInputs.text(JSON_COOLDOWN, INPUT_COOLDOWN).initial(String.valueOf(crate.getOpeningCooldownTime())).build(),
                    DialogInputs.text(JSON_AMOUNT, INPUT_AMOUNT).initial(String.valueOf(crate.getOpeningLimitAmount())).build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                boolean enabled = nbtHolder.getBoolean(JSON_ENABLED, crate.isOpeningCooldownEnabled());
                int cooldown = nbtHolder.getInt(JSON_COOLDOWN, crate.getOpeningCooldownTime());
                int amount = nbtHolder.getInt(JSON_AMOUNT, crate.getOpeningLimitAmount());

                crate.setOpeningCooldownEnabled(enabled);
                crate.setOpeningCooldownTime(cooldown);
                crate.setOpeningLimitAmount(amount);
                crate.markDirty();
                viewer.callback();
            });
        });
    }
}
