package su.nightexpress.excellentcrates.dialog.reward;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.api.crate.Reward;
import su.nightexpress.excellentcrates.dialog.Dialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import java.util.*;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RewardPermissionsDialog extends Dialog<Reward> {

    private static final String NEGATIVE = "-";
    private static final String POSITIVE = "+";

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Reward.Permissions.Title").text(title("Récompense", "Permissions"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Reward.Permissions.Body").dialogElement(400,
        "Indiquez les permissions qu'un joueur doit avoir ou ne pas avoir pour cette récompense.",
        "",
        "Préfixe " + GREEN.and(BOLD).wrap(POSITIVE) + " : permissions que le joueur doit " + GREEN.wrap("avoir") + ". Préfixe " + RED.and(BOLD).wrap(NEGATIVE) + " : permissions qu'il ne doit " + RED.wrap("pas avoir") + ".",
        "",
        "Le joueur doit avoir au moins une permission " + GREEN.wrap("« positive »") + " et aucune permission " + RED.wrap("« négative »") + "."
    );

    private static final TextLocale INPUT_PERMISSIONS = LangEntry.builder("Dialog.Reward.Permissions.Input.Permissions").text("Permissions");

    private static final String JSON_PERMISSIONS = "permissions";

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull Reward reward) {
        List<String> initial = new ArrayList<>();

        reward.getRequiredPermissions().forEach(perm -> initial.add(POSITIVE + perm));
        reward.getIgnoredPermissions().forEach(perm -> initial.add(NEGATIVE + perm));

        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(DialogInputs.text(JSON_PERMISSIONS, INPUT_PERMISSIONS)
                    .maxLength(300)
                    .width(300)
                    .initial(String.join("\n", initial))
                    .multiline(new WrappedMultilineOptions(10, 150))
                    .build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build());

            builder.handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                List<String> permissions = nbtHolder.getText(JSON_PERMISSIONS).map(raw -> raw.isBlank() ? new ArrayList<String>() : List.of(raw.split("\n"))).orElse(null);
                if (permissions == null) return;

                Set<String> negative = new HashSet<>();
                Set<String> positive = new HashSet<>();
                permissions.forEach(perm -> {
                    if (perm.startsWith(POSITIVE)) {
                        positive.add(perm.substring(1));
                    }
                    else if (perm.startsWith(NEGATIVE)) {
                        negative.add(perm.substring(1));
                    }
                    else positive.add(perm);
                });

                reward.setIgnoredPermissions(negative);
                reward.setRequiredPermissions(positive);
                reward.getCrate().markDirty();
                viewer.callback();
            });
        });
    }
}
