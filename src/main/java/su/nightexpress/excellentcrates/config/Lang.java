package su.nightexpress.excellentcrates.config;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.api.crate.RewardType;
import su.nightexpress.excellentcrates.crate.limit.CooldownMode;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.*;
import su.nightexpress.nightcore.locale.message.MessageData;
import su.nightexpress.nightcore.util.bridge.RegistryType;

import static su.nightexpress.excellentcrates.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class Lang implements LangContainer {

    public static final TextLocale COMMAND_ARGUMENT_NAME_CRATE = LangEntry.builder("Command.Argument.Name.Crate").text("crate");
    public static final TextLocale COMMAND_ARGUMENT_NAME_KEY   = LangEntry.builder("Command.Argument.Name.Key").text("key");
    public static final TextLocale COMMAND_ARGUMENT_NAME_X     = LangEntry.builder("Command.Argument.Name.X").text("x");
    public static final TextLocale COMMAND_ARGUMENT_NAME_Y     = LangEntry.builder("Command.Argument.Name.Y").text("y");
    public static final TextLocale COMMAND_ARGUMENT_NAME_Z     = LangEntry.builder("Command.Argument.Name.Z").text("z");

    public static final MessageLocale ERROR_COMMAND_INVALID_CRATE_ARGUMENT = LangEntry.builder("Error.Command.Argument.InvalidCrate").chatMessage(
        GRAY.wrap(SOFT_RED.wrap(GENERIC_VALUE) + " n'est pas une caisse valide !"));

    public static final MessageLocale ERROR_COMMAND_INVALID_KEY_ARGUMENT = LangEntry.builder("Error.Command.Argument.InvalidKey").chatMessage(
        GRAY.wrap(SOFT_RED.wrap(GENERIC_VALUE) + " n'est pas une clé valide !"));

    public static final TextLocale COMMAND_EDITOR_DESC         = LangEntry.builder("Command.Editor.Desc").text("Ouvre l'éditeur graphique.");
    public static final TextLocale COMMAND_DROP_DESC           = LangEntry.builder("Command.Drop.Desc").text("Place l'objet-caisse dans le monde.");
    public static final TextLocale COMMAND_DROP_KEY_DESC       = LangEntry.builder("Command.DropKey.Desc").text("Place l'objet-clé dans le monde.");
    public static final TextLocale COMMAND_OPEN_DESC           = LangEntry.builder("Command.Open.Desc").text("Ouvre une caisse.");
    public static final TextLocale COMMAND_OPEN_FOR_DESC       = LangEntry.builder("Command.OpenFor.Desc").text("Ouvre une caisse pour un joueur.");
    public static final TextLocale COMMAND_GIVE_DESC           = LangEntry.builder("Command.Give.Desc").text("Donne une caisse à un joueur.");
    public static final TextLocale COMMAND_KEY_DESC            = LangEntry.builder("Command.Key.Desc").text("Gère les clés d'un joueur.");
    public static final TextLocale COMMAND_KEY_GIVE_DESC       = LangEntry.builder("Command.Key.Give.Desc").text("Donne une clé à un joueur.");
    public static final TextLocale COMMAND_KEY_TAKE_DESC       = LangEntry.builder("Command.Key.Take.Desc").text("Retire une clé à un joueur.");
    public static final TextLocale COMMAND_KEY_SET_DESC        = LangEntry.builder("Command.Key.Set.Desc").text("Définit le nombre de clés d'un joueur.");
    public static final TextLocale COMMAND_KEY_INSPECT_DESC    = LangEntry.builder("Command.Key.Show.Desc").text("Affiche les clés virtuelles [du joueur].");
    public static final TextLocale COMMAND_PREVIEW_DESC        = LangEntry.builder("Command.Preview.Desc").text("Ouvre l'aperçu d'une caisse.");
    public static final TextLocale COMMAND_RESET_COOLDOWN_DESC = LangEntry.builder("Command.ResetCooldown.Desc").text("Réinitialise le temps de recharge d'ouverture.");
    public static final TextLocale COMMAND_MENU_DESC           = LangEntry.builder("Command.Menu.Desc").text("Ouvre le menu des caisses.");

    public static final MessageLocale COMMAND_DROP_DONE = LangEntry.builder("Command.Drop.Done").chatMessage(
        GRAY.wrap("Caisse " + SOFT_YELLOW.wrap(CRATE_NAME) + " placée à " + SOFT_YELLOW.wrap(LOCATION_X + ", " + LOCATION_Y + ", " + LOCATION_Z) + " dans " + SOFT_YELLOW.wrap(LOCATION_WORLD) + "."));

    public static final MessageLocale COMMAND_DROP_KEY_DONE = LangEntry.builder("Command.DropKey.Done").chatMessage(
        GRAY.wrap("Clé " + SOFT_YELLOW.wrap(KEY_NAME) + " posée à " + SOFT_YELLOW.wrap(LOCATION_X + ", " + LOCATION_Y + ", " + LOCATION_Z) + " dans " + SOFT_YELLOW.wrap(LOCATION_WORLD) + "."));



    public static final MessageLocale COMMAND_OPEN_FOR_DONE = LangEntry.builder("Command.OpenFor.Done").chatMessage(
        GRAY.wrap("Ouverture de " + SOFT_YELLOW.wrap(CRATE_NAME) + " pour " + SOFT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final MessageLocale COMMAND_OPEN_FOR_NOTIFY = LangEntry.builder("Command.OpenFor.Notify").chatMessage(
        GRAY.wrap("Vous avez été forcé d'ouvrir " + SOFT_YELLOW.wrap(CRATE_NAME) + "."));



    public static final MessageLocale COMMAND_GIVE_DONE = LangEntry.builder("Command.Give.Done").chatMessage(
        GRAY.wrap("Don de " + SOFT_YELLOW.wrap("x" + GENERIC_AMOUNT) + " " + SOFT_YELLOW.wrap(CRATE_NAME) + "(s) à " + SOFT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final MessageLocale COMMAND_GIVE_NOTIFY = LangEntry.builder("Command.Give.Notify").chatMessage(
        GRAY.wrap("Vous avez reçu " + SOFT_YELLOW.wrap("x" + GENERIC_AMOUNT) + " " + SOFT_YELLOW.wrap(CRATE_NAME) + "."));



    public static final MessageLocale COMMAND_KEY_GIVE_DONE = LangEntry.builder("Command.Key.Give.Done").chatMessage(
        GRAY.wrap("Don de " + SOFT_YELLOW.wrap("x" + GENERIC_AMOUNT) + " clé(s) " + SOFT_YELLOW.wrap(KEY_NAME) + " à " + SOFT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final MessageLocale COMMAND_KEY_GIVE_NOTIFY = LangEntry.builder("Command.Key.Give.Notify").chatMessage(
        GRAY.wrap("Vous avez reçu " + SOFT_YELLOW.wrap("x" + GENERIC_AMOUNT) + " clé(s) " + SOFT_YELLOW.wrap(KEY_NAME) + " !"));

    public static final TextLocale COMMAND_KEY_GIVE_ALL_DESC = LangEntry.builder("Command.Key.GiveAll.Desc").text(
        "Donne la clé à tous les joueurs en ligne.");

    public static final MessageLocale COMMAND_KEY_GIVE_ALL_DONE = LangEntry.builder("Command.Key.GiveAll.Done").chatMessage(
        GRAY.wrap("Don de " + SOFT_YELLOW.wrap("x" + GENERIC_AMOUNT) + " clé(s) " + SOFT_YELLOW.wrap(KEY_NAME) + " à " + SOFT_YELLOW.wrap("tous les joueurs en ligne") + "."));

    public static final MessageLocale COMMAND_KEY_TAKE_DONE = LangEntry.builder("Command.Key.Take.Done").chatMessage(
        GRAY.wrap("Retrait de " + SOFT_YELLOW.wrap("x" + GENERIC_AMOUNT) + " clé(s) " + SOFT_YELLOW.wrap(KEY_NAME) + " à " + SOFT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final MessageLocale COMMAND_KEY_TAKE_NOTIFY = LangEntry.builder("Command.Key.Take.Notify").chatMessage(
        GRAY.wrap("Vous avez perdu " + SOFT_RED.wrap("x" + GENERIC_AMOUNT) + " clé(s) " + SOFT_RED.wrap(KEY_NAME) + "."));

    public static final MessageLocale COMMAND_KEY_SET_DONE = LangEntry.builder("Command.Key.Set.Done").chatMessage(
        GRAY.wrap("Nombre de clés " + SOFT_YELLOW.wrap(KEY_NAME) + " défini à " + SOFT_YELLOW.wrap("x" + GENERIC_AMOUNT) + " pour " + SOFT_YELLOW.wrap(PLAYER_NAME) + "."));

    public static final MessageLocale COMMAND_KEY_SET_NOTIFY = LangEntry.builder("Command.Key.Set.Notify").chatMessage(
        GRAY.wrap("Votre nombre de clés " + SOFT_YELLOW.wrap(KEY_NAME) + " est maintenant " + SOFT_YELLOW.wrap("x" + GENERIC_AMOUNT) + "."));



    public static final MessageLocale COMMAND_KEY_INSPECT_LIST = LangEntry.builder("Command.Key.Show.Format.List").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        SOFT_YELLOW.wrap(BOLD.wrap("Clés virtuelles de " + PLAYER_NAME + " : ")),
        GENERIC_ENTRY,
        " "
    );

    public static final TextLocale COMMAND_KEY_INSPECT_ENTRY = LangEntry.builder("Command.Key.Show.Format.Entry").text(
        SOFT_YELLOW.wrap("▪ " + GRAY.wrap(KEY_NAME + ": ") + "x" + GENERIC_AMOUNT)
    );

    public static final MessageLocale COMMAND_PREVIEW_DONE_OTHERS = LangEntry.builder("Command.Preview.Done.Others").chatMessage(
        GRAY.wrap("Aperçu de " + SOFT_YELLOW.wrap(CRATE_NAME) + " ouvert pour " + SOFT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + "."));

    public static final MessageLocale COMMAND_RESET_COOLDOWN_DONE = LangEntry.builder("Command.ResetCooldown.Done").chatMessage(
        GRAY.wrap("Temps de recharge d'ouverture de " + SOFT_YELLOW.wrap(PLAYER_NAME) + " réinitialisé pour " + SOFT_YELLOW.wrap(CRATE_NAME) + "."));

    public static final MessageLocale COMMAND_MENU_DONE_OTHERS = LangEntry.builder("Command.Menu.Done.Others").chatMessage(
        GRAY.wrap("Menu des caisses ouvert pour " + SOFT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + "."));





    public static final MessageLocale CRATE_OPEN_ERROR_INVENTORY_SPACE = LangEntry.builder("Crate.Open.Error.InventorySpace").titleMessage(
        SOFT_RED.wrap(BOLD.wrap("Inventaire plein !")),
        GRAY.wrap("Libérez de la place pour ouvrir des caisses."),
        Sound.ENTITY_VILLAGER_NO
    );

    public static final MessageLocale CRATE_OPEN_ERROR_COOLDOWN_TEMPORARY = LangEntry.builder("Crate.Open.Error.Cooldown.Temporary").titleMessage(
        SOFT_RED.wrap(BOLD.wrap("Caisse en temps de recharge !")),
        GRAY.wrap("Vous pourrez la rouvrir dans " + SOFT_RED.wrap(GENERIC_TIME)),
        Sound.ENTITY_VILLAGER_NO
    );

    public static final MessageLocale CRATE_OPEN_ERROR_COOLDOWN_ONE_TIMED = LangEntry.builder("Crate.Open.Error.Cooldown.OneTimed").titleMessage(
        SOFT_RED.wrap(BOLD.wrap("Oups !")),
        GRAY.wrap("Vous avez déjà ouvert cette caisse à usage unique !"),
        Sound.ENTITY_VILLAGER_NO
    );

    public static final MessageLocale CRATE_OPEN_ERROR_NO_REWARDS = LangEntry.builder("Crate.Open.Error.NoRewards").titleMessage(
        RED.wrap(BOLD.wrap("Oups !")),
        GRAY.wrap("Aucune récompense pour vous pour le moment. Réessayez plus tard."),
        Sound.ENTITY_VILLAGER_NO
    );

    public static final MessageLocale CRATE_OPEN_ERROR_ALREADY = LangEntry.builder("Crate.Open.Error.Already").titleMessage(
        RED.wrap(BOLD.wrap("Oups !")),
        GRAY.wrap("Vous êtes déjà en train d'ouvrir une caisse !"),
        Sound.ENTITY_VILLAGER_NO
    );

    public static final MessageLocale CRATE_OPEN_TOO_EXPENSIVE = LangEntry.builder("Crate.Open.TooExpensive").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        RED.and(BOLD).wrap("CAISSE NON OUVERTE :"),
        RED.wrap("» ") + GRAY.wrap("Caisse : ") + WHITE.wrap(CRATE_NAME),
        RED.wrap("» ") + GRAY.wrap("Coût d'ouverture insuffisant : " + GENERIC_COSTS),
        " "
    );

    public static final MessageLocale CRATE_OPEN_RESULT_INFO = LangEntry.builder("Crate.Rewards").message(
        MessageData.CHAT_NO_PREFIX,
        " ",
        YELLOW.and(BOLD).wrap("CAISSE OUVERTE :"),
        YELLOW.wrap("» ") + GRAY.wrap("Caisse : ") + WHITE.wrap(CRATE_NAME),
        YELLOW.wrap("» ") + GRAY.wrap("Récompenses : ") + WHITE.wrap(GENERIC_REWARDS),
        " "
    );

    public static final TextLocale CRATE_OPEN_RESULT_REWARD = LangEntry.builder("Crate.Opened.Result.Reward").text(REWARD_NAME);

    public static final MessageLocale CRATE_OPEN_MILESTONE_COMPLETED = LangEntry.builder("Crate.Open.Milestone.Completed").message(
        MessageData.chat().usePrefix(false).sound(Sound.ENTITY_PLAYER_LEVELUP).build(),
        GRAY.wrap("Vous avez atteint le palier " + GREEN.wrap(MILESTONE_OPENINGS + " ouvertures") + " et reçu " + GREEN.wrap(REWARD_NAME) + " en récompense !")
    );

    public static final MessageLocale CRATE_OPEN_REWARD_BROADCAST = LangEntry.builder("Crate.Open.Reward.Broadcast").message(
        MessageData.chat().usePrefix(false).sound(Sound.BLOCK_NOTE_BLOCK_BELL).build(),
        " ",
        GRAY.wrap(LIGHT_PURPLE.wrap(PLAYER_DISPLAY_NAME) + " a ouvert " + LIGHT_PURPLE.wrap(CRATE_NAME) + " et a reçu " + LIGHT_PURPLE.wrap(REWARD_NAME) + " !"),
        " ",
        GRAY.wrap("Acheter des clés : " + OPEN_URL.with("https://YOUR_LINK_HERE.xyz").wrap(LIGHT_PURPLE.wrap("[Ouvrir la boutique]"))),
        " "
    );

    public static final MessageLocale CRATE_PREVIEW_ERROR_COOLDOWN = LangEntry.builder("Crate.Preview.Error.Cooldown").chatMessage(
        GRAY.wrap("Vous pourrez à nouveau prévisualiser cette caisse dans " + SOFT_RED.wrap(GENERIC_TIME))
    );

    public static final MessageLocale ERROR_DATA_IS_LOADING = LangEntry.builder("Error.DataIsLoading").chatMessage(
        SOFT_RED.wrap("Les données sont encore en cours de chargement... Réessayez plus tard.")
    );

    public static final TextLocale OTHER_COOLDOWN_READY       = LangEntry.builder("Other.Cooldown.Ready").text(GREEN.wrap("Prêt à ouvrir !"));
    public static final TextLocale OTHER_LAST_OPENER_EMPTY    = LangEntry.builder("Other.LastOpener.Empty").text("-");
    public static final TextLocale OTHER_LAST_REWARD_EMPTY    = LangEntry.builder("Other.LastReward.Empty").text("-");
    public static final TextLocale OTHER_NEXT_MILESTONE_EMPTY = LangEntry.builder("Other.NextMilestone.Empty").text("-");

    public static final TextLocale OTHER_MIDNIGHT = LangEntry.builder("Other.Midnight").text("Minuit");
    public static final TextLocale OTHER_FREE     = LangEntry.builder("Other.Free").text("Gratuit");

    public static final TextLocale EFFECT_MODEL_NONE    = LangEntry.builder("EffectModel.None").text("Aucun");
    public static final TextLocale EFFECT_MODEL_HELIX   = LangEntry.builder("EffectModel.Helix").text("Hélice");
    public static final TextLocale EFFECT_MODEL_SPIRAL  = LangEntry.builder("EffectModel.Spiral").text("Spirale");
    public static final TextLocale EFFECT_MODEL_SPHERE  = LangEntry.builder("EffectModel.Sphere").text("Sphère");
    public static final TextLocale EFFECT_MODEL_HEART   = LangEntry.builder("EffectModel.Heart").text("Cœur");
    public static final TextLocale EFFECT_MODEL_PULSAR  = LangEntry.builder("EffectModel.Pulsar").text("Pulsar");
    public static final TextLocale EFFECT_MODEL_BEACON  = LangEntry.builder("EffectModel.Beacon").text("Signal");
    public static final TextLocale EFFECT_MODEL_TORNADO = LangEntry.builder("EffectModel.Tornado").text("Tornade");
    public static final TextLocale EFFECT_MODEL_VORTEX  = LangEntry.builder("EffectModel.Vortex").text("Vortex");
    public static final TextLocale EFFECT_MODEL_SIMPLE  = LangEntry.builder("EffectModel.Simple").text("Simple");

    public static final BooleanLocale INSPECTIONS_GENERIC_OVERVIEW = LangEntry.builder("Inspections.Generic.Overview").bool("Aucun problème détecté.", "Des problèmes ont été détectés !");
    public static final BooleanLocale INSPECTIONS_GENERIC_ITEM     = LangEntry.builder("Inspections.Generic.Item").bool("Item valide.", "Item invalide !");
    public static final BooleanLocale INSPECTIONS_GENERIC_COMMANDS = LangEntry.builder("Inspections.Generic.Commands").bool("Toutes les commandes sont valides.", "Commandes invalides détectées !");

    public static final BooleanLocale INSPECTIONS_CRATE_PREVIEW      = LangEntry.builder("Inspections.Crate.Preview").bool("Aperçu valide.", "Aperçu invalide !");
    public static final BooleanLocale INSPECTIONS_CRATE_OPENING      = LangEntry.builder("Inspections.Crate.Opening").bool("Animation d'ouverture valide.", "Animation d'ouverture invalide !");
    public static final BooleanLocale INSPECTIONS_CRATE_HOLOGRAM     = LangEntry.builder("Inspections.Crate.Hologram").bool("Modèle d'hologramme valide.", "Modèle d'hologramme invalide !");
    public static final BooleanLocale INSPECTIONS_REWARD_PREVIEW     = LangEntry.builder("Inspections.Reward.Preview").bool("Item d'aperçu valide.", "Item d'aperçu invalide !");
    public static final BooleanLocale INSPECTIONS_REWARD_ITEMS       = LangEntry.builder("Inspections.Reward.Items").bool("Tous les items sont valides.", "Items invalides détectés !");
    public static final TextLocale    INSPECTIONS_REWARD_NO_ITEMS    = LangEntry.builder("Inspections.Reward.NoItems").text("Aucun item ajouté.");
    public static final TextLocale    INSPECTIONS_REWARD_NO_COMMANDS = LangEntry.builder("Inspections.Reward.NoCommands").text("Aucune commande définie.");

    public static final IconLocale UI_COSTS_OPTION_AVAILABLE = LangEntry.iconBuilder("UI.Costs.Option.Available")
        .rawName(WHITE.wrap(COST_NAME))
        .rawLore(
            GENERIC_COSTS,
            EMPTY_IF_ABOVE,
            YELLOW.and(BOLD).wrap("OUVERTURES DISPONIBLES : ") + WHITE.and(UNDERLINED).wrap(GENERIC_AVAILABLE),
            "",
            GREEN.wrap("→ " + UNDERLINED.wrap("Cliquer pour choisir"))
        )
        .build();

    public static final IconLocale UI_COSTS_OPTION_UNAVAILABLE = LangEntry.iconBuilder("UI.Costs.Option.Unavailable")
        .rawName(WHITE.wrap(COST_NAME))
        .rawLore(
            GENERIC_COSTS,
            EMPTY_IF_ABOVE,
            RED.and(BOLD).wrap("VOUS NE POUVEZ PAS PAYER CE COÛT")
        )
        .build();

    public static final TextLocale UI_COSTS_ENTRY_AVAILABLE   = LangEntry.builder("UI.Costs.Entry0.Available").text(WHITE.wrap(GENERIC_ENTRY) + " " + GRAY.wrap("(" + GREEN.wrap("✔") + ")"));
    public static final TextLocale UI_COSTS_ENTRY_UNAVAILABLE = LangEntry.builder("UI.Costs.Entry0.Unavailable").text(WHITE.wrap(GENERIC_ENTRY) + " " + GRAY.wrap("(" + RED.wrap("✘") + ")"));

    public static final IconLocale UI_OPEN_AMOUNT_SINGLE = LangEntry.iconBuilder("UI.OpenAmount.Single")
        .rawName(YELLOW.and(BOLD).wrap("Une ouverture"))
        .rawLore(
            GRAY.wrap("Ouvre une seule caisse."),
            "",
            YELLOW.wrap("→ " + UNDERLINED.wrap("Cliquer pour choisir"))
        )
        .build();

    public static final IconLocale UI_OPEN_AMOUNT_ALL = LangEntry.iconBuilder("UI.OpenAmount.All")
        .rawName(YELLOW.and(BOLD).wrap("Tout ouvrir"))
        .rawLore(
            GRAY.wrap("Ouvre jusqu'à " + WHITE.wrap(GENERIC_MAX) + " caisse(s)."),
            "",
            YELLOW.wrap("→ " + UNDERLINED.wrap("Cliquer pour choisir"))
        )
        .build();

    public static final TextLocale EDITOR_TITLE_MAIN             = LangEntry.builder("Editor.Title.Main").text(BLACK.wrap("Éditeur ExcellentCrates"));
    public static final TextLocale EDITOR_TITLE_CRATE_LIST       = LangEntry.builder("Editor.Title.Crates").text(BLACK.wrap("Éditeur des caisses"));
    public static final TextLocale EDITOR_TITLE_CRATE_SETTINGS   = LangEntry.builder("Editor.Title.Crate.Settings").text(BLACK.wrap("Paramètres de la caisse"));
    public static final TextLocale EDITOR_TITLE_CRATE_COSTS      = LangEntry.builder("Editor.Title.Crate.CostOptions").text(BLACK.wrap("Options de coût"));
    public static final TextLocale EDITOR_TITLE_CRATE_COST       = LangEntry.builder("Editor.Title.Crate.CostOption").text(BLACK.wrap("Paramètres d'une option de coût"));
    public static final TextLocale EDITOR_TITLE_CRATE_MILESTONES = LangEntry.builder("Editor.Title.Crate.Milestones").text(BLACK.wrap("Paliers de la caisse"));
    public static final TextLocale EDITOR_TITLE_REWARD_LIST      = LangEntry.builder("Editor.Title.Reward.List").text(BLACK.wrap("Récompenses de la caisse"));
    public static final TextLocale EDITOR_TITLE_REWARD_CONTENT   = LangEntry.builder("Editor.Title.Reward.Content").text(BLACK.wrap("Objets de récompense"));
    public static final TextLocale EDITOR_TITLE_REWARD_SETTINGS  = LangEntry.builder("Editor.Title.Reward.Settings").text(BLACK.wrap("Paramètres de récompense"));
    public static final TextLocale EDITOR_TITLE_KEY_LIST         = LangEntry.builder("Editor.Title.Keys").text(BLACK.wrap("Éditeur des clés"));
    public static final TextLocale EDITOR_TITLE_KEY_SETTINGS     = LangEntry.builder("Editor.Title.Key.Settings").text(BLACK.wrap("Paramètres de la clé"));

    public static final IconLocale EDITOR_BUTTON_RETURN = LangEntry.iconBuilder("Editor.Button.Return")
        .name(SOFT_YELLOW.and(BOLD).wrap("Retour"))
        .build();

    @Deprecated
    public static final TextLocale EDITOR_ENTER_AMOUNT            = LangEntry.builder("Editor.Enter.Amount").text(GRAY.wrap("Saisir " + GREEN.wrap("[Montant]")));
    @Deprecated
    public static final TextLocale EDITOR_ENTER_REWARD_ID         = LangEntry.builder("Editor.Reward.Enter.Id").text(GRAY.wrap("Saisir " + GREEN.wrap("[Identifiant de récompense]")));


    public static final DialogElementLocale DIALOG_GENERIC_CREATION_BODY = LangEntry.builder("Dialog.Generic.Creation.Body").dialogElement(400,
        "Saisissez un " + SOFT_YELLOW.wrap("identifiant unique") + " (ID) pour le nouvel objet.",
        "",
        SOFT_ORANGE.wrap("⚠") + " Cet ID sert dans les " + SOFT_ORANGE.wrap("commandes") + " et les " + SOFT_ORANGE.wrap("fichiers de config") + " : choisissez quelque chose de " + SOFT_ORANGE.wrap("clair") + " et " + SOFT_ORANGE.wrap("facile à retenir") + ".",
        "",
        SOFT_RED.wrap("→") + " Seuls les " + SOFT_RED.wrap("lettres") + ", " + SOFT_RED.wrap("chiffres") + " et le " + SOFT_RED.wrap("tiret bas") + " (_) sont autorisés."
    );


    public static final DialogElementLocale DIALOG_GENERIC_NAME_BODY = LangEntry.builder("Dialog.Generic.Name.Body").dialogElement(400,
        "Saisissez le " + SOFT_YELLOW.wrap("nom d'affichage") + "."
    );

    public static final TextLocale DIALOG_GENERIC_NAME_INPUT_NAME         = LangEntry.builder("Dialog.Generic.Name.Input.Name").text("Nom");
    public static final TextLocale DIALOG_GENERIC_NAME_INPUT_REPLACE_NAME = LangEntry.builder("Dialog.Generic.Name.Input.ReplaceName").text("Remplacer le nom de l'item");


    public static final DialogElementLocale DIALOG_GENERIC_DESCRIPTION_BODY = LangEntry.builder("Dialog.Generic.Description.Body").dialogElement(400,
        "Saisissez la " + SOFT_YELLOW.wrap("description") + "."
    );

    public static final TextLocale DIALOG_GENERIC_DESCRIPTION_INPUT_DESC         = LangEntry.builder("Dialog.Generic.Description.Input.Description").text("Description");
    public static final TextLocale DIALOG_GENERIC_DESCRIPTION_INPUT_REPLACE_LORE = LangEntry.builder("Dialog.Generic.Description.Input.ReplaceItemLore").text("Remplacer le lore de l'item");


    public static final DialogElementLocale DIALOG_GENERIC_ITEM_BODY_NORMAL = LangEntry.builder("Dialog.Generic.Item.Body.Normal").dialogElement(400,
        "Confirmer le remplacement de l'item.",
        GRAY.wrap("Cochez les options supplémentaires si besoin.")
    );

    public static final DialogElementLocale DIALOG_GENERIC_ITEM_BODY_CUSTOM = LangEntry.builder("Dialog.Generic.Item.Body.Custom").dialogElement(400,
        "Confirmer le remplacement de l'item.",
        GRAY.wrap("Cochez les options supplémentaires si besoin."),
        "",
        SOFT_RED.and(BOLD).wrap("NOTE IMPORTANTE :"),
        "Si l'item ci-dessus ne correspond pas à celui que vous avez utilisé, activez l'option " + SOFT_RED.wrap("Sauver en NBT") + ".",
        GRAY.wrap("Cela garantit que les données exactes sont enregistrées.")
    );

    public static final TextLocale DIALOG_GENERIC_ITEM_INPUT_NBT      = LangEntry.builder("Dialog.Generic.Item.Input.NBT").text(SOFT_RED.wrap("Sauver en NBT"));
    public static final TextLocale DIALOG_GENERIC_ITEM_INPUT_REP_NAME = LangEntry.builder("Dialog.Generic.Item.Input.ReplaceName").text("Reprendre le nom de l'item");
    public static final TextLocale DIALOG_GENERIC_ITEM_INPUT_REP_DESC = LangEntry.builder("Dialog.Generic.Item.Input.ReplaceDesc").text("Reprendre le lore de l'item");

    public static final MessageLocale DIALOG_REWARD_ITEM_NO_NEXO_ID = LangEntry.builder("Dialog.Reward.Item.Error.NoNexoId").chatMessage(
        GRAY.wrap("Cette pile n'a pas d'") + SOFT_RED.wrap("identifiant Nexo") + GRAY.wrap(". Utilisez ") + SOFT_YELLOW.wrap("Sauver en NBT") + GRAY.wrap(" ou ") + SOFT_YELLOW.wrap("Sauver en référence") + GRAY.wrap("."));

    /** When no other rewards share this rarity tier, weight cannot tweak the percentage shown. */
    public static final MessageLocale DIALOG_REWARD_WEIGHT_SINGLE_CATEGORY = LangEntry.builder("Dialog.Reward.Weight.Notice.SingleCategory").chatMessage(
        GRAY.wrap("Vous êtes seul dans cette ") + SOFT_YELLOW.wrap("catégorie") + GRAY.wrap(" : la chance affichée vaut alors la ")
            + SOFT_YELLOW.wrap("part de tirage de la catégorie") + GRAY.wrap(" ; changer le champ ne peut pas créer plusieurs pourcentages. ")
            + GRAY.wrap("Ajoutez d'autres récompenses avec la même catégorie pour partager la chance."));


    public static final EnumLocale<RewardType>   REWARD_TYPE   = LangEntry.builder("Enums.RewardType").enumeration(RewardType.class);
    public static final EnumLocale<CooldownMode> COOLDOWN_MODE = LangEntry.builder("Enums.CooldownMode").enumeration(CooldownMode.class);
    public static final RegistryLocale<Particle> PARTICLE      = LangEntry.builder("Assets.Particle").registry(RegistryType.PARTICLE_TYPE);

    @NotNull
    public static String inspection(@NotNull BooleanLocale locale, boolean state) {
        return CoreLang.formatEntry(locale.get(state), state);
    }
}
