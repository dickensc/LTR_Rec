package org.linqs.psl.LTR_Rec.modelSettings;

import org.linqs.psl.LTR_Rec.modelSettings.ModelSetting;
import org.linqs.psl.model.term.ConstantType;

import java.util.HashMap;

public class YelpSetting extends ModelSetting {

    @Override
    public String[] getSettingRules() {
        String[] rules = new String[21];
        // Similarities like Pearson, Cosine, and Adjusted Cosine Similarity between items.
        rules[0] = "1.0 :  rated(U,I1) & rated(U,I2) & rating(U,I1) & sim_pearson_items(I1,I2) >> rating(U,I2)";
        rules[1] = "1.0 :  rated(U,I1) & rated(U,I2) & rating(U,I1) & sim_cosine_items(I1,I2) >> rating(U,I2)";
        rules[2] = "1.0 :  rated(U,I1) & rated(U,I2) & rating(U,I1) & sim_adjcos_items(I1,I2) >> rating(U,I2)";

        // Similarities like Pearson and Cosine Similarity between users.
        rules[3] = "1.0 :  rated(U1,I) & rated(U2,I) & rating(U1,I) & sim_pearson_users(U1,U2) >> rating(U2,I)";
        rules[4] = "1.0 :  rated(U1,I) & rated(U2,I) & rating(U1,I) & sim_cosine_users(U1,U2) >> rating(U2,I)";

        // Other low dimension space similarities like Matrix Factorization Cosine and Euclidean Similarity between users.
        rules[5] = "1.0 :  user(U1) & user(U2) & item(I) & rating(U1,I) & rated(U1,I) &rated(U2,I) & sim_mf_cosine_users(U1,U2) >> rating(U2,I)";
        rules[6] = "1.0 :  user(U1) & user(U2) & item(I) & rating(U1,I) & rated(U1,I) &rated(U2,I) & sim_mf_euclidean_users(U1,U2) >> rating(U2,I)";

        // Other low dimension space similarities like Matrix Factorization Cosine and Euclidean Similarity between items.
        rules[7] = "1.0 :  user(U) & item(I1) & item(I2) & rating(U,I1) & rated(U,I1) & rated(U,I2) & sim_mf_cosine_items(I1,I2) >> rating(U,I2)";
        rules[8] = "1.0 :  user(U) & item(I1) & item(I2) & rating(U,I1) & rated(U,I1) & rated(U,I2) & sim_mf_euclidean_items(I1,I2) >> rating(U,I2)";

        // Predictions by different other methods like SGD, Item based Pearson methods, and BPMF methods.
        rules[9] = "1.0 : sgd_rating(U,I) >> rating(U,I)";
        rules[10] = "1.0 : rating(U,I) >> sgd_rating(U,I)";
        rules[11] = "1.0 : item_pearson_rating(U,I) >> rating(U,I)";
        rules[12] = "1.0 : rating(U,I) >> item_pearson_rating(U,I)";
        rules[13] = "1.0 : bpmf_rating(U,I) >> rating(U,I)";
        rules[14] = "1.0 : rating(U,I) >> bpmf_rating(U,I)";

        // Average prior of user rating and item ratings.
        rules[15] = "1.0  : user(U) & item(I) & rated(U,I) & avg_user_rating(U) >> rating(U,I)";
        rules[16] = "1.0  : user(U) & item(I) & rated(U,I) & rating(U,I) >> avg_user_rating(U)";
        rules[17] = "1.0  : user(U) & item(I) & rated(U,I) & avg_item_rating(I) >> rating(U,I)";
        rules[18] = "1.0  : user(U) & item(I) & rated(U,I) & rating(U,I) >> avg_item_rating(I)";

        // Social rule of friendship influencing ratings.
        rules[19] = "1.0 : rated(U1,I) & rated(U2,I) & users_are_friends(U1,U2) & rating(U1,I) >> rating(U2,I)";

        // Content rule by Jaccard similarity.
        rules[20] = "1.0  :  rated(U,I1) & rated(U,I2) & rating(U,I1) & sim_content_items_jaccard(I1,I2) >> rating(U,I2)";
        return rules;
    }

    @Override
    public HashMap<String, ConstantType[]> getSettingPredicates() {
        HashMap<String, ConstantType[]> predicates = new HashMap<>();

        predicates.put("user", new ConstantType[] {ConstantType.UniqueStringID});
        predicates.put("item", new ConstantType[] {ConstantType.UniqueStringID});
        predicates.put("users_are_friends", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_content_items_jaccard", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_pearson_items",new ConstantType[] { ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_cosine_items", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_adjcos_items", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_pearson_users", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_cosine_users", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_mf_cosine_users", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_mf_euclidean_users", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_mf_cosine_items", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sim_mf_euclidean_items", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("avg_user_rating", new ConstantType[] {ConstantType.UniqueStringID});
        predicates.put("avg_item_rating", new ConstantType[] {ConstantType.UniqueStringID});
        predicates.put("rated", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("sgd_rating", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("bpmf_rating", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("item_pearson_rating", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});
        predicates.put("rating", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});

        return predicates;
    }

    @Override
    public HashMap<String, String> getObservedPredicateData() {
        HashMap<String, String> ObservedPredicateData = new HashMap<>();
        ObservedPredicateData.put("user", "user");
        ObservedPredicateData.put("item", "item");
        ObservedPredicateData.put("users_are_friends", "users_are_friends");
        ObservedPredicateData.put("sim_content_items_jaccard", "sim_content_items_jaccard");
        ObservedPredicateData.put("sim_pearson_items", "sim_pearson_items");
        ObservedPredicateData.put("sim_cosine_items", "sim_cosine_items");
        ObservedPredicateData.put("sim_adjcos_items", "sim_adjcos_items");
        ObservedPredicateData.put("sim_pearson_users", "sim_pearson_users");
        ObservedPredicateData.put("sim_cosine_users", "sim_cosine_users");
        ObservedPredicateData.put("sim_mf_cosine_users", "sim_mf_cosine_users");
        ObservedPredicateData.put("sim_mf_euclidean_users", "sim_mf_euclidean_users");
        ObservedPredicateData.put("sim_mf_cosine_items", "sim_mf_cosine_items");
        ObservedPredicateData.put("sim_mf_euclidean_items", "sim_mf_euclidean_items");
        ObservedPredicateData.put("avg_user_rating", "avg_user_rating");
        ObservedPredicateData.put("avg_item_rating", "avg_item_rating");
        ObservedPredicateData.put("rated", "rated");
        ObservedPredicateData.put("sgd_rating", "sgd_rating");
        ObservedPredicateData.put("bpmf_rating", "bpmf_rating");
        ObservedPredicateData.put("item_pearson_rating", "item_pearson_rating");
        return ObservedPredicateData;
    }

    @Override
    public HashMap<String, String> getTargetPredicateData() {
        HashMap<String, String> TargetPredicateData = new HashMap<>();
        TargetPredicateData.put("Rating", "rating");
        return TargetPredicateData;
    }

    @Override
    public HashMap<String, String> getTruthPredicateData() {
        HashMap<String, String> TruthPredicateData = new HashMap<>();
        TruthPredicateData.put("Rating", "rating");
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
        String[] closedPredicateNames = new String[19];

        closedPredicateNames[0] = "user";
        closedPredicateNames[1] = "item";
        closedPredicateNames[2] = "users_are_friends";
        closedPredicateNames[3] = "sim_content_items_jaccard";
        closedPredicateNames[4] = "sim_pearson_items";
        closedPredicateNames[5] = "sim_cosine_items";
        closedPredicateNames[6] = "sim_adjcos_items";
        closedPredicateNames[7] = "sim_pearson_users";
        closedPredicateNames[8] = "sim_cosine_users";
        closedPredicateNames[9] = "sim_mf_cosine_users";
        closedPredicateNames[10] = "sim_mf_euclidean_users";
        closedPredicateNames[11] = "sim_mf_cosine_items";
        closedPredicateNames[12] = "sim_mf_euclidean_items";
        closedPredicateNames[13] = "avg_user_rating";
        closedPredicateNames[14] = "avg_item_rating";
        closedPredicateNames[15] = "rated";
        closedPredicateNames[16] = "sgd_rating";
        closedPredicateNames[17] = "bpmf_rating";
        closedPredicateNames[18] = "item_pearson_rating";

        return closedPredicateNames;
    }

    @Override
    public String[] getBlockingPredicateNames() {
        return new String[0];
    }
}
