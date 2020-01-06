package org.linqs.psl.LTR_Rec.modelSettings;

import org.linqs.psl.model.term.ConstantType;

import java.util.HashMap;

public abstract class ModelSetting {
    public abstract String[] getSettingRules();

    public abstract HashMap<String, ConstantType[]> getSettingPredicates();

    public abstract HashMap<String, String> getObservedPredicateData();

    public abstract HashMap<String, String> getTargetPredicateData();

    public abstract HashMap<String, String> getTruthPredicateData();

    public abstract String[] getOpenPredicateNames();

    public abstract String[] getClosedPredicateNames();

    public abstract String[] getBlockingPredicateNames();
}
