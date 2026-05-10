package su.nightexpress.excellentcrates.util;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.api.crate.Reward;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.crate.impl.Rarity;

/**
 * Roll chance displayed in GUIs equals (weight<sub>i</sub> / Σ(weight in same rarity)) × this rarity share of crate.
 */
public final class RewardRollMath {

    private static final double EPS = 1e-6D;

    private RewardRollMath() {}

    /**
     * Computes the reward weight needed for {@code targetChancePercent}.
     *
     * @return the new weight, or {@link Double#NaN} when this reward is the only weighted line in {@code rarity}
     * (then chance is forced to {@code rarity.getRollChance(crate)} regardless of positive weight).
     */
    public static double weightFromTargetRollChance(
        @NotNull Crate crate,
        @NotNull Reward edited,
        @NotNull Rarity rarity,
        double targetChancePercent
    ) {
        double rarityShare = rarity.getRollChance(crate);
        if (rarityShare <= EPS) {
            return edited.getWeight();
        }

        double c = Math.max(0D, Math.min(targetChancePercent, rarityShare));

        double sOthers = crate.getRewards(rarity).stream()
            .filter(r -> !r.getId().equalsIgnoreCase(edited.getId()))
            .mapToDouble(Reward::getWeight)
            .sum();

        if (sOthers < EPS) {
            return Double.NaN;
        }

        if (c < EPS) {
            return 0D;
        }

        double denom = rarityShare - c;
        if (denom <= EPS) {
            return Math.min(1e9D, Math.max(edited.getWeight(), sOthers * 1_000_000D));
        }

        double w = (c * sOthers) / denom;
        return Math.min(1e9D, Math.max(EPS * 100D, w));
    }
}
