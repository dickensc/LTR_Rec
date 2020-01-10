package org.linqs.psl.LTR_Rec.ablationSettings;

import org.linqs.psl.model.term.ConstantType;

import java.util.HashMap;

public abstract class AblationSetting {

    public abstract String[] getSettingRules();

    public abstract HashMap<String, ConstantType[]> getSettingPredicates();

    public abstract String[] getObservedPredicateNames();

    public abstract String[] getTargetPredicateNames();

    public abstract String[] getTruthPredicateNames();

    public abstract String[] getOpenPredicateNames();

    public abstract String[] getClosedPredicateNames();

    public abstract String[] getBlockingPredicateNames();
}
