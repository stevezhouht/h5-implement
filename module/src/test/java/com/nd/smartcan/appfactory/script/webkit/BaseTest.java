package com.nd.smartcan.appfactory.script.webkit;

import com.nd.smartcan.appfactory.BuildConfig;
import com.nd.smartcan.appfactory.component.ComponentEntry;
import com.nd.smartcan.appfactory.component.HandlerEventInfo;
import com.nd.smartcan.mockbase.MockBaseTest;

import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/2/26 0026.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,sdk=21,manifest=Config.NONE)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BaseTest extends MockBaseTest {
    @Override
    public String setLanuage() {
        return null;
    }

    @Override
    public ComponentEntry addComponentEntry() {
        return null;
    }

    @Override
    public List<ComponentEntry> setComponentEntryList() {
        List<ComponentEntry> componentEntryList = null;
        ComponentEntry componentEntry = null;
        HashMap<String, String> propMap = null;
        List<HandlerEventInfo> handlerEventList = null;
        HandlerEventInfo info = null;
        componentEntryList = new ArrayList<ComponentEntry>();

        // get property for namespace: com.nd.smartcan.appfactory, name:main_component
        propMap = new HashMap<String, String>();
        propMap.put("updataGapMinute", " ");
        propMap.put("allow_check_update", "true");
        propMap.put("is_open_self_tab", "false");
        propMap.put("tabbar_item_selected_text_color", "");
        propMap.put("tabbar_item_text_color", "");
        propMap.put("tabbar_background_color", "");
        propMap.put("tabbar_background_image_android", "");
        propMap.put("tabbar_background_image_ios", "");
        propMap.put("default_tab_index", "0");
        propMap.put("dont_remind_update_interval", "0");
        propMap.put("is_show_tab", "");
        propMap.put("bonree_app_key_ios", "1ee35f2d-5bf7-494e-8f3c-e151c143aa43");
        propMap.put("bonree_app_key_android", "b66db0e1-42d3-4507-ad7d-58e0e2fe8b59");
        // get handler event for namespace: com.nd.smartcan.appfactory, name:main_component
        handlerEventList = new ArrayList<HandlerEventInfo>();

        componentEntry = new ComponentEntry("com.nd.smartcan.appfactory", "main_component", "com.nd.component.MainComponent", null, null, handlerEventList, propMap);
        componentEntryList.add(componentEntry);
        return componentEntryList;
    }
}
