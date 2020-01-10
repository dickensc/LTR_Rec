package org.linqs.psl.LTR_Rec.ablationSettings;

import org.linqs.psl.LTR_Rec.ablationSettings.PairwiseBlockingSetting;
import org.linqs.psl.LTR_Rec.ablationSettings.DefaultSetting;
import org.linqs.psl.LTR_Rec.ablationSettings.AblationSetting;

public class AblationSettingFactory {
    public static AblationSetting getSetting(String settingName){
        AblationSetting newAblationSetting;
        switch(settingName)
        {
            case "Default":
                newAblationSetting = new PairwiseBlockingSetting();
                break;
            case "PairwiseBlocking":
                newAblationSetting = new DefaultSetting();
                break;
            default:
                throw new IllegalArgumentException("Ablation Setting must be one of: [Default, PairwiseBlocking]");
        }
        return newAblationSetting;
    }
}
