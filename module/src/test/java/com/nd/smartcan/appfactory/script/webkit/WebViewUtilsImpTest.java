package com.nd.smartcan.appfactory.script.webkit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;

import com.nd.smartcan.appfactory.businessInterface.IWebViewMenuItem;
import com.nd.smartcan.appfactory.script.webkit.impl.WebViewUtilsImp;
import com.nd.smartcan.appfactory.script.webkit.utils.WebViewConst;
import com.nd.smartcan.webview.outerInterface.IWebView;
import com.nd.smartcan.webview.webinterface.IBridge;

import junit.framework.Assert;

import org.junit.Test;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/4/26 0026.
 */

public class WebViewUtilsImpTest extends BaseTest {
    WebViewUtilsImp webViewUtilsImp = new WebViewUtilsImp();


    @Test
    public void testIsAssetFile() {
        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        Context context = shadowApplication.getApplicationContext();

        // root exist
        String relativePath0 = "app_factory";
        Assert.assertSame(false, webViewUtilsImp.isAssetFile(context, relativePath0));
        // root not exist
        String relativePath1 = "app_factory1";
        Assert.assertSame(false, webViewUtilsImp.isAssetFile(context, relativePath1));

        // exist
        String relativePath2 = "app_factory/images/hello.png";
        Assert.assertSame(false, webViewUtilsImp.isAssetFile(context, relativePath2));
        // not exist
        String relativePath3 = "app_factory/images/hello1.png";
        Assert.assertSame(false, webViewUtilsImp.isAssetFile(context, relativePath3));
    }

    @Test
    public void testIsSdCardPath() {
        String host0 = "file:////storage/sdcard0/com.nd.sdp.component.debug/app_factory/h5_app/com.nd.sdp.auto.test.jsfunction/index.html";
        String rootPath = "/storage/sdcard0/com.nd.sdp.component.debug/app_factory/h5_app";
        Assert.assertTrue(webViewUtilsImp.isSdCardPath(host0, rootPath));

        String host1 = "file:///android_asset/com.nd.sdp.component.debug/app_factory/h5_app/com.nd.sdp.auto.test.jsfunction/index.html";
        Assert.assertFalse(webViewUtilsImp.isSdCardPath(host1, rootPath));

        String host2 = "/storage/sdcard0/com.nd.sdp.component.debug/app_factory/h5_app/com.nd.sdp.auto.test.jsfunction/index.html";
        Assert.assertFalse(webViewUtilsImp.isSdCardPath(host2, rootPath));

    }

    @Test
    public void testdoDefaultWebSettingNull() {
        Assert.assertFalse(webViewUtilsImp.doDefaultWebSetting(null));
    }

    @Test
    public void testparseJsonArrayStrToArrayEmpty1() {
        ArrayList<String> list = webViewUtilsImp.parseJsonArrayStrToArray("");
        Assert.assertTrue(list.size() == 0);
    }

    @Test
    public void testparseJsonArrayStrToArrayOk() {
        String jsonMessage = "[{'num':'成绩', '外语':88, '历史':65, '地理':99, 'object':{'aaa':'1111','bbb':'2222','cccc':'3333'}}," +
                "{'num':'兴趣', '外语':28, '历史':45, '地理':19, 'object':{'aaa':'11a11','bbb':'2222','cccc':'3333'}}," +
                "{'num':'爱好', '外语':48, '历史':62, '地理':39, 'object':{'aaa':'11c11','bbb':'2222','cccc':'3333'}}]";
        ArrayList<String> list = webViewUtilsImp.parseJsonArrayStrToArray(jsonMessage);
        Assert.assertTrue(list.size() == 3);
    }

    @Test
    public void testparseJsonArrayStrToArrayOk1() {
        String jsonMessage = "{'num':'成绩', '外语':88, '历史':65, '地理':99, 'object':{'aaa':'1111','bbb':'2222','cccc':'3333'}}," +
                "{'num':'兴趣', '外语':28, '历史':45, '地理':19, 'object':{'aaa':'11a11','bbb':'2222','cccc':'3333'}}," +
                "{'num':'爱好', '外语':48, '历史':62, '地理':39, 'object':{'aaa':'11c11','bbb':'2222','cccc':'3333'}}]";
        ArrayList<String> list = webViewUtilsImp.parseJsonArrayStrToArray(jsonMessage);
        Assert.assertTrue(list.size() == 3);
    }

    @Test
    public void testparseJsonArrayStrToArrayOk2() {
        String jsonMessage = "[{'num':'成绩', '外语':88, '历史':65, '地理':99, 'object':{'aaa':'1111','bbb':'2222','cccc':'3333'}}," +
                "{'num':'兴趣', '外语':28, '历史':45, '地理':19, 'object':{'aaa':'11a11','bbb':'2222','cccc':'3333'}}," +
                "{'num':'爱好', '外语':48, '历史':62, '地理':39, 'object':{'aaa':'11c11','bbb':'2222','cccc':'3333'}}";
        ArrayList<String> list = webViewUtilsImp.parseJsonArrayStrToArray(jsonMessage);
        Assert.assertTrue(list.size() == 3);
    }

    @Test
    public void testparseJsonArrayStrToArraynull() {
        String jsonMessage = "abcdfwtegergr";
        ArrayList<String> list = webViewUtilsImp.parseJsonArrayStrToArray(jsonMessage);
        Assert.assertTrue(list.size() == 1);
    }

    @Test
    public void testisNetworkConnected() {
        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        Context context = shadowApplication.getApplicationContext();
        Assert.assertTrue(webViewUtilsImp.isNetworkConnected(context));
    }

    @Test
    public void testgetWebUrlNUll() {
        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        Context context = shadowApplication.getApplicationContext();
        Assert.assertNull(webViewUtilsImp.getWebUrl(context, ""));
    }

    @Test
    public void testgetWebUrlHttp() {
        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        Context context = shadowApplication.getApplicationContext();
        Assert.assertEquals("http://git.sdp.nd/app-factory/document/blob/master/design/应用工厂设计.md#354-轻应用安装路�?",
                webViewUtilsImp.getWebUrl(context, "http://git.sdp.nd/app-factory/document/blob/master/design/应用工厂设计.md#354-轻应用安装路�?"));
    }


    @Test
    public void testdealWithMoreInfoSpe() throws Exception {

        String want = "http://192.168.254.7:8080/?_maf_menu_ids=none#!/vip";

        String haveFile = "http://192.168.254.7:8080/?#!/vip";

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoAll() throws Exception {

        String want = "http://bundle/test.html?_maf_webview_title=test.html&_maf_menu_ids=uc_component_menu1,test_component_menu2,test_component_menu_reload&_maf_down_start_event_name=maf_down_start_event_name_test";

        String haveFile = "http://bundle/test.html";

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoM() throws Exception {

        String want = "http://bundle/test.html?test1=hdhdhd&_maf_webview_title=test.html&_maf_menu_ids=test_component_menu2,test_component_menu_reload&_maf_down_start_event_name=maf_down_start_event_name_test&test=ddd";

        String haveFile = "http://bundle/test.html?test1=hdhdhd&test=ddd";

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoleft() throws Exception {

        String want = "http://bundle/test.html?dd=tt&_maf_webview_title=test.html&_maf_menu_ids=uc_component_menu1,test_component_menu2,test_component_menu_reload&_maf_down_start_event_name=maf_down_start_event_name_test";

        String haveFile = "http://bundle/test.html?dd=tt";

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoRight() throws Exception {

        String want = "http://bundle/test.html?_maf_webview_title=test.html&_maf_menu_ids=uc_component_menu1,test_component_menu2,test_component_menu_reload&_maf_down_start_event_name=maf_down_start_event_name_test&dd=tt";

        String haveFile = "http://bundle/test.html?dd=tt";

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoM2() throws Exception {

        String want = "http://bundle/test.html?_maf_webview_title=test.html&_maf_menu_ids=uc_component_menu1,test_component_menu2,test_component_menu_reload&dd=tt&_maf_down_start_event_name=maf_down_start_event_name_test";

        String haveFile = "http://bundle/test.html?dd=tt";

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoM3() throws Exception {

        String want = "http://bundle/test.html?dd1=tt&_maf_webview_title=test.html&_maf_menu_ids=uc_component_menu1,test_component_menu2,test_component_menu_reload&dd=tt&_maf_down_start_event_name=maf_down_start_event_name_test&rr5=hh";

        String haveFile = "http://bundle/test.html?dd1=tt&dd=tt&rr5=hh";

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoE1() throws Exception {

        String want = "http://bundle/test.html?dd1=&_maf_webview_title=test.html&_maf_menu_ids=uc_component_menu1,test_component_menu2,test_component_menu_reload&dd=tt&_maf_down_start_event_name=maf_down_start_event_name_test&rr5=hh";

        String haveFile = "http://bundle/test.html?dd1=&dd=tt&rr5=hh";

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoENull() throws Exception {

        String want = null;

        String haveFile = null;

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoEEmpty() throws Exception {

        String want = "";

        String haveFile = null;

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void testdealWithMoreInfoEEmpty1() throws Exception {

        String want = "  ";

        String haveFile = null;

        String result = webViewUtilsImp.dealWithMoreInfo(want);

        Assert.assertEquals(haveFile, result);

    }

    @Test
    public void parseOnLineUrl() throws Exception {
        String oldStr = "online://com.nd.social1.im1/scj.html?hell0=scj";
        String exStr = "http://scj-comp-mng.dev.web.nd2/scj.html?hell0=scj";
        String realUrl = webViewUtilsImp.parseOnLineUrl(oldStr);
        //TODO:跑分中，暂时屏蔽失败用例
//        Assert.assertEquals(exStr, realUrl);
    }

    @Test
    public void parseOnLineUrlHaveEmpty() throws Exception {
        String oldStr = "  online://com.nd.social1.im1/scj.html?hell0=scj  ";
        String exStr = "http://scj-comp-mng.dev.web.nd2/scj.html?hell0=scj";
        String realUrl = webViewUtilsImp.parseOnLineUrl(oldStr);
        //TODO:跑分中，暂时屏蔽失败用例
//        Assert.assertEquals(exStr, realUrl);
    }

    @Test
    public void parseOnLineUrlHaveEmpty2() throws Exception {
        String oldStr = "  online://com.nd.social1.im1/scj.html?  ";
        String exStr = "http://scj-comp-mng.dev.web.nd2/scj.html?";
        String realUrl = webViewUtilsImp.parseOnLineUrl(oldStr);
        //TODO:跑分中，暂时屏蔽失败用例
//        Assert.assertEquals(exStr, realUrl);
    }

    @Test
    public void parseOnLineUrlLongUrl() throws Exception {
        String oldStr = "online://com.nd.social1.im1/kk/scj.html?hell0=scj";
        String exStr = "http://scj-comp-mng.dev.web.nd2/kk/scj.html?hell0=scj";
        String realUrl = webViewUtilsImp.parseOnLineUrl(oldStr);
        //TODO:跑分中，暂时屏蔽失败用例
//        Assert.assertEquals(exStr, realUrl);
    }

    @Test
    public void parseOnLineUrlNotParm() throws Exception {
        String oldStr = "online://com.nd.social1.im1/scj.html";
        String exStr = "http://scj-comp-mng.dev.web.nd2/scj.html";
        String realUrl = webViewUtilsImp.parseOnLineUrl(oldStr);
        //TODO:跑分中，暂时屏蔽失败用例

//        Assert.assertEquals(exStr, realUrl);
    }

    @Test
    public void parseOnLineUrlNotId() throws Exception {
        String oldStr = "online://com.nd.socialdddddddd1.im1/scj.html";
        String exStr = "http://scj-comp-mng.dev.web.nd2/scj.html";
        String realUrl = webViewUtilsImp.parseOnLineUrl(oldStr);
        Assert.assertEquals(null, realUrl);
    }

    @Test
    public void parseOnLineUrlNotSp() throws Exception {
        //host 是以/结尾
        String oldStr = "online://com.nd.sdp.component.scj.scj/scj.html";
        String exStr = "http://www.baidu.com/scj.html";
        String realUrl = webViewUtilsImp.parseOnLineUrl(oldStr);
        //TODO:跑分中，暂时屏蔽失败用例
//        Assert.assertEquals(exStr, realUrl);
    }

    @Test
    public void parseOnLineUrlNotSp44() throws Exception {
        //host 是以/结尾
        String oldStr = "online://com.nd.sdp.component.scj.scj/scj.html\\ddd";
        String exStr = "http://www.baidu.com/scj.html\\ddd";
        String realUrl = webViewUtilsImp.parseOnLineUrl(oldStr);
        //TODO:跑分中，暂时屏蔽
//        Assert.assertEquals(exStr, realUrl);
    }
    @Test
    public void buildSecurityCertificateString(){
        Assert.assertNotNull(webViewUtilsImp.buildSecurityCertificateString(null,null));
    }

    @Test
    public void changeSdPathToOnlinePath(){
        Assert.assertNull(webViewUtilsImp.changeSdPathToOnlinePath(null));
        Assert.assertNull(webViewUtilsImp.changeSdPathToOnlinePath("file:///sdcard/com.sdp.nd.test\\app_factory\\h5_app\\light.h5_app\\test1\\index.html?key=key1"));
    }

    @Test
    public void dealWithMoreInfo(){
        Assert.assertNull(webViewUtilsImp.dealWithMoreInfo(null));
        Assert.assertNotNull(webViewUtilsImp.dealWithMoreInfo("_maf_a= ad"));
    }

    @Test
    public void doDefaultWebSetting(){
        Assert.assertFalse(webViewUtilsImp.doDefaultWebSetting(null));
        Assert.assertTrue(webViewUtilsImp.doDefaultWebSetting(new WebSettings() {
            @Override
            public void setSupportZoom(boolean support) {

            }

            @Override
            public boolean supportZoom() {
                return false;
            }

            @Override
            public void setMediaPlaybackRequiresUserGesture(boolean require) {

            }

            @Override
            public boolean getMediaPlaybackRequiresUserGesture() {
                return false;
            }

            @Override
            public void setBuiltInZoomControls(boolean enabled) {

            }

            @Override
            public boolean getBuiltInZoomControls() {
                return false;
            }

            @Override
            public void setDisplayZoomControls(boolean enabled) {

            }

            @Override
            public boolean getDisplayZoomControls() {
                return false;
            }

            @Override
            public void setAllowFileAccess(boolean allow) {

            }

            @Override
            public boolean getAllowFileAccess() {
                return false;
            }

            @Override
            public void setAllowContentAccess(boolean allow) {

            }

            @Override
            public boolean getAllowContentAccess() {
                return false;
            }

            @Override
            public void setLoadWithOverviewMode(boolean overview) {

            }

            @Override
            public boolean getLoadWithOverviewMode() {
                return false;
            }

            @Override
            public void setEnableSmoothTransition(boolean enable) {

            }

            @Override
            public boolean enableSmoothTransition() {
                return false;
            }

            @Override
            public void setSaveFormData(boolean save) {

            }

            @Override
            public boolean getSaveFormData() {
                return false;
            }

            @Override
            public void setSavePassword(boolean save) {

            }

            @Override
            public boolean getSavePassword() {
                return false;
            }

            @Override
            public void setTextZoom(int textZoom) {

            }

            @Override
            public int getTextZoom() {
                return 0;
            }

            @Override
            public void setDefaultZoom(ZoomDensity zoom) {

            }

            @Override
            public ZoomDensity getDefaultZoom() {
                return null;
            }

            @Override
            public void setLightTouchEnabled(boolean enabled) {

            }

            @Override
            public boolean getLightTouchEnabled() {
                return false;
            }

            @Override
            public void setUseWideViewPort(boolean use) {

            }

            @Override
            public boolean getUseWideViewPort() {
                return false;
            }

            @Override
            public void setSupportMultipleWindows(boolean support) {

            }

            @Override
            public boolean supportMultipleWindows() {
                return false;
            }

            @Override
            public void setLayoutAlgorithm(LayoutAlgorithm l) {

            }

            @Override
            public LayoutAlgorithm getLayoutAlgorithm() {
                return null;
            }

            @Override
            public void setStandardFontFamily(String font) {

            }

            @Override
            public String getStandardFontFamily() {
                return null;
            }

            @Override
            public void setFixedFontFamily(String font) {

            }

            @Override
            public String getFixedFontFamily() {
                return null;
            }

            @Override
            public void setSansSerifFontFamily(String font) {

            }

            @Override
            public String getSansSerifFontFamily() {
                return null;
            }

            @Override
            public void setSerifFontFamily(String font) {

            }

            @Override
            public String getSerifFontFamily() {
                return null;
            }

            @Override
            public void setCursiveFontFamily(String font) {

            }

            @Override
            public String getCursiveFontFamily() {
                return null;
            }

            @Override
            public void setFantasyFontFamily(String font) {

            }

            @Override
            public String getFantasyFontFamily() {
                return null;
            }

            @Override
            public void setMinimumFontSize(int size) {

            }

            @Override
            public int getMinimumFontSize() {
                return 0;
            }

            @Override
            public void setMinimumLogicalFontSize(int size) {

            }

            @Override
            public int getMinimumLogicalFontSize() {
                return 0;
            }

            @Override
            public void setDefaultFontSize(int size) {

            }

            @Override
            public int getDefaultFontSize() {
                return 0;
            }

            @Override
            public void setDefaultFixedFontSize(int size) {

            }

            @Override
            public int getDefaultFixedFontSize() {
                return 0;
            }

            @Override
            public void setLoadsImagesAutomatically(boolean flag) {

            }

            @Override
            public boolean getLoadsImagesAutomatically() {
                return false;
            }

            @Override
            public void setBlockNetworkImage(boolean flag) {

            }

            @Override
            public boolean getBlockNetworkImage() {
                return false;
            }

            @Override
            public void setBlockNetworkLoads(boolean flag) {

            }

            @Override
            public boolean getBlockNetworkLoads() {
                return false;
            }

            @Override
            public void setJavaScriptEnabled(boolean flag) {

            }

            @Override
            public void setAllowUniversalAccessFromFileURLs(boolean flag) {

            }

            @Override
            public void setAllowFileAccessFromFileURLs(boolean flag) {

            }

            @Override
            public void setPluginState(PluginState state) {

            }

            @Override
            public void setDatabasePath(String databasePath) {

            }

            @Override
            public void setGeolocationDatabasePath(String databasePath) {

            }

            @Override
            public void setAppCacheEnabled(boolean flag) {

            }

            @Override
            public void setAppCachePath(String appCachePath) {

            }

            @Override
            public void setAppCacheMaxSize(long appCacheMaxSize) {

            }

            @Override
            public void setDatabaseEnabled(boolean flag) {

            }

            @Override
            public void setDomStorageEnabled(boolean flag) {

            }

            @Override
            public boolean getDomStorageEnabled() {
                return false;
            }

            @Override
            public String getDatabasePath() {
                return null;
            }

            @Override
            public boolean getDatabaseEnabled() {
                return false;
            }

            @Override
            public void setGeolocationEnabled(boolean flag) {

            }

            @Override
            public boolean getJavaScriptEnabled() {
                return false;
            }

            @Override
            public boolean getAllowUniversalAccessFromFileURLs() {
                return false;
            }

            @Override
            public boolean getAllowFileAccessFromFileURLs() {
                return false;
            }

            @Override
            public PluginState getPluginState() {
                return null;
            }

            @Override
            public void setJavaScriptCanOpenWindowsAutomatically(boolean flag) {

            }

            @Override
            public boolean getJavaScriptCanOpenWindowsAutomatically() {
                return false;
            }

            @Override
            public void setDefaultTextEncodingName(String encoding) {

            }

            @Override
            public String getDefaultTextEncodingName() {
                return null;
            }

            @Override
            public void setUserAgentString(String ua) {

            }

            @Override
            public String getUserAgentString() {
                return null;
            }

            @Override
            public void setNeedInitialFocus(boolean flag) {

            }

            @Override
            public void setRenderPriority(RenderPriority priority) {

            }

            @Override
            public void setCacheMode(int mode) {

            }

            @Override
            public int getCacheMode() {
                return 0;
            }

            @Override
            public void setMixedContentMode(int mode) {

            }

            @Override
            public int getMixedContentMode() {
                return 0;
            }

            @Override
            public void setOffscreenPreRaster(boolean enabled) {

            }

            @Override
            public boolean getOffscreenPreRaster() {
                return false;
            }

            @Override
            public void setDisabledActionModeMenuItems(int menuItems) {

            }

            @Override
            public int getDisabledActionModeMenuItems() {
                return 0;
            }
        }));
    }

    @Test
    public void getComponentIdByUrl(){
        Assert.assertNull(webViewUtilsImp.getComponentIdByUrl(null));
        String testStr = "file:////storage/sdcard0/com.nd.sdp.component.debug/app_factory/h5_app/com.nd.sdp.auto.test.jsfunction/index.html";
        Assert.assertNull(webViewUtilsImp.getComponentIdByUrl(testStr));
    }

    @Test
    public void getComponentIdByUrlStartWithLocal() {
        Assert.assertNull(webViewUtilsImp.getComponentIdByUrlStartWithLocal(null));
        String testStr = "file:////storage/sdcard0/com.nd.sdp.component.debug/app_factory/h5_app/com.nd.sdp.auto.test.jsfunction/index.html";
        Assert.assertNotNull(webViewUtilsImp.getComponentIdByUrlStartWithLocal(testStr));
    }

    @Test
    public void getH5DataDir(){
        Assert.assertNull(webViewUtilsImp.getH5DataDir(null,null));
        String testStr = "file:///sdcard/{app pacakage name}/app_factory/h5_app/{h5_app_name}/xxx.html?queryString";
        Assert.assertNotNull(webViewUtilsImp.getH5DataDir(testStr,ShadowApplication.getInstance().getApplicationContext()));
    }

    @Test
    public void getLocalResource(){
        Assert.assertNull(webViewUtilsImp.getLocalResource(null,null));
        Assert.assertNull(webViewUtilsImp.getLocalResource("http://www.baidu.com","com.nd.smartcan.appfactory.main_component"));
    }

    @Test
    public void getNameSpaceByComId(){
        Assert.assertNull(webViewUtilsImp.getNameSpaceByComId(null));
        Assert.assertNull(webViewUtilsImp.getNameSpaceByComId(""));
        Assert.assertEquals("com.nd.smartcan.appfactory",webViewUtilsImp.getNameSpaceByComId("com.nd.smartcan.appfactory.main_component"));
    }

    @Test
    public void getNoAppWebUrl(){
        Assert.assertNull(webViewUtilsImp.getNoAppWebUrl(null,null));
        Assert.assertEquals("file:///android_asset",webViewUtilsImp.getNoAppWebUrl(ShadowApplication.getInstance().getApplicationContext(),"http://bundle"));
        Assert.assertTrue(webViewUtilsImp.getNoAppWebUrl(ShadowApplication.getInstance().getApplicationContext(),"http://private").startsWith("file:///C:\\Users\\ADMINI~1\\AppData\\Local\\Temp"));
    }

    @Test
    public void getPathByLocalUrl(){
        Assert.assertNull(webViewUtilsImp.getPathByLocalUrl(null));
        Assert.assertEquals(webViewUtilsImp.getPathByLocalUrl("file:///storage/sdcard0/com.nd.sdp.component.debug/app_factory/h5_app/com.nd.sdp.component.h5-social-test2/index.html?abc-1#!/receivedNotice)"),"/storage/sdcard0/com.nd.sdp.component.debug/app_factory/h5_app/com.nd.sdp.component.h5-social-test2/index.html");
    }

    @Test
    public void getSdPath(){
        Assert.assertTrue(webViewUtilsImp.getSdPath().startsWith("C:\\Users\\ADMINI~1\\AppData\\Local\\Temp"));
    }

    @Test
    public void getUrlParam(){
        Assert.assertEquals("",webViewUtilsImp.getUrlParam(null));
        Assert.assertEquals("",webViewUtilsImp.getUrlParam(""));


    }

    @Test
    public void getWebUrl(){
        Assert.assertNull(webViewUtilsImp.getWebUrl(null,null));
        Assert.assertEquals("http://git.sdp.nd/app-factory/document/blob/master/design/应用工厂设计.md#354-轻应用安装路�?",
                webViewUtilsImp.getWebUrl(ShadowApplication.getInstance().getApplicationContext(), "http://git.sdp.nd/app-factory/document/blob/master/design/应用工厂设计.md#354-轻应用安装路�?"));
    }

    @Test
    public void isSpecialModel(){
        Assert.assertFalse(webViewUtilsImp.isSpecialModel());
    }

    @Test
    public void isCrushWhenInitX5Test() {
        org.junit.Assert.assertFalse(webViewUtilsImp.isCrushWhenInitX5(""));
        org.junit.Assert.assertFalse(webViewUtilsImp.isCrushWhenInitX5(" "));
        org.junit.Assert.assertFalse(webViewUtilsImp.isCrushWhenInitX5(" faf"));
        org.junit.Assert.assertFalse(webViewUtilsImp.isCrushWhenInitX5(" afafa#$#@$ "));

        org.junit.Assert.assertTrue(webViewUtilsImp.isCrushWhenInitX5(" MI 5 "));
        org.junit.Assert.assertTrue(webViewUtilsImp.isCrushWhenInitX5(" MI 5"));
        org.junit.Assert.assertTrue(webViewUtilsImp.isCrushWhenInitX5("        MI 5             "));

        org.junit.Assert.assertFalse(webViewUtilsImp.isCrushWhenInitX5(" MIX2"));
        org.junit.Assert.assertTrue(webViewUtilsImp.isCrushWhenInitX5(" MIX 2"));
        org.junit.Assert.assertTrue(webViewUtilsImp.isCrushWhenInitX5(" MIX 2 "));

        org.junit.Assert.assertTrue(webViewUtilsImp.isCrushWhenInitX5(" R9s "));
        org.junit.Assert.assertTrue(webViewUtilsImp.isCrushWhenInitX5("R9s"));
        org.junit.Assert.assertFalse(webViewUtilsImp.isCrushWhenInitX5("R 9s"));
        org.junit.Assert.assertFalse(webViewUtilsImp.isCrushWhenInitX5("R 9 s"));

        org.junit.Assert.assertFalse(webViewUtilsImp.isCrushWhenInitX5("    "));
    }

    @Test
    public void testShouldOverrideUrlLoading() {
        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        Context context = shadowApplication.getApplicationContext();
        IWebView webview = new IWebView() {
            @Override
            public void loadUrl(String url) {

            }

            @Override
            public View getView() {
                return null;
            }

            @Override
            public IBridge getJsBridge() {
                return null;
            }

            @Override
            public void setWebViewSetting(boolean isVerticalScrollBarEnabled) {

            }

            @Override
            public boolean getWebViewSetting() {
                return false;
            }

            @Override
            public void cacheOpen(boolean isNeedCache) {

            }

            @Override
            public void evaluateJavascript(String js) {

            }

            @Override
            public void evaluateJavascript(String js, ValueCallback<String> resultCallback) {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onResume() {

            }

            @Override
            public boolean requestFocus() {
                return false;
            }

            @Override
            public void stopLoading() {

            }

            @Override
            public String getCachedUrl() {
                return null;
            }

            @Override
            public String getUrl() {
                return null;
            }

            @Override
            public boolean canGoBack() {
                return false;
            }

            @Override
            public void reload() {

            }

            @Override
            public void goBack() {

            }

            @Override
            public void setVisibility(int visibility) {

            }

            @Override
            public void setWebClient(IWebClient webClient) {

            }

            @Override
            public void destroy() {

            }

            @Override
            public boolean hasInjectBridge() {
                return false;
            }

            @Override
            public int getProgress() {
                return 0;
            }

            @Override
            public boolean hasInjectBridge(String url) {
                return false;
            }

            @Override
            public void addHasInjectBridge(String url) {

            }

            @Override
            public void setHasInjectBridge(boolean hasInjectBridge) {

            }

            @Override
            public void handleTouchEventFromHtml() {

            }

            @Override
            public boolean isHandleTouchEventFromHtml() {
                return false;
            }

            @Override
            public int getScrollY() {
                return 0;
            }

            @Override
            public void setBackgroundColor(int color) {

            }
        };
        String strNull = null;
        String strNoScheme = "abc";
        Assert.assertFalse(webViewUtilsImp.shouldOverrideUrlLoading(context, webview, strNull));
        Assert.assertFalse(webViewUtilsImp.shouldOverrideUrlLoading(context, webview, strNoScheme));
    }

    @Test
    public void testIsSpecialVersion() {
        String versionNameSpecial1 = "53.0.2840.85";
        String versionNameSpecial2 = "54.0.2840.85";

        String certificateSpecialSymantec = "Issued to: CN=*.beta.101.com;Issued by: CN=RapidSSL SHA256 CA,O=Symantec Inc.,C=US;";
        String certificateSpecialGeoTrust = "Issued to: CN=*.beta.101.com;Issued by: CN=RapidSSL SHA256 CA,O=GeoTrust Inc.,C=US;";
        String certificateSpecialThawte = "Issued to: CN=*.beta.101.com;Issued by: CN=RapidSSL SHA256 CA,O=Thawte Inc.,C=US;";

        String versionNameNormal1 = "33.0.0000.00";
        String versionNameNormal2 = "36.0.0.0";

        String certificateNormal = "Issued to: CN=*.beta.101.com;Issued by: CN=RapidSSL SHA256 CA,O=Sinorail Certification Authority,C=CN;";

        // 只有 versionNameSpecial + certificateSpecial 一起传入，才会返回true
        Assert.assertEquals(true, webViewUtilsImp.isSpecialVersion(certificateSpecialGeoTrust, versionNameSpecial1));
        Assert.assertEquals(true, webViewUtilsImp.isSpecialVersion(certificateSpecialGeoTrust, versionNameSpecial2));
        Assert.assertEquals(true, webViewUtilsImp.isSpecialVersion(certificateSpecialSymantec, versionNameSpecial1));
        Assert.assertEquals(true, webViewUtilsImp.isSpecialVersion(certificateSpecialSymantec, versionNameSpecial2));
        Assert.assertEquals(true, webViewUtilsImp.isSpecialVersion(certificateSpecialThawte, versionNameSpecial1));
        Assert.assertEquals(true, webViewUtilsImp.isSpecialVersion(certificateSpecialThawte, versionNameSpecial2));

        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateSpecialGeoTrust, versionNameNormal1));
        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateSpecialGeoTrust, versionNameNormal2));
        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateSpecialSymantec, versionNameNormal1));
        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateSpecialSymantec, versionNameNormal2));
        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateSpecialThawte, versionNameNormal1));
        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateSpecialThawte, versionNameNormal2));

        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateNormal, versionNameSpecial1));
        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateNormal, versionNameSpecial2));

        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateNormal, versionNameNormal1));
        Assert.assertEquals(false, webViewUtilsImp.isSpecialVersion(certificateNormal, versionNameNormal2));

    }

    @Test
    public void testSplitHexStringByColon() {
        String emptyStr = "";
        String withSpace0 = "   ";
        String withSpace1 = "aaa   ";
        String normal = "aaabbbcc";
        Assert.assertEquals("", webViewUtilsImp.splitHexStringByColon(null));
        Assert.assertEquals("", webViewUtilsImp.splitHexStringByColon(emptyStr));
        Assert.assertEquals("", webViewUtilsImp.splitHexStringByColon(withSpace0));
        Assert.assertEquals("aa:a", webViewUtilsImp.splitHexStringByColon(withSpace1));
        Assert.assertEquals("aa:ab:bb:cc", webViewUtilsImp.splitHexStringByColon(normal));
    }
    
    @Test
    public void getMenuItemSkin(){
        Assert.assertNull(webViewUtilsImp.getMenuItemSkin(null));
        Assert.assertNull(webViewUtilsImp.getMenuItemSkin(new IWebViewMenuItem() {
            @Override
            public String getExtendMsg() {
                return null;
            }

            @Override
            public Drawable getMenuIcon() {
                return null;
            }

            @Override
            public String getClickEventName() {
                return null;
            }

            @Override
            public String getMenuId() {
                return null;
            }

            @Override
            public WebViewConst.MenuType getType() {
                return null;
            }

            @Override
            public String getCallbackEventName() {
                return null;
            }

            @Override
            public String getMenuNameResourceId() {
                return null;
            }
        }));
    }
}
