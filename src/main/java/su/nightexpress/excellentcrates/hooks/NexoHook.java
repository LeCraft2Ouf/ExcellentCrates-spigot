package su.nightexpress.excellentcrates.hooks;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.util.ItemNbt;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Optional integration with Nexo: rebuild items via {@code NexoItems.itemFromId} so rewarded stacks match
 * Nexo's canonical ItemStack (avoids Vanilla/Paper CompoundTag normalization on custom-model items).
 * <p>
     * Templates are cloned from RAM and optionally {@linkplain #setTemplatePersistenceDirectory persisted} so
     * stacking stays consistent across server restarts. Persisted templates are checked against Nexo's current
     * visual item data before reuse, so stale templates do not turn working Nexo items back into plain paper.
 * After changing Nexo items, delete the cache folder or call {@link #deletePersistedSingletonTemplates()}.
 * On startup, {@link NexoTemplateWarmup} calls {@link #warmSingletonTemplates} for ids used by crates/keys (with retries)
 * so templates exist even if Nexo registers items a few ticks later.
 */
public final class NexoHook {

    private static final Pattern NEXO_ID_IN_SNBT = Pattern.compile("\"nexo:id\"\\s*:\\s*\"([^\"]+)\"");

    private static final String NEXO_ITEMS_CLASS = "com.nexomc.nexo.api.NexoItems";

    private static final String TEMPLATE_FILE_SUFFIX = ".txt";

    private static volatile boolean reflectChecked;
    private static volatile boolean reflectOk;
    /**
     * One template stack (amount 1) per Nexo id so repeated {@link #buildSingleton} calls clone the same
     * component patch — avoids non-canonical MiniMessage/Adventure trees differing between crate openings.
     */
    private static final ConcurrentHashMap<String, ItemStack> SINGLETON_TEMPLATE_CACHE = new ConcurrentHashMap<>();
    /** When non-null, successful Nexo builds are saved here and loaded before calling Nexo again. */
    private static volatile @Nullable File templatePersistenceDirectory;
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

    /**
     * Root folder for {@link ItemNbt#compress}/{@link ItemNbt#decompress} blobs (one file per Nexo id).
     * Pass {@code null} to disable disk persistence (restores only in-memory caching).
     */
    public static void setTemplatePersistenceDirectory(@Nullable File directory) {
        templatePersistenceDirectory = directory;
    }

    /** Clears in-memory Nexo templates; persisted files are kept so the next boot reloads the same stacks. */
    public static void clearSingletonTemplateCache() {
        SINGLETON_TEMPLATE_CACHE.clear();
    }

    /**
     * Calls {@link #buildSingleton(String)} for each distinct id so RAM and optional disk cache are filled.
     * Safe to run multiple times (e.g. delayed tasks until Nexo finishes registering items).
     *
     * @return how many ids produced a non-air stack this pass
     */
    public static int warmSingletonTemplates(@NotNull Collection<String> nexoItemIds) {
        if (!isReflectReady()) {
            return 0;
        }
        LinkedHashSet<String> distinct = new LinkedHashSet<>();
        for (String raw : nexoItemIds) {
            if (raw == null) {
                continue;
            }
            String id = raw.trim();
            if (!id.isEmpty()) {
                distinct.add(id);
            }
        }
        int ok = 0;
        for (String id : distinct) {
            ItemStack built = buildSingleton(id);
            if (built != null && !built.getType().isAir()) {
                ok++;
            }
        }
        return ok;
    }

    /** Deletes persisted template files (and clears RAM). Use after Nexo item definition changes. */
    public static void deletePersistedSingletonTemplates() {
        SINGLETON_TEMPLATE_CACHE.clear();
        File dir = templatePersistenceDirectory;
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(TEMPLATE_FILE_SUFFIX)) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }
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
     * Nexo singleton (quantity 1): clones a cached template so identical ids match on merge/stack.
     * The first successful build becomes the RAM template for this server session. On boot, a persisted template is
     * reused only when its Nexo id and model-bearing components still match the current Nexo build; otherwise it is
     * refreshed. Once RAM is warm, Nexo is not asked again for the same id, because repeated Nexo builds can serialize
     * equivalent lore/name components differently and break stacking.
     */
    public static synchronized @Nullable ItemStack buildSingleton(@NotNull String nexoItemId) {
        String id = nexoItemId.trim();
        if (id.isEmpty()) {
            return null;
        }
        ItemStack cached = SINGLETON_TEMPLATE_CACHE.get(id);
        if (cached != null && !cached.getType().isAir()) {
            return cached.clone();
        }

        ItemStack fromDisk = loadPersistedTemplate(id);
        if (isReflectReady()) {
            ItemStack fresh = buildSingletonFresh(id);
            if (fresh != null && !fresh.getType().isAir()) {
                fresh.setAmount(1);

                if (isTemplateCompatible(fromDisk, fresh, id)) {
                    fromDisk.setAmount(1);
                    ItemStack template = fromDisk.clone();
                    SINGLETON_TEMPLATE_CACHE.put(id, template);
                    return template.clone();
                }

                ItemStack template = fresh.clone();
                persistTemplate(id, template);
                SINGLETON_TEMPLATE_CACHE.put(id, template);
                return template.clone();
            }
        }
        if (fromDisk != null && !fromDisk.getType().isAir()) {
            fromDisk.setAmount(1);
            ItemStack template = fromDisk.clone();
            SINGLETON_TEMPLATE_CACHE.put(id, template);
            return template.clone();
        }
        return null;
    }

    private static boolean isTemplateCompatible(@Nullable ItemStack template, @NotNull ItemStack fresh, @NotNull String expectedNexoId) {
        if (template == null || template.getType().isAir()) {
            return false;
        }
        ItemStack normalizedTemplate = template.clone();
        ItemStack normalizedFresh = fresh.clone();
        normalizedTemplate.setAmount(1);
        normalizedFresh.setAmount(1);
        if (normalizedTemplate.getType() != normalizedFresh.getType()) {
            return false;
        }
        Optional<String> templateId = resolveNexoRebuildId(normalizedTemplate);
        Optional<String> freshId = resolveNexoRebuildId(normalizedFresh);
        if (templateId.isEmpty() || freshId.isEmpty()) {
            return false;
        }
        if (!templateId.get().equalsIgnoreCase(expectedNexoId) || !freshId.get().equalsIgnoreCase(expectedNexoId)) {
            return false;
        }
        return visualSignature(normalizedTemplate).equals(visualSignature(normalizedFresh));
    }

    private static @NotNull String visualSignature(@NotNull ItemStack stack) {
        String tag = ItemNbt.getTagString(stack);
        if (tag == null || tag.isBlank()) {
            return "";
        }
        return componentSignature(tag, "minecraft:custom_model_data")
            + "|"
            + componentSignature(tag, "minecraft:item_model");
    }

    private static @NotNull String componentSignature(@NotNull String tag, @NotNull String componentName) {
        String needle = "\"" + componentName + "\":";
        int start = tag.indexOf(needle);
        if (start < 0) {
            return "";
        }
        int valueStart = start + needle.length();
        int valueEnd = findComponentValueEnd(tag, valueStart);
        if (valueEnd <= valueStart) {
            return "";
        }
        return tag.substring(start, valueEnd);
    }

    private static int findComponentValueEnd(@NotNull String tag, int index) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = index; i < tag.length(); i++) {
            char c = tag.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (c == '{' || c == '[') {
                depth++;
                continue;
            }
            if (c == '}' || c == ']') {
                if (depth == 0) {
                    return i;
                }
                depth--;
                continue;
            }
            if (c == ',' && depth == 0) {
                return i;
            }
        }
        return tag.length();
    }

    private static @NotNull String safeTemplateFileName(@NotNull String nexoItemId) {
        String s = nexoItemId.replaceAll("[^a-zA-Z0-9._-]", "_");
        return s.isEmpty() ? "_" : s;
    }

    private static @Nullable ItemStack loadPersistedTemplate(@NotNull String nexoItemId) {
        File dir = templatePersistenceDirectory;
        if (dir == null) {
            return null;
        }
        File file = new File(dir, safeTemplateFileName(nexoItemId) + TEMPLATE_FILE_SUFFIX);
        if (!file.isFile()) {
            return null;
        }
        try {
            String compressed = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            ItemStack stack = ItemNbt.decompress(compressed);
            return stack == null || stack.getType().isAir() ? null : stack.clone();
        } catch (@SuppressWarnings("unused") IOException ignored) {
            return null;
        }
    }

    private static void persistTemplate(@NotNull String nexoItemId, @NotNull ItemStack template) {
        File dir = templatePersistenceDirectory;
        if (dir == null) {
            return;
        }
        try {
            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }
            File file = new File(dir, safeTemplateFileName(nexoItemId) + TEMPLATE_FILE_SUFFIX);
            String compressed = ItemNbt.compress(template);
            if (compressed != null && !compressed.isBlank()) {
                Files.writeString(file.toPath(), compressed, StandardCharsets.UTF_8);
            }
        } catch (@SuppressWarnings("unused") IOException ignored) {
            // Best-effort; in-memory cache still applies for this session.
        }
    }

    private static @Nullable ItemStack buildSingletonFresh(@NotNull String nexoItemId) {
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
            return stack.clone();
        } catch (@SuppressWarnings("unused") ReflectiveOperationException e) {
            return null;
        }
    }

}
