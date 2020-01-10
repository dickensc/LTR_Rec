package org.linqs.psl.LTR_Rec.modelSettings;

import org.linqs.psl.LTR_Rec.modelSettings.ModelSetting;
import org.linqs.psl.model.term.ConstantType;

import java.util.HashMap;

public class MovieLensSetting extends ModelSetting {

    @Override
    public String[] getSettingRules() {
        String[] rules = new String[2];
        rules[0] = "1: RATING(U, M1) & SIMMOVIES(M1, M2) >> RATING(U, M2) ^2";
        rules[1] = "1: RATING(U1, M) & SIMUSERS(U1, U2) >> RATING(U2, M) ^2";
        return rules;
    }

    @Override
    public HashMap<String, ConstantType[]> getSettingPredicates() {
        HashMap<String, ConstantType[]> predicates = new HashMap<>();

        predicates.put("Rating", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("SimMovies", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("SimUsers", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});

        return predicates;
    }

    private String predicateNameToDataPrefix(String PredicateName) {
        String prefix;

        switch (PredicateName){
            case "Preference":
                prefix = "rel_rank";
                break;
            case "SimilarUsersBlock":
                prefix = "sim_users";
                break;
            case "SimilarItemsBlock":
                prefix = "sim_items";
                break;
            default:
                throw new IllegalArgumentException("Predicate Name:" + PredicateName + " does not exist for MovieLens");
        }

        return prefix;
    }


    @Override
    public HashMap<String, String> getObservedPredicateData() {
        return getObservedPredicateData(new String[0]);
    }

    @Override
    public HashMap<String, String> getObservedPredicateData(String[] ablationPredicateNames) {
        HashMap<String, String> ObservedPredicateData = new HashMap<>();

        // Default
        ObservedPredicateData.put("Rating", "rating");
        ObservedPredicateData.put("SimMovies", "sim_items");
        ObservedPredicateData.put("SimUsers", "sim_users");

        // Ablation predicates
        for(String predicateName: ablationPredicateNames){
            ObservedPredicateData.put(predicateName, this.predicateNameToDataPrefix(predicateName));
        }


        return ObservedPredicateData;
    }

    @Override
    public HashMap<String, String> getTargetPredicateData() {
        return getTargetPredicateData(new String[0]);
    }

    @Override
    public HashMap<String, String> getTargetPredicateData(String[] ablationPredicateNames) {
        HashMap<String, String> TargetPredicateData = new HashMap<>();

        // Default
        TargetPredicateData.put("Rating", "rating");

        // Ablation predicates
        for(String predicateName: ablationPredicateNames){
            TargetPredicateData.put(predicateName, this.predicateNameToDataPrefix(predicateName));
        }

        return TargetPredicateData;
    }

    @Override
    public HashMap<String, String> getTruthPredicateData() {
        return getTruthPredicateData(new String[0]);
    }

    @Override
    public HashMap<String, String> getTruthPredicateData(String[] ablationPredicateNames) {
        HashMap<String, String> TruthPredicateData = new HashMap<>();

        // default
        TruthPredicateData.put("Rating", "rating");

        // Ablation predicates
        for(String predicateName: ablationPredicateNames){
            TruthPredicateData.put(predicateName, this.predicateNameToDataPrefix(predicateName));
        }

        return TruthPredicateData;
    }

    @Override
    public String[] getOpenPredicateNames() {
        String[] openPredicates = new String[1];
        openPredicates[0] = "Rating";
        return new String[0];
    }

    @Override
    public String[] getClosedPredicateNames() {
        String[] closedPredicateNames = new String[2];

        closedPredicateNames[0] = "SimMovies" ;
        closedPredicateNames[1] = "SimUsers";

        return closedPredicateNames;
    }

    @Override
    public String[] getBlockingPredicateNames() {
        return new String[0];
    }
}
