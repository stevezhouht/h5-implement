package com.nd.smartcan.appfactory.script.webkit;

import com.nd.smartcan.appfactory.script.webkit.impl.StatusBarUtilsImp;

import org.junit.Test;
import org.robolectric.Robolectric;

/**
 * Created by Administrator on 2018/4/25 0025.
 */

public class StatusBarUtilsImpTest extends BaseTest {
    StatusBarUtilsImp statusBarUtilsImp = new StatusBarUtilsImp();
    @Test
    public void setDefaultWindowStatusBarColor(){
        TestActivity testActivity = Robolectric.setupActivity(TestActivity.class);
        statusBarUtilsImp.setDefaultWindowStatusBarColor(testActivity);
    }

    @Test
    public void setWindowStatusBarColor(){
        TestActivity testActivity = Robolectric.setupActivity(TestActivity.class);
        statusBarUtilsImp.setWindowStatusBarColor(testActivity,0);
    }
}
