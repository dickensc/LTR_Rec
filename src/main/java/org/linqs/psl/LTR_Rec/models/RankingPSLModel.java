package org.linqs.psl.LTR_Rec.models;

import org.linqs.psl.LTR_Rec.ablationSettings.AblationSettingFactory;
import org.linqs.psl.LTR_Rec.modelSettings.ModelSetting;
import org.linqs.psl.LTR_Rec.modelSettings.ModelSettingFactory;
import org.linqs.psl.LTR_Rec.ablationSettings.AblationSetting;
import org.linqs.psl.model.predicate.StandardPredicate;
import org.linqs.psl.model.term.ConstantType;
import org.linqs.psl.java.PSLModel;
import org.linqs.psl.model.rule.Rule;
import org.linqs.psl.database.DataStore;

import java.util.HashMap;
import java.util.Map;

public class RankingPSLModel extends PSLModel {
    String dsName;
    AblationSetting ablationSetting;
    ModelSetting defaultModelSetting;

    public RankingPSLModel(String ablationSetting, String dsname, DataStore datastore) {
        super(datastore);

        this.dsName = dsname;
        this.ablationSetting = AblationSettingFactory.getSetting(ablationSetting);
        this.defaultModelSetting = ModelSettingFactory.getSetting(dsname);
    }

    public List<StandardPredicate> addPredicates(){
        List<StandardPredicate> addedPredicates = new ArrayList<StandardPredicate>();

        // Add ablation setting predicates
        for(Map.Entry<String, ConstantType[]> PredicateTypeMapEntry: ablationSetting.getSettingPredicates().entrySet()){
            addedPredicates.add(super.addPredicate(PredicateTypeMapEntry.getKey(), PredicateTypeMapEntry.getValue()));
        }

        // Add default setting predicates
        for(Map.Entry<String, ConstantType[]> PredicateTypeMapEntry: defaultModelSetting.getSettingPredicates().entrySet()){
            addedPredicates.add(super.addPredicate(PredicateTypeMapEntry.getKey(), PredicateTypeMapEntry.getValue()));
        }

        //Set ablation setting blocking predicates
        for(String predicateName: ablationSetting.getBlockingPredicateNames()){
            StandardPredicate Canopy = super.getStandardPredicate(predicateName);
            Canopy.setBlock(true);
        }


        //Set default setting blocking predicates
        for(String predicateName: defaultModelSetting.getBlockingPredicateNames()){
            StandardPredicate Canopy = super.getStandardPredicate(predicateName);
            Canopy.setBlock(true);
        }

        return addedPredicates;
    }


    public List<Rule> addRules() {
        List<Rule> addedRules = new ArrayList<Rule>();

        // Add ablation setting predicates
        for(String Rule: ablationSetting.getSettingRules()){
            addedRules.add(super.addRule(Rule));
        }

        // Add default setting predicates
        for(String Rule: defaultModelSetting.getSettingRules()){
            addedRules.add(super.addRule(Rule));
        }

        return addedRules;
    }


    public HashMap<String, String> getObservedPredicateData(){
        HashMap<String, String> observedPredicateData = ablationSetting.getObservedPredicateData();
        observedPredicateData.putAll(defaultModelSetting.getObservedPredicateData());
        return observedPredicateData;
    }

    public HashMap<String, String> getTargetPredicateData(){
        HashMap<String, String> targetPredicateData = ablationSetting.getTargetPredicateData();
        targetPredicateData.putAll(defaultModelSetting.getTargetPredicateData());
        return targetPredicateData;
    }

    public HashMap<String, String> getTruthPredicateData(){
        HashMap<String, String> truthPredicateData = ablationSetting.getTruthPredicateData();
        truthPredicateData.putAll(defaultModelSetting.getTruthPredicateData());
        return truthPredicateData;
    }


    public StandardPredicate[] getClosedPredicates(){
        StandardPredicate[] closedPredicates;

        String[] ablationClosedPredicateNames = ablationSetting.getClosedPredicateNames();
        String[] defaultClosedPredicateNames = ablationSetting.getClosedPredicateNames();

        closedPredicates = new StandardPredicate[ablationClosedPredicateNames.length +
                defaultClosedPredicateNames.length];

        int i;
        for(i = 0; i < ablationClosedPredicateNames.length; i++) {
            closedPredicates[i] = this.getStandardPredicate(ablationClosedPredicateNames[i]);
        }
        for(i = ablationClosedPredicateNames.length; i < closedPredicates.length; i++) {
            closedPredicates[i] = this.getStandardPredicate(defaultClosedPredicateNames[i -
                    ablationClosedPredicateNames.length]);
        }

        return closedPredicates;
    }

    public String[] getOpenPredicateNames(){
        String[] ablationOpenPredicateNames = ablationSetting.getOpenPredicateNames();
        String[] defaultOpenPredicateNames = defaultModelSetting.getOpenPredicateNames();
        String[] openPredicateNames = new String[ablationOpenPredicateNames.length + defaultOpenPredicateNames.length];

        int i;
        for(i = 0; i < ablationOpenPredicateNames.length; i++) {
            openPredicateNames[i] = ablationOpenPredicateNames[i];
        }
        for(i = defaultOpenPredicateNames.length; i < openPredicateNames.length; i++) {
            openPredicateNames[i] = defaultOpenPredicateNames[i - ablationOpenPredicateNames.length];
        }

        return openPredicateNames;
    }


}
