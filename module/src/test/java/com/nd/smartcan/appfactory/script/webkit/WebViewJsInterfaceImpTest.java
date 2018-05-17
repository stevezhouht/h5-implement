package com.nd.smartcan.appfactory.script.webkit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.nd.smartcan.appfactory.script.webkit.impl.WebViewJsInterfaceImp;
import com.nd.smartcan.frame.js.ActivityResultCallback;
import com.nd.smartcan.frame.js.IActivityProxy;
import com.nd.smartcan.frame.js.IContainerProxy;
import com.nd.smartcan.frame.js.INativeContext;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.robolectric.Robolectric;

import java.util.Map;

/**
 * Created by Administrator on 2018/4/26 0026.
 */

public class WebViewJsInterfaceImpTest extends  BaseTest {
    WebViewJsInterfaceImp webViewJsInterfaceImp = new WebViewJsInterfaceImp();

    @Test
    public void setNavigationBarAppearance(){
        webViewJsInterfaceImp.setNavigationBarAppearance(nativeContext,null);

        JSONObject jsonObject = new JSONObject();
        webViewJsInterfaceImp.setNavigationBarAppearance(nativeContext,jsonObject);

        try {
            jsonObject.put(WebViewJsInterfaceImp.TEXT_COLOR,"0");
            webViewJsInterfaceImp.setNavigationBarAppearance(nativeContext,jsonObject);

            jsonObject.put(WebViewJsInterfaceImp.BACKGROUND_COLOR,"0");
            webViewJsInterfaceImp.setNavigationBarAppearance(nativeContext,jsonObject);

            jsonObject.put(WebViewJsInterfaceImp.DIVIDER_COLOR,"0");
            webViewJsInterfaceImp.setNavigationBarAppearance(nativeContext,jsonObject);

            jsonObject.put(WebViewJsInterfaceImp.STATUSBAR_TEXT_COLOR,"0");
            webViewJsInterfaceImp.setNavigationBarAppearance(nativeContext,jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadUrl(){
        webViewJsInterfaceImp.loadUrl(nativeContext,null);

        JSONObject jsonObject = new JSONObject();
        webViewJsInterfaceImp.loadUrl(nativeContext,jsonObject);

        try {
            jsonObject.put("uuid","0");
            webViewJsInterfaceImp.loadUrl(nativeContext,jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isJsTakeKeyBack(){
        webViewJsInterfaceImp.isJsTakeKeyBack(nativeContext,null);

        JSONObject jsonObject = new JSONObject();
        webViewJsInterfaceImp.isJsTakeKeyBack(nativeContext,jsonObject);

        try {
            jsonObject.put("isJsTakeKeyBack","false");
            webViewJsInterfaceImp.isJsTakeKeyBack(nativeContext,jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void trigerNativeKeyBack(){
        webViewJsInterfaceImp.trigerNativeKeyBack(nativeContext,null);

        JSONObject jsonObject = new JSONObject();
        webViewJsInterfaceImp.isJsTakeKeyBack(nativeContext,jsonObject);

        try {
            jsonObject.put("isJsTakeKeyBack","false");
            webViewJsInterfaceImp.isJsTakeKeyBack(nativeContext,jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    INativeContext nativeContext = new INativeContext() {
        @Override
        public Context getContext() {
            return null;
        }

        @Override
        public IActivityProxy getActivity() {
            return new IActivityProxy() {
                @Override
                public void finish() {

                }

                @Override
                public void openHardwareAccelerate() {

                }

                @Override
                public Context getContext() {
                    return null;
                }

                @Override
                public void startActivityForResult(Intent intent, int i) {

                }

                @Override
                public boolean registerMenu(Map<String, String> map) {
                    return false;
                }

                @Override
                public boolean unRegisterMenu(String s) {
                    return false;
                }

                @Override
                public void setActivityResultCallback(ActivityResultCallback activityResultCallback) {

                }
            };
        }

        @Override
        public IContainerProxy getContainer() {
            return null;
        }

        @Override
        public Object getValue(String s) {
            return null;
        }

        @Override
        public void putContextObjectMap(Map<String, Object> map) {

        }

        @Override
        public void putContextObject(String s, Object o) {

        }

        @Override
        public void success(String s) throws IllegalStateException {

        }

        @Override
        public void success(JSONObject jsonObject) throws IllegalStateException {

        }

        @Override
        public void fail(String s) throws IllegalStateException {

        }

        @Override
        public void fail(JSONObject jsonObject) throws IllegalStateException {

        }

        @Override
        public void callListener(String s) throws IllegalStateException {

        }

        @Override
        public void callListener(JSONObject jsonObject) throws IllegalStateException {

        }

        @Override
        public void notify(String s) throws IllegalStateException {

        }

        @Override
        public void notify(JSONObject jsonObject) throws IllegalStateException {

        }
    };
}
