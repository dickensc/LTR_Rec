package org.linqs.psl.LTR_Rec.modelSettings;

import org.linqs.psl.LTR_Rec.modelSettings.ModelSetting;
import org.linqs.psl.model.term.ConstantType;

import java.util.HashMap;

public class JesterSetting extends ModelSetting {

    @Override
    public String[] getSettingRules() {
        String[] rules = new String[2];

        // If J1 and J2 have similar observed ratings, then U will rate them similarly.
        rules[0] = "1: SIMOBSRATING(J1, J2) & RATING(U, J1) >> RATING(U, J2) ^2";

        // Ratings should concentrate around observed User/Joke averages.
        rules[1] = "1: USER(U) & JOKE(J) & AVGUSERRATINGOBS(U) >> RATING(U, J) ^2";
        rules[2] = "1: USER(U) & AVGJOKERATINGOBS(J) & JOKE(J) >> RATING(U, J) ^2";
        rules[3] = "1: USER(U) & RATING(U, J) & JOKE(J) >> AVGUSERRATINGOBS(U) ^2";
        rules[4] = "1: USER(U) & RATING(U, J) & JOKE(J) >> AVGJOKERATINGOBS(J) ^2";

        // Two-sided prior.
        rules[5] = "1: USER(U) & RATINGPRIOR('0') & JOKE(J) >> RATING(U, J) ^2";
        rules[6] = "1: RATING(U, J) >> RATINGPRIOR('0') ^2";

        // Negative Prior
        rules[7] = "1: ~RATING(U, J) ^2";

        return rules;
    }

    @Override
    public HashMap<String, ConstantType[]> getSettingPredicates() {
        HashMap<String, ConstantType[]> predicates = new HashMap<>();

        predicates.put("AvgJokeRatingObs", new ConstantType[] {ConstantType.UniqueStringID});
        predicates.put("AvgUserRatingObs", new ConstantType[] {ConstantType.UniqueStringID});
        predicates.put("Joke", new ConstantType[] {ConstantType.UniqueStringID});
        predicates.put("RatingPrior", new ConstantType[] {ConstantType.UniqueStringID});
        predicates.put("User", new ConstantType[] {ConstantType.UniqueStringID});
        predicates.put("SimObsRating", new ConstantType[] {ConstantType.UniqueStringID,
                ConstantType.UniqueStringID});
        predicates.put("Rating", new ConstantType[] {ConstantType.UniqueStringID, ConstantType.UniqueStringID});

        return predicates;
    }

    @Override
    public HashMap<String, String> getObservedPredicateData() {
        HashMap<String, String> ObservedPredicateData = new HashMap<>();
        ObservedPredicateData.put("AvgJokeRatingObs", "avgJokeRatingObs");
        ObservedPredicateData.put("AvgUserRatingObs", "avgUserRatingObs");
        ObservedPredicateData.put("Joke", "joke");
        ObservedPredicateData.put("RatingPrior", "ratingPrior");
        ObservedPredicateData.put("User", "user");
        ObservedPredicateData.put("SimObsRating", "simObsRating");
        ObservedPredicateData.put("Rating", "rating");
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
        String[] closedPredicateNames = new String[6];

        closedPredicateNames[0] = "AvgJokeRatingObs";
        closedPredicateNames[1] = "AvgUserRatingObs";
        closedPredicateNames[2] = "Joke";
        closedPredicateNames[3] = "RatingPrior";
        closedPredicateNames[4] = "User";
        closedPredicateNames[5] = "SimObsRating";

        return closedPredicateNames;
    }

    @Override
    public String[] getBlockingPredicateNames() {
        return new String[0];
    }
}
