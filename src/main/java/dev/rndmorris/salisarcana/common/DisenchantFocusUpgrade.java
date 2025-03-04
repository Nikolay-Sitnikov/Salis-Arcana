package dev.rndmorris.salisarcana.common;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import dev.rndmorris.salisarcana.SalisArcana;
import dev.rndmorris.salisarcana.config.ConfigModuleRoot;
import dev.rndmorris.salisarcana.lib.ArrayHelper;
import dev.rndmorris.salisarcana.lib.FormattedResearchPage;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.wands.FocusUpgradeType;

public class DisenchantFocusUpgrade extends FocusUpgradeType {

    public static final String RESEARCH_KEY = "salisarcana:FOCUS_DISENCHANTING";
    private static final int[] RANK_TO_XP_POINTS = new int[] { 136, 272, 516, 956, 1760 }; // 8L, 16L, 24L, 32L, 40L
    private static final ResourceLocation ICON_LOCATION = new ResourceLocation(
        SalisArcana.MODID,
        "textures/foci/disenchant.png");
    private static final String NAME_KEY = "focus.upgrade.salisarcana:disenchant.name";
    private static final String UNFORMATTED_TOOLTIP_KEY = "focus.upgrade.salisarcana:disenchant.text";
    private static final String GENERIC_TOOLTIP_KEY = "focus.upgrade.salisarcana:disenchant.text.generic";
    private static final String SPECIFIC_TOOLTIP_KEY = "focus.upgrade.salisarcana:disenchant.text.specific";
    private static final AspectList ASPECT_VALUE = (new AspectList()).add(Aspect.ENTROPY, 1);
    public static short upgradeID;
    public static DisenchantFocusUpgrade baseInstance;
    public static ResearchItem researchItem;

    public static void initialize() {
        upgradeID = (short) FocusUpgradeType.types.length;
        baseInstance = new DisenchantFocusUpgrade(upgradeID);
    }

    public static void registerResearch() {
        final var researchAspects = (new AspectList()).add(Aspect.ENTROPY, 4)
            .add(Aspect.AURA, 4)
            .add(Aspect.VOID, 8)
            .add(Aspect.MAGIC, 6);
        final var percentXP = ConfigModuleRoot.enhancements.focusDisenchantingRefundPercentage.getValueOrDefault();
        researchItem = new ResearchItem(RESEARCH_KEY, "THAUMATURGY", researchAspects, -1, -8, 2, ICON_LOCATION);
        researchItem.setPages(
            new FormattedResearchPage(
                "tc.research_page.salisarcana:FOCUS_DISENCHANTING.0",
                new Object[] { percentXP }));
        researchItem.setConcealed()
            .setSecondary()
            .setParents("FOCALMANIPULATION")
            .registerResearchItem();

        if (ConfigModuleRoot.enhancements.autoUnlockFocusDisenchanting.isEnabled()) {
            final var parent = ResearchCategories.getResearch("FOCALMANIPULATION");
            parent.siblings = ArrayHelper.appendToArray(parent.siblings, RESEARCH_KEY);
        }
    }

    public static DisenchantFocusUpgrade createSpecific(final short[] upgrades) {
        // This is stupid, but the only other solution is ASM.
        FocusUpgradeType.types[upgradeID] = null;
        final var out = new DisenchantFocusUpgrade(upgradeID, upgrades);
        FocusUpgradeType.types[upgradeID] = baseInstance;
        return out;
    }

    public final FocusUpgradeType lastUpgrade;
    public final int lastUpgradeLevel;
    public final int lastRank;

    private DisenchantFocusUpgrade(short id) {
        super(id, ICON_LOCATION, NAME_KEY, UNFORMATTED_TOOLTIP_KEY, ASPECT_VALUE);
        this.lastUpgrade = null;
        this.lastUpgradeLevel = -1;
        this.lastRank = -1;
    }

    private DisenchantFocusUpgrade(short id, final short[] upgrades) {
        super(id, ICON_LOCATION, NAME_KEY, UNFORMATTED_TOOLTIP_KEY, ASPECT_VALUE);

        if (upgrades.length == 0 || upgrades[0] == -1) {
            this.lastUpgrade = null;
            this.lastUpgradeLevel = -1;
            this.lastRank = -1;
        } else {
            int rank = 1;
            while (rank < upgrades.length && upgrades[rank] != -1) rank++;
            this.lastRank = rank;

            short lastUpgradeID = upgrades[rank - 1];
            this.lastUpgrade = FocusUpgradeType.types[lastUpgradeID];

            int level = 1;
            for (int i = rank - 2; i >= 0; i--) {
                if (upgrades[i] == lastUpgradeID) level++;
            }
            this.lastUpgradeLevel = level;
        }
    }

    @Override
    public String getLocalizedText() {
        if (this.lastUpgradeLevel > 0) {
            StringBuilder upgradeName = new StringBuilder(this.lastUpgrade.getLocalizedName());
            if (this.lastUpgradeLevel > 1) {
                upgradeName.append(' ');
                upgradeName.append(StatCollector.translateToLocal("enchantment.level." + this.lastUpgradeLevel));
            }
            return StatCollector
                .translateToLocalFormatted(SPECIFIC_TOOLTIP_KEY, upgradeName.toString(), this.getXpPoints());
        } else {
            return StatCollector.translateToLocalFormatted(
                GENERIC_TOOLTIP_KEY,
                ConfigModuleRoot.enhancements.focusDisenchantingRefundPercentage.getValueOrDefault());
        }
    }

    public int getXpPoints() {
        return (RANK_TO_XP_POINTS[Math.min(this.lastRank, 5) - 1]
            * ConfigModuleRoot.enhancements.focusDisenchantingRefundPercentage.getValueOrDefault()) / 100;
    }

    public AspectList getVisPoints() {
        return (new AspectList()).add(Aspect.ENTROPY, 100 << this.lastRank);
    }
}
