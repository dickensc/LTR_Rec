package org.linqs.psl.LTR_Rec.models;

import org.linqs.psl.model.predicate.StandardPredicate;
import org.linqs.psl.model.term.ConstantType;
import org.linqs.psl.java.PSLModel;
import org.linqs.psl.model.rule.Rule;
import org.linqs.psl.database.DataStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RankingPSLModel extends PSLModel {
    String dsName;

    public RankingPSLModel(String dsname, DataStore datastore) {
        super(datastore);
        this.dsName = dsname;
    }

    public List<StandardPredicate> addDefaultPredicates(){
        List<StandardPredicate> addedPredicates = new ArrayList<StandardPredicate>();

        if(dsName.matches("movie_lens")) {

            addedPredicates.add(super.addPredicate("Rating", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("SimMovies", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("SimUsers", ConstantType.UniqueStringID, ConstantType.UniqueStringID));

        } else if(dsName.matches("jester")) {

            addedPredicates.add(super.addPredicate("AvgJokeRatingObs", ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("AvgUserRatingObs", ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("Joke", ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("RatingPrior", ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("User", ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("SimObsRating", ConstantType.UniqueStringID,
                    ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("Rating", ConstantType.UniqueStringID, ConstantType.UniqueStringID));

        } else if(dsName.matches("lastfm") || dsName.matches("yelp")) {

            addedPredicates.add(super.addPredicate("user", ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("item", ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("users_are_friends", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_content_items_jaccard", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_pearson_items", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_cosine_items", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_adjcos_items", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_pearson_users", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_cosine_users", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_mf_cosine_users", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_mf_euclidean_users", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_mf_cosine_items", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sim_mf_euclidean_items", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("avg_user_rating", ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("avg_item_rating", ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("rated", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("sgd_rating", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("bpmf_rating", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("item_pearson_rating", ConstantType.UniqueStringID, ConstantType.UniqueStringID));
            addedPredicates.add(super.addPredicate("rating", ConstantType.UniqueStringID, ConstantType.UniqueStringID));

        }

        return addedPredicates;
    }

    public List<Rule> addDefaultRules() {
        List<Rule> addedRules = new ArrayList<Rule>();

        if(dsName.matches("movie_lens")) {

            addedRules.add(super.addRule("1: RATING(U, M1) & SIMMOVIES(M1, M2) >> RATING(U, M2) ^2"));
            addedRules.add(super.addRule("1: RATING(U1, M) & SIMUSERS(U1, U2) >> RATING(U2, M) ^2"));

        } else if(dsName.matches("jester")) {

            // If J1 and J2 have similar observed ratings, then U will rate them similarly.
            addedRules.add(super.addRule("1: SIMOBSRATING(J1, J2) & RATING(U, J1) >> RATING(U, J2) ^2"));

            // Ratings should concentrate around observed User/Joke averages.
            addedRules.add(super.addRule("1: USER(U) & JOKE(J) & AVGUSERRATINGOBS(U) >> RATING(U, J) ^2"));
            addedRules.add(super.addRule("1: USER(U) & AVGJOKERATINGOBS(J) & JOKE(J) >> RATING(U, J) ^2"));
            addedRules.add(super.addRule("1: USER(U) & RATING(U, J) & JOKE(J) >> AVGUSERRATINGOBS(U) ^2"));
            addedRules.add(super.addRule("1: USER(U) & RATING(U, J) & JOKE(J) >> AVGJOKERATINGOBS(J) ^2"));

            // Two-sided prior.
            addedRules.add(super.addRule("1: USER(U) & RATINGPRIOR('0') & JOKE(J) >> RATING(U, J) ^2"));
            addedRules.add(super.addRule("1: RATING(U, J) >> RATINGPRIOR('0') ^2"));

            // Negative Prior
            addedRules.add(super.addRule("1: ~RATING(U, J) ^2"));
        } else if(dsName.matches("lastfm") || dsName.matches("yelp")) {

            // Similarities like Pearson, Cosine, and Adjusted Cosine Similarity between items.
            addedRules.add(super.addRule("1.0 :  rated(U,I1) & rated(U,I2) & rating(U,I1) & sim_pearson_items(I1,I2) >> rating(U,I2)"));
            addedRules.add(super.addRule("1.0 :  rated(U,I1) & rated(U,I2) & rating(U,I1) & sim_cosine_items(I1,I2) >> rating(U,I2)"));
            addedRules.add(super.addRule("1.0 :  rated(U,I1) & rated(U,I2) & rating(U,I1) & sim_adjcos_items(I1,I2) >> rating(U,I2)"));

            // Similarities like Pearson and Cosine Similarity between users.
            addedRules.add(super.addRule("1.0 :  rated(U1,I) & rated(U2,I) & rating(U1,I) & sim_pearson_users(U1,U2) >> rating(U2,I)"));
            addedRules.add(super.addRule("1.0 :  rated(U1,I) & rated(U2,I) & rating(U1,I) & sim_cosine_users(U1,U2) >> rating(U2,I)"));

            // Other low dimension space similarities like Matrix Factorization Cosine and Euclidean Similarity between users.
            addedRules.add(super.addRule("1.0 :  user(U1) & user(U2) & item(I) & rating(U1,I) & rated(U1,I) &rated(U2,I) & sim_mf_cosine_users(U1,U2) >> rating(U2,I)"));
            addedRules.add(super.addRule("1.0 :  user(U1) & user(U2) & item(I) & rating(U1,I) & rated(U1,I) &rated(U2,I) & sim_mf_euclidean_users(U1,U2) >> rating(U2,I)"));

            // Other low dimension space similarities like Matrix Factorization Cosine and Euclidean Similarity between items.
            addedRules.add(super.addRule("1.0 :  user(U) & item(I1) & item(I2) & rating(U,I1) & rated(U,I1) & rated(U,I2) & sim_mf_cosine_items(I1,I2) >> rating(U,I2)"));
            addedRules.add(super.addRule("1.0 :  user(U) & item(I1) & item(I2) & rating(U,I1) & rated(U,I1) & rated(U,I2) & sim_mf_euclidean_items(I1,I2) >> rating(U,I2)"));

            // Predictions by different other methods like SGD, Item based Pearson methods, and BPMF methods.
            addedRules.add(super.addRule("1.0 : sgd_rating(U,I) >> rating(U,I)"));
            addedRules.add(super.addRule("1.0 : rating(U,I) >> sgd_rating(U,I)"));
            addedRules.add(super.addRule("1.0 : item_pearson_rating(U,I) >> rating(U,I)"));
            addedRules.add(super.addRule("1.0 : rating(U,I) >> item_pearson_rating(U,I)"));
            addedRules.add(super.addRule("1.0 : bpmf_rating(U,I) >> rating(U,I)"));
            addedRules.add(super.addRule("1.0 : rating(U,I) >> bpmf_rating(U,I)"));

            // Average prior of user rating and item ratings.
            addedRules.add(super.addRule("1.0  : user(U) & item(I) & rated(U,I) & avg_user_rating(U) >> rating(U,I)"));
            addedRules.add(super.addRule("1.0  : user(U) & item(I) & rated(U,I) & rating(U,I) >> avg_user_rating(U)"));
            addedRules.add(super.addRule("1.0  : user(U) & item(I) & rated(U,I) & avg_item_rating(I) >> rating(U,I)"));
            addedRules.add(super.addRule("1.0  : user(U) & item(I) & rated(U,I) & rating(U,I) >> avg_item_rating(I)"));

            // Social rule of friendship influencing ratings.
            addedRules.add(super.addRule("1.0 : rated(U1,I) & rated(U2,I) & users_are_friends(U1,U2) & rating(U1,I) >> rating(U2,I)"));

            // Content rule by Jaccard similarity.
            addedRules.add(super.addRule("1.0  :  rated(U,I1) & rated(U,I2) & rating(U,I1) & sim_content_items_jaccard(I1,I2) >> rating(U,I2)"));
        }

        return addedRules;
    }

    public HashMap<String, String> getDefaultObservedPredicateData(){
        HashMap<String, String> ObservedPredicateData = new HashMap<>();

        if(dsName.matches("movie_lens")) {

            ObservedPredicateData.put("Rating", "rating");
            ObservedPredicateData.put("SimMovies", "sim_items");
            ObservedPredicateData.put("SimUsers", "sim_users");

        } else if(dsName.matches("jester")) {

            ObservedPredicateData.put("AvgJokeRatingObs", "avgJokeRatingObs");
            ObservedPredicateData.put("AvgUserRatingObs", "avgUserRatingObs");
            ObservedPredicateData.put("Joke", "joke");
            ObservedPredicateData.put("RatingPrior", "ratingPrior");
            ObservedPredicateData.put("User", "user");
            ObservedPredicateData.put("SimObsRating", "simObsRating");
            ObservedPredicateData.put("Rating", "rating");

        } else if(dsName.matches("lastfm") || dsName.matches("yelp")) {

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

        }

        return ObservedPredicateData;
    }

    public HashMap<String, String> getDefaultTargetsPredicateData(){
        HashMap<String, String> TargetPredicateData = new HashMap<>();

        if(dsName.matches("movie_lens")) {

            TargetPredicateData.put("Rating", "rating");

        } else if(dsName.matches("jester")) {

            TargetPredicateData.put("Rating", "rating");

        } else if(dsName.matches("lastfm") || dsName.matches("yelp")) {

            TargetPredicateData.put("rating", "rating");

        }

        return TargetPredicateData;
    }

    public HashMap<String, String> getDefaultTruthPredicateData(){
        HashMap<String, String> TruthPredicateData = new HashMap<>();

        if(dsName.matches("movie_lens")) {

            TruthPredicateData.put("Rating", "rating");

        } else if(dsName.matches("jester")) {

            TruthPredicateData.put("Rating", "rating");

        } else if(dsName.matches("lastfm") || dsName.matches("yelp")) {

            TruthPredicateData.put("Rating", "rating");

        }

        return TruthPredicateData;
    }

    public int getNumDefaultClosedPredicates() {
        int numClosedPredicates = 0;
        if(dsName.matches("movie_lens")) {

            numClosedPredicates = 2;

        } else if (dsName.matches("jester")) {

            numClosedPredicates = 6;

        } else if (dsName.matches("lastfm") || dsName.matches("yelp")) {

            numClosedPredicates = 19;

        }
        return numClosedPredicates;
    }

    public StandardPredicate[] getDefaultClosedPredicates() {
        StandardPredicate[] closedPredicates = new StandardPredicate[this.getNumDefaultClosedPredicates()];

        if(dsName.matches("movie_lens")) {
            closedPredicates[0] = this.getStandardPredicate("SimMovies");
            closedPredicates[1] = this.getStandardPredicate("SimUsers");

        } else if(dsName.matches("jester")) {

            closedPredicates[0] = this.getStandardPredicate("AvgJokeRatingObs");
            closedPredicates[1] = this.getStandardPredicate("AvgUserRatingObs");
            closedPredicates[2] = this.getStandardPredicate("Joke");
            closedPredicates[3] = this.getStandardPredicate("RatingPrior");
            closedPredicates[4] = this.getStandardPredicate("User");
            closedPredicates[5] = this.getStandardPredicate("SimObsRating");

        } else if(dsName.matches("lastfm") || dsName.matches("yelp")) {

            closedPredicates[0] = this.getStandardPredicate("user");
            closedPredicates[1] = this.getStandardPredicate("item");
            closedPredicates[2] = this.getStandardPredicate("users_are_friends");
            closedPredicates[3] = this.getStandardPredicate("sim_content_items_jaccard");
            closedPredicates[4] = this.getStandardPredicate("sim_pearson_items");
            closedPredicates[5] = this.getStandardPredicate("sim_cosine_items");
            closedPredicates[6] = this.getStandardPredicate("sim_adjcos_items");
            closedPredicates[7] = this.getStandardPredicate("sim_pearson_users");
            closedPredicates[8] = this.getStandardPredicate("sim_cosine_users");
            closedPredicates[9] = this.getStandardPredicate("sim_mf_cosine_users");
            closedPredicates[10] = this.getStandardPredicate("sim_mf_euclidean_users");
            closedPredicates[11] = this.getStandardPredicate("sim_mf_cosine_items");
            closedPredicates[12] = this.getStandardPredicate("sim_mf_euclidean_items");
            closedPredicates[13] = this.getStandardPredicate("avg_user_rating");
            closedPredicates[14] = this.getStandardPredicate("avg_item_rating");
            closedPredicates[15] = this.getStandardPredicate("rated");
            closedPredicates[16] = this.getStandardPredicate("sgd_rating");
            closedPredicates[17] = this.getStandardPredicate("bpmf_rating");
            closedPredicates[18] = this.getStandardPredicate("item_pearson_rating");
        }
        return closedPredicates;
    }

}
