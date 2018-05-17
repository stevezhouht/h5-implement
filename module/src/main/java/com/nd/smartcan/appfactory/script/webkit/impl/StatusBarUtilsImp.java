package com.nd.smartcan.appfactory.script.webkit.impl;

import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.nd.sdp.android.apf.h5.R;
import com.nd.sdp.android.common.res.utils.CommonSkinUtils;
import com.nd.smartcan.appfactory.utils.IStatusBarUtils;
import com.nd.smartcan.appfactory.utils.StatusBarUtils;

/**
 * Created by Administrator on 2018/2/22 0022.
 */

public class StatusBarUtilsImp implements IStatusBarUtils {
    @Override
    public void setDefaultWindowStatusBarColor(Activity activity) {
        StatusBarUtils.setWindowStatusBarColor(activity, R.color.navigation_status_bar_color);
    }

    @Override
    public void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (activity != null
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(CommonSkinUtils.getColor(activity, colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
