package org.linqs.psl.LTR_Rec.ablationSettings;

import org.linqs.psl.model.term.ConstantType;
import org.linqs.psl.model.predicate.StandardPredicate;

import java.lang.IllegalArgumentException;
import java.util.HashMap;

public class AblationSetting {
    private String setting;

    public AblationSetting(String setting) {
        this.setting = setting;
        switch(setting)
        {
            case "Default":
                break;
            case "Pairwise Preference 1":
                break;
            default:
                throw new IllegalArgumentException("setting must be one of: [Default]");
        }
    }

    public String[] getSettingRules() {
        String[] settingRules;

        if(setting.matches("Default")) {
            // No settings to add.
            settingRules = new String[0];
        } else if(setting.matches("Pairwise Preference 1")) {
            // Similarity Predicates
            settingRules = new String[4];
            // pair-wise preferences
            settingRules[0] = "Preference(U1, I1, I2) + Preference(U1, I2, I1) = 1.";
            settingRules[1] = "1: Preference(U1, I1, I2) & SimilarUsers(U1, U2) & QueryQueryCanopy(U1, U2) & ItemItemCanopy(I1, I2) -> Preference(U2, I1, I2) ^2";
            settingRules[2] = "1: Preference(U1, I1, I2) & SimilarItems(I2, I3) & ItemItemCanopy(I1, I2) & ItemItemCanopy(I1, I3) & ItemItemCanopy(I2, I3) -> Preference(U1, I1, I3) ^2";

            // pair-wise and point-wise relation
            settingRules[3] = "1: 0.5 * RATING(U1, I1) - 0.5 * RATING(U1, I2) + 0.5 <= Preference(U1, I1, I2) ^2";
        } else {
            // How did this happen? Did I change the state somewhere?
            throw new IllegalStateException("Setting state became invalid: " + this.setting);
        }

        return settingRules;
    }

    public HashMap<String, ConstantType[]> getSettingPredicates() {
        HashMap<String, ConstantType[]> settingPredicates = new HashMap<String, ConstantType[]>();
        if(setting.matches("Default")) {
            // No settings to add.
        } else if(setting.matches("Pairwise Preference 1")) {
            // Similarity Predicates
            settingPredicates.put("SimilarUsers",
                    new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
            settingPredicates.put("SimilarItems",
                    new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});

            // Pairwise Predicate
            settingPredicates.put("Preference",
                    new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID,
                            ConstantType.UniqueStringID});

            // Blocking Predicates
            settingPredicates.put("QueryQueryCanopy",
                    new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
            settingPredicates.put("ItemItemCanopy",
                    new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        } else {
            // How did this happen? Did I change the state somewhere?
            throw new IllegalStateException("Setting state became invalid: " + this.setting);
        }
        return settingPredicates;
    }

    public HashMap<String, String> getObservedPredicateData() {
        HashMap<String, String> ObservedPredicateData = new HashMap<>();

        if(setting.matches("Default")) {
            // No data.
        } else if(setting.matches("Pairwise Preference 1")) {
            ObservedPredicateData.put("Preference", "_pref");
            ObservedPredicateData.put("SimilarUsers", "_sim_users");
            ObservedPredicateData.put("SimilarItems", "_sim_items");
            ObservedPredicateData.put("QueryQueryCanopy", "_query_query_canopy");
            ObservedPredicateData.put("ItemItemCanopy", "_item_item_canopy");
        } else {
            // How did this happen? Did I change the state somewhere?
            throw new IllegalStateException("Setting state became invalid: " + this.setting);
        }

        return ObservedPredicateData;
    }

    public HashMap<String, String> getTargetPredicateData() {
        HashMap<String, String> TargetPredicateData = new HashMap<>();

        if(setting.matches("Default")) {
            // No data.
        } else if(setting.matches("Pairwise Preference 1")) {
            TargetPredicateData.put("Preference", "_pref");
        } else {
            // How did this happen? Did I change the state somewhere?
            throw new IllegalStateException("Setting state became invalid: " + this.setting);
        }

        return TargetPredicateData;
    }

    public String[] getOpenPredicateNames() {
        String[] openPredicates;

        if(setting.matches("Default")) {
            // No data.
            openPredicates = new String[0];
        } else if(setting.matches("Pairwise Preference 1")) {
            openPredicates = new String[1];
            openPredicates[0] = "Preference";
        } else {
            // How did this happen? Did I change the state somewhere?
            throw new IllegalStateException("Setting state became invalid: " + this.setting);
        }

        return openPredicates;
    }

    public String[] getClosedPredicateNames() {
        String[] closedPredicates;

        if(setting.matches("Default")) {
            // No data.
            closedPredicates = new String[0];
        } else if(setting.matches("Pairwise Preference 1")) {
            closedPredicates = new String[2];
            closedPredicates[0] = "QueryQueryCanopy";
            closedPredicates[1] = "ItemItemCanopy";
        } else {
            // How did this happen? Did I change the state somewhere?
            throw new IllegalStateException("Setting state became invalid: " + this.setting);
        }

        return closedPredicates;
    }

    public HashMap<String, String> getTruthPredicateData() {
        HashMap<String, String> TruthPredicateData = new HashMap<>();

        if(setting.matches("Default")) {
            // No data.
        } else if(setting.matches("Pairwise Preference 1")) {
            TruthPredicateData.put("Preference", "_pref");
        } else {
            // How did this happen? Did I change the state somewhere?
            throw new IllegalStateException("Setting state became invalid: " + this.setting);
        }

        return TruthPredicateData;
    }

    public String[] getBlockingPredicateNames() {
        String[] blockingPredicateNames;
        if(setting.matches("Default")) {
            // No blocks.
            blockingPredicateNames = new String[0];
        } else if(setting.matches("Pairwise Preference 1")) {
            // Add Blocking Predicates
            blockingPredicateNames = new String[] {"QueryQueryCanopy", "ItemItemCanopy"};
        } else {
            // How did this happen? Did I change the state somewhere?
            throw new IllegalStateException("Setting state became invalid: " + this.setting);
        }
        return blockingPredicateNames;
    }
}
