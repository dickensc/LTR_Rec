package org.linqs.psl.LTR_Rec.ablationSettings;

import org.linqs.psl.model.term.ConstantType;
import org.linqs.psl.LTR_Rec.ablationSettings.AblationSetting;

import java.lang.IllegalArgumentException;
import java.util.HashMap;

public class PairwiseBlockingSetting extends AblationSetting {

    @Override
    public String[] getSettingRules() {
        String[] settingRules;

        // Similarity Predicates
        settingRules = new String[4];
        // pair-wise preferences
        settingRules[0] = "Preference(U1, I1, I2) + Preference(U1, I2, I1) = 1.";
        settingRules[1] = "1: Preference(U1, I1, I2) & SimilarUsersBlock(U1, U2) -> Preference(U2, I1, I2) ^2";
        settingRules[2] = "1: Preference(U1, I1, I2) & SimilarItemsBlock(I2, I3) & SimilarItemsBlock(I1, I2) -> Preference(U1, I1, I3) ^2";

        // pair-wise and point-wise relation
        settingRules[3] = "1: 0.5 * RATING(U1, I1) - 0.5 * RATING(U1, I2) + 0.5 <= Preference(U1, I1, I2) ^2";

        return settingRules;
    }

    @Override
    public HashMap<String, ConstantType[]> getSettingPredicates() {
        /// Assuming Rating predicate is in default setting

        HashMap<String, ConstantType[]> settingPredicates = new HashMap<String, ConstantType[]>();

        // Pairwise Predicate
        settingPredicates.put("Preference",
                new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID,
                        ConstantType.UniqueStringID});

        // Blocking Predicates
        settingPredicates.put("SimilarUsersBlock",
                new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        settingPredicates.put("SimilarItemsBlock",
                new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});

        return settingPredicates;
    }

    @Override
    public String[] getObservedPredicateNames() {
        /// Assuming Rating predicate is in default setting

        String[] ObservedPredicateData = new String[5];

        ObservedPredicateData[0] = "Preference";
        ObservedPredicateData[1] = "SimilarUsersBlock";
        ObservedPredicateData[2] = "SimilarItemsBlock";

        return ObservedPredicateData;
    }

    @Override
    public String[] getTargetPredicateNames() {
        /// Assuming Rating predicate is in default setting

        String[] TargetPredicateData = new String[1];
        TargetPredicateData[0] = "Preference";
        return TargetPredicateData;
    }

    @Override
    public String[] getTruthPredicateNames() {
        /// Assuming Rating predicate is in default setting

        String[] TruthPredicateData = new String[1];
        TruthPredicateData[0] = "Preference";
        return TruthPredicateData;
    }


    @Override
    public String[] getOpenPredicateNames() {
        /// Assuming Rating predicate is in default setting

        String[] openPredicates;

        openPredicates = new String[1];
        openPredicates[0] = "Preference";

        return openPredicates;
    }

    @Override
    public String[] getClosedPredicateNames() {
        String[] closedPredicates;

        closedPredicates = new String[2];
        closedPredicates[0] = "SimilarUsersBlock";
        closedPredicates[1] = "SimilarItemsBlock";

        return closedPredicates;
    }

    @Override
    public String[] getBlockingPredicateNames() {
        String[] blockingPredicateNames;
        // Add Blocking Predicates
        blockingPredicateNames = new String[] {"SimilarUsersBlock", "SimilarItemsBlock"};
        return blockingPredicateNames;
    }
}
