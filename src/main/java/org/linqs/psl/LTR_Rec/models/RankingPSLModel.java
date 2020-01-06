package org.linqs.psl.LTR_Rec.models;

import org.linqs.psl.LTR_Rec.modelSettings.ModelSetting;
import org.linqs.psl.LTR_Rec.modelSettings.ModelSettingFactory;
import org.linqs.psl.LTR_Rec.ablationSettings.AblationSetting;
import org.linqs.psl.model.predicate.StandardPredicate;
import org.linqs.psl.model.term.ConstantType;
import org.linqs.psl.java.PSLModel;
import org.linqs.psl.model.rule.Rule;
import org.linqs.psl.database.DataStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingPSLModel extends PSLModel {
    String dsName;
    AblationSetting ablationSetting;
    ModelSetting defaultModelSetting;

    public RankingPSLModel(String ablationSetting, String dsname, DataStore datastore) {
        super(datastore);

        this.dsName = dsname;
        this.ablationSetting = new AblationSetting(ablationSetting);
        this.defaultModelSetting = ModelSettingFactory.getSetting(dsname);
    }

    public List<StandardPredicate> addAblationSettingPredicates(){
        List<StandardPredicate> addedPredicates = new ArrayList<StandardPredicate>();

        // Add ablation setting predicates
        for(Map.Entry<String, ConstantType[]> PredicateTypeMapEntry: ablationSetting.getSettingPredicates().entrySet()){
            addedPredicates.add(super.addPredicate(PredicateTypeMapEntry.getKey(), PredicateTypeMapEntry.getValue()));
        }

        //Set ablation setting blocking predicates
        for(String predicateName: ablationSetting.getBlockingPredicateNames()){
            StandardPredicate Canopy = super.getStandardPredicate(predicateName);
            Canopy.setBlock(true);
        }

        return addedPredicates;
    }

    public List<StandardPredicate> addDefaultPredicates(){
        List<StandardPredicate> addedPredicates = new ArrayList<StandardPredicate>();

        // Add ablation setting predicates
        for(Map.Entry<String, ConstantType[]> PredicateTypeMapEntry: defaultModelSetting.getSettingPredicates().entrySet()){
            addedPredicates.add(super.addPredicate(PredicateTypeMapEntry.getKey(), PredicateTypeMapEntry.getValue()));
        }

        //Set ablation setting blocking predicates
        for(String predicateName: defaultModelSetting.getBlockingPredicateNames()){
            StandardPredicate Canopy = super.getStandardPredicate(predicateName);
            Canopy.setBlock(true);
        }

        return addedPredicates;
    }

    public List<Rule> addAblationSettingRules() {
        List<Rule> addedRules = new ArrayList<Rule>();

        // Add ablation setting predicates
        for(String Rule: ablationSetting.getSettingRules()){
            addedRules.add(super.addRule(Rule));
        }

        return addedRules;
    }

    public List<Rule> addDefaultRules() {
        List<Rule> addedRules = new ArrayList<Rule>();

        // Add ablation setting predicates
        for(String Rule: defaultModelSetting.getSettingRules()){
            addedRules.add(super.addRule(Rule));
        }

        return addedRules;
    }

    public HashMap<String, String> getAblationObservedPredicateData(){
        return ablationSetting.getObservedPredicateData();
    }

    public HashMap<String, String> getAblationTargetPredicateData(){
        return ablationSetting.getTargetPredicateData();
    }

    public HashMap<String, String> getAblationTruthPredicateData(){
        return ablationSetting.getTruthPredicateData();
    }

    public HashMap<String, String> getDefaultObservedPredicateData(){
        return defaultModelSetting.getObservedPredicateData();
    }


    public HashMap<String, String> getDefaultTargetPredicateData(){
        return defaultModelSetting.getTargetPredicateData();
    }


    public HashMap<String, String> getDefaultTruthPredicateData(){
        return defaultModelSetting.getTruthPredicateData();
    }

    public StandardPredicate[] getAblationClosedPredicates(){
        StandardPredicate[] closedPredicates;

        String[] closedPredicateNames = ablationSetting.getClosedPredicateNames();

        closedPredicates = new StandardPredicate[closedPredicateNames.length];

        int i;
        for(i = 0; i < closedPredicateNames.length; i++) {
            closedPredicates[i] = this.getStandardPredicate(closedPredicateNames[i]);
        }

        return closedPredicates;
    }

    public String[] getAblationOpenPredicateNames(){
        return ablationSetting.getOpenPredicateNames();
    }

    public String[] getDefaultOpenPredicateNames(){
        return defaultModelSetting.getOpenPredicateNames();
    }

    public StandardPredicate[] getDefaultClosedPredicates() {
        StandardPredicate[] closedPredicates;

        String[] closedPredicateNames = defaultModelSetting.getClosedPredicateNames();

        closedPredicates = new StandardPredicate[closedPredicateNames.length];

        int i;
        for(i = 0; i < closedPredicateNames.length; i++) {
            closedPredicates[i] = this.getStandardPredicate(closedPredicateNames[i]);
        }

        return closedPredicates;
    }

}
