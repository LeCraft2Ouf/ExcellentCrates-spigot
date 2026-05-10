package su.nightexpress.excellentcrates.editor;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.nightcore.ui.menu.item.ItemClick;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.util.bukkit.NightItem;

/**
 * Bouton retour des menus d'éditeur : libellé fourni par ExcellentCrates ({@link Lang#EDITOR_BUTTON_RETURN}),
 * et non par {@code MenuItem.buildReturn} de NightCore (souvent résolu en anglais si la locale NightCore n'est pas appliquée).
 */
public final class EditorReturnButton {

    private EditorReturnButton() {}

    @NotNull
    public static MenuItem.Builder menuItem(int slot, @NotNull ItemClick handler) {
        return NightItem.fromType(Material.IRON_DOOR)
            .hideAllComponents()
            .localized(Lang.EDITOR_BUTTON_RETURN)
            .toMenuItem()
            .setSlots(slot)
            .setHandler(handler);
    }
}
