package org.linqs.psl.LTR_Rec.modelSettings;

import org.linqs.psl.LTR_Rec.modelSettings.ModelSetting;
import org.linqs.psl.LTR_Rec.modelSettings.MovieLensSetting;
import org.linqs.psl.LTR_Rec.modelSettings.JesterSetting;
import org.linqs.psl.LTR_Rec.modelSettings.LastFMSetting;
import org.linqs.psl.LTR_Rec.modelSettings.YelpSetting;

public class ModelSettingFactory {
    public static ModelSetting getSetting(String settingName){
        ModelSetting newModelSetting;
        switch(settingName)
        {
            case "movie_lens":
                newModelSetting = new MovieLensSetting();
                break;
            case "jester":
                newModelSetting = new JesterSetting();
                break;
            case "lastfm":
                newModelSetting = new LastFMSetting();
                break;
            case "yelp":
                newModelSetting = new YelpSetting();
                break;
            default:
                throw new IllegalArgumentException("setting must be one of: [movie_lens, jester, lastfm, yelp]");
        }
        return newModelSetting;
    }
}
