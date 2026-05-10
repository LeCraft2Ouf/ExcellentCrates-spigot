package su.nightexpress.excellentcrates.hooks;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.util.ItemNbt;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Optional integration with Nexo: rebuild items via {@code NexoItems.itemFromId} so rewarded stacks match
 * Nexo's canonical ItemStack (avoids Vanilla/Paper CompoundTag normalization on custom-model items).
 */
public final class NexoHook {

    private static final Pattern NEXO_ID_IN_SNBT = Pattern.compile("\"nexo:id\"\\s*:\\s*\"([^\"]+)\"");

    private static final String NEXO_ITEMS_CLASS = "com.nexomc.nexo.api.NexoItems";

    private static volatile boolean reflectChecked;
    private static volatile boolean reflectOk;
    @Nullable
    private static Method optionalItemFromId;
    @Nullable
    private static Method itemFromId;
    @Nullable
    private static Method idFromItem;

    private NexoHook() {}

    private static synchronized void resolveReflect() {
        if (reflectChecked) return;
        reflectChecked = true;
        try {
            Class<?> nexoItems = Class.forName(NEXO_ITEMS_CLASS);
            try {
                optionalItemFromId = nexoItems.getMethod("optionalItemFromId", String.class);
            } catch (@SuppressWarnings("unused") NoSuchMethodException ignored) {
                // older API
            }
            itemFromId = nexoItems.getMethod("itemFromId", String.class);
            idFromItem = nexoItems.getMethod("idFromItem", ItemStack.class);
            reflectOk = true;
        } catch (@SuppressWarnings("unused") ClassNotFoundException | NoSuchMethodException e) {
            reflectOk = false;
        }
    }

    /** Nexo jars + API reachable on classpath. */
    public static boolean isReflectReady() {
        resolveReflect();
        return reflectOk;
    }

    /** Nexo's id string for stack, null if unavailable or item is not a Nexo item. */
    public static @Nullable String reflectIdFromItem(@Nullable ItemStack stack) {
        if (!isReflectReady() || stack == null || stack.getType().isAir() || idFromItem == null) {
            return null;
        }
        try {
            Object id = idFromItem.invoke(null, stack);
            return id == null ? null : String.valueOf(id).trim().isEmpty() ? null : String.valueOf(id).trim();
        } catch (@SuppressWarnings("unused") ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * Id read from Nexo API when possible, otherwise parsed from serialized item tag ({@link ItemNbt#getTagString}).
     */
    public static @NotNull Optional<String> resolveNexoRebuildId(@Nullable ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return Optional.empty();
        }
        String fromApi = reflectIdFromItem(stack);
        if (fromApi != null) {
            return Optional.of(fromApi);
        }
        String tagStr = ItemNbt.getTagString(stack);
        if (tagStr == null || tagStr.isBlank()) {
            return Optional.empty();
        }
        Matcher m = NEXO_ID_IN_SNBT.matcher(tagStr);
        return m.find() ? Optional.of(m.group(1)) : Optional.empty();
    }

    /**
     * Canonical Nexo singleton (quantity 1) or null when Nexo is not usable or item id unknown.
     */
    public static @Nullable ItemStack buildSingleton(@NotNull String nexoItemId) {
        resolveReflect();
        if (!reflectOk) {
            return null;
        }
        try {
            Object builder = null;
            if (optionalItemFromId != null) {
                Object optInvoke = optionalItemFromId.invoke(null, nexoItemId);
                if (optInvoke instanceof Optional<?> optFound && optFound.isPresent()) {
                    builder = optFound.get();
                }
            }
            if (builder == null && itemFromId != null) {
                builder = itemFromId.invoke(null, nexoItemId);
            }
            if (builder == null) {
                return null;
            }
            Method build = builder.getClass().getMethod("build");
            Object crafted = build.invoke(builder);
            if (!(crafted instanceof ItemStack stack)) {
                return null;
            }
            if (stack.getType().isAir()) {
                return null;
            }
            stack.setAmount(1);
            return stack.clone();
        } catch (@SuppressWarnings("unused") ReflectiveOperationException e) {
            return null;
        }
    }

}
