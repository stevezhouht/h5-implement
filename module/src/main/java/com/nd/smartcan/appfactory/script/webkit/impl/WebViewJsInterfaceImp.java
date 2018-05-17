package com.nd.smartcan.appfactory.script.webkit.impl;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.nd.smartcan.appfactory.js.AppFactoryJsInterfaceImp;
import com.nd.smartcan.appfactory.js.IWebViewJsInterface;
import com.nd.smartcan.appfactory.script.webkit.WebViewActivity;
import com.nd.smartcan.appfactory.utils.ApfUiThreadUtil;
import com.nd.smartcan.commons.util.logger.Logger;
import com.nd.smartcan.frame.js.INativeContext;

import org.json.JSONException;
import org.json.JSONObject;

import static com.nd.smartcan.appfactory.js.AppFactoryJsInterfaceImp.ERROR_CONTENT_PARA;

/**
 * Created by Administrator on 2018/2/22 0022.
 */

public class WebViewJsInterfaceImp implements IWebViewJsInterface {
    public static final String TEXT_COLOR = "text_color";
    public static final String BACKGROUND_COLOR = "background_color";
    public static final String DIVIDER_COLOR = "divider_color";
    public static final String STATUSBAR_TEXT_COLOR = "statusbar_text_color";
    private final static String UUID = "uuid";
    private final static String IS_JS_TAKE_KEY_BACK = "isJsTakeKeyBack";

    public final static String TAG = WebViewJsInterfaceImp.class.getSimpleName();
    @Override
    public void setNavigationBarAppearance(INativeContext context, JSONObject param) {
        if (context == null || param == null) {
            Logger.w(TAG, ERROR_CONTENT_PARA);
            context.fail(AppFactoryJsInterfaceImp.FAILURE);
            return;
        }
        int colorText = 0;
        int colorBackground = 0;
        int colorDivider = 0;
        try {
            //导航栏上正常态文字的颜色
            String textColor = param.optString(TEXT_COLOR);
            if (!TextUtils.isEmpty(textColor)) {
                colorText = Color.parseColor(textColor);
            }
            //导航栏背景颜色
            String backgroundColor = param.optString(BACKGROUND_COLOR);
            if (!TextUtils.isEmpty(backgroundColor)) {
                colorBackground = Color.parseColor(backgroundColor);
            }
            // divider 颜色
            // WebViewActivity中的引入的Toolbar视图分为两个部分：
            // 1. 上面的一个是原生的toolbar，
            // 2. 底下还有一个1px的白色的divider。
            // 如果对divider有颜色渲染需求，这里提供接口
            String dividerColor = param.optString(DIVIDER_COLOR);
            if (!TextUtils.isEmpty(dividerColor)) {
                colorDivider = Color.parseColor(dividerColor);
            }
            //状态栏（信号、时间条）文本颜色 只对iOS设备有效,暂不处理

            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(TEXT_COLOR, colorText);
            jsonObject.put(BACKGROUND_COLOR, colorBackground);
            jsonObject.put(DIVIDER_COLOR, colorDivider);

            Context realContext = context.getContext();
            if (realContext instanceof WebViewActivity) {
                final WebViewActivity ac = (WebViewActivity) realContext;
                ac.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ac.setNavigationBarAppearance(jsonObject);
                    }
                });


            }
            context.success(AppFactoryJsInterfaceImp.SUCCESS);
        } catch (IllegalArgumentException e) {
            Logger.w(TAG, e.getMessage());
            context.fail(AppFactoryJsInterfaceImp.FAILURE);
        } catch (JSONException e) {
            Logger.w(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void loadUrl(INativeContext context, JSONObject param) {
        if (context == null || param == null) {
            Logger.w(TAG, ERROR_CONTENT_PARA);
            return;
        }

        String uuid = param.optString(UUID);
        if (TextUtils.isEmpty(uuid)) {
            Logger.w(TAG, "参数为空, uuid = " + uuid);
            return;
        }
        WebViewActivity wva = null;
        if (context.getActivity() != null && context.getActivity().getContext() != null && context.getActivity().getContext() instanceof WebViewActivity) {
            wva = (WebViewActivity) context.getActivity().getContext();
            if (wva != null) {
                wva.loadUrlFromJsSdk(uuid);
            }

        }
    }

    @Override
    public void isJsTakeKeyBack(INativeContext context, final JSONObject param) {
        if (context == null || param == null) {
            Logger.w(TAG, ERROR_CONTENT_PARA);
            return;
        }

        if (null != context.getActivity() &&
                null != context.getActivity().getContext() &&
                context.getActivity().getContext() instanceof WebViewActivity) {
            final WebViewActivity wva = (WebViewActivity) context.getActivity().getContext();
            if (wva != null) {
                ApfUiThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wva.isJsTakeKeyBack(param.optBoolean(IS_JS_TAKE_KEY_BACK,false));
                    }
                });

            }

        }
    }

    @Override
    public void trigerNativeKeyBack(INativeContext context, JSONObject param) {
        if (context == null) {
            Logger.w(TAG, ERROR_CONTENT_PARA);
            return;
        }
        if (null != context.getActivity() &&
                null != context.getActivity().getContext() &&
                context.getActivity().getContext() instanceof WebViewActivity) {
            final WebViewActivity wva = (WebViewActivity) context.getActivity().getContext();
            if (wva != null) {
                ApfUiThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wva.onKeyUp(KeyEvent.KEYCODE_BACK,new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_BACK));
                    }
                });

            }
        }
    }
}
