package org.linqs.psl.LTR_Rec.ablationSettings;

import org.linqs.psl.model.term.ConstantType;
import org.linqs.psl.LTR_Rec.ablationSettings.AblationSetting;

import java.util.HashMap;

public class DefaultSetting extends AblationSetting {

    @Override
    public String[] getSettingRules() {
        String[] settingRules;
        settingRules = new String[0];
        return settingRules;
    }

    @Override
    public HashMap<String, ConstantType[]> getSettingPredicates() {
        HashMap<String, ConstantType[]> settingPredicates = new HashMap<>();
        // No data.
        return settingPredicates;
    }

    @Override
    public String[] getObservedPredicateNames() {
        String[] ObservedPredicateData = new String[0];
         // No data.
        return ObservedPredicateData;
    }

    @Override
    public String[] getTargetPredicateNames() {
        String[] TargetPredicateData = new String[0];
         // No data.
        return TargetPredicateData;
    }

    @Override
    public String[] getTruthPredicateNames() {
        String[] TruthPredicateData = new String[0];
        // No data.
        return TruthPredicateData;
    }

    @Override
    public String[] getOpenPredicateNames() {
        String[] openPredicates;
        // No data.
        openPredicates = new String[0];
        return openPredicates;
    }

    @Override
    public String[] getClosedPredicateNames() {
        String[] closedPredicates;
        // No data.
        closedPredicates = new String[0];
        return closedPredicates;
    }

    @Override
    public String[] getBlockingPredicateNames() {
        String[] blockingPredicateNames;
        // No blocks.
        blockingPredicateNames = new String[0];
        return blockingPredicateNames;
    }
}
