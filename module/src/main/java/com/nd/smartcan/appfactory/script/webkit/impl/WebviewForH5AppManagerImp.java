package com.nd.smartcan.appfactory.script.webkit.impl;

import android.content.Context;
import android.content.Intent;

import com.nd.smartcan.appfactory.keying.ProtocolConstant;
import com.nd.smartcan.appfactory.script.webkit.WebViewActivity;
import com.nd.smartcan.appfactory.script.webkit.utils.IWebviewForH5AppManager;
import com.nd.smartcan.appfactory.script.webkit.utils.WebViewUtils;
import com.nd.smartcan.appfactory.vm.PageUri;
import com.nd.smartcan.appfactory.vm.PageWrapper;
import com.nd.smartcan.commons.util.logger.Logger;

/**
 * Created by Administrator on 2018/2/22 0022.
 */

public class WebviewForH5AppManagerImp implements IWebviewForH5AppManager {
    private final static String TAG = WebviewForH5AppManagerImp.class.getSimpleName();
    @Override
    public PageWrapper getPage(Context context, PageUri pageUri) {
        if (pageUri != null) {
            Logger.i(TAG, "goPage, pageUri=" + pageUri.getPageUrl());
        }else{
            Logger.i(TAG, "goPage, pageUri= null");
            return null;
        }

        String protocol = pageUri.getProtocol();
        if (ProtocolConstant.KEY_HTML_PAGE_MANAGER.equals(protocol)
                || ProtocolConstant.KEY_HTTPS_PRO.equals(protocol)
                || ProtocolConstant.KEY_ONLINE.equals(protocol)
                || ProtocolConstant.KEY_LOCAL_HTML_PAGE_MANAGER.equals(protocol)) {
            PageWrapper pageWrapper = new PageWrapper(WebViewActivity.class.getName());
            Intent intent = new Intent(context, WebViewActivity.class);
            boolean result = WebViewUtils.setStartIntentExtra(context, pageUri, intent);
            if (!result) {
                return null;
            }
            pageWrapper.setIntent(intent);
            return pageWrapper;
        }

        return null;
    }
}
