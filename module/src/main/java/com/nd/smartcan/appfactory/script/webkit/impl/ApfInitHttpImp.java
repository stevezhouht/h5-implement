package com.nd.smartcan.appfactory.script.webkit.impl;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.nd.android.smartcan.network.NetworkRequest;
import com.nd.smartcan.appfactory.AppFactory;
import com.nd.smartcan.appfactory.businessInterface.OperateImp.IApfInitHttp;
import com.nd.smartcan.appfactory.keying.ProtocolConstant;
import com.nd.smartcan.commons.util.logger.Logger;
import com.nd.smartcan.core.http.Interceptor;
import com.nd.smartcan.core.restful.ResourceException;
import com.nd.smartcan.core.security.SecurityDelegate;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2018/2/26 0026.
 */

public class ApfInitHttpImp implements IApfInitHttp {
    private final static String TAG = "ApfInitHttpImp";
    private static final String AUTO_ADD_HEADER_KEY = "apf_";

    @Override
    public void initialPkgId() {
        SecurityDelegate.getInstance().registerNetworkInterceptor(new Interceptor() {
            @Override
            public HttpResponse intercept(@NonNull Chain chain) throws ResourceException {
                NetworkRequest request = chain.getNetworkRequest();
                List<Header> heads = request.getHeaders();

                //如果业务头部已经有SDP-PKG-ID，这时候框架不会再设置SDP-PKG-ID
                for (Header header : heads) {
                    if (TextUtils.equals(header.getName(), ProtocolConstant.SDP_PKG_ID)) {
                        return chain.proceed(request);
                    }
                }

                //否则，由MAF框架在头部加上SDP-PKG-ID默认值,优先设置CI输出中“pkgid”
                String pkgid = AppFactory.instance().getEnvironment(ProtocolConstant.PKG_ID, "");
                if (TextUtils.isEmpty(pkgid)) {
                    //如果没有，则值来至CI输出中“appid”
                    pkgid = AppFactory.instance().getEnvironment(ProtocolConstant.APP_ID, "");
                }

                final String haveEncodeAppId = android.net.Uri.encode(pkgid);
                Logger.i(TAG, "get current appId is=[" + pkgid + "] haveEnCodeAppId=[" + haveEncodeAppId + "]");
                request.addHeader(ProtocolConstant.SDP_PKG_ID, haveEncodeAppId);

                return chain.proceed(request);
            }
        });
    }

    @Override
    public void initialSDPHeader() {
        SecurityDelegate.getInstance().registerApplicationInterceptor(new Interceptor() {
            @Override
            public HttpResponse intercept(@NonNull Chain chain) throws ResourceException {
                NetworkRequest request = chain.getNetworkRequest();
                List<Header> heads = request.getHeaders();
                Set<String> set = AppFactory.instance().getConfigMap().keySet();

                String realSetKey=null;
                for (String key : set) {
                    if (key == null || key.trim().isEmpty()) {
                        continue;
                    }

                    if (key.trim().startsWith(AUTO_ADD_HEADER_KEY)) {
                        realSetKey=key.replaceFirst(AUTO_ADD_HEADER_KEY, "").trim();
                        boolean headerExist = false;
                        for (Header header : heads) {
                            if (TextUtils.equals(header.getName(), realSetKey)) {
                                headerExist = true;
                            }
                        }
                        if (!headerExist) {
                            // 获取 key 对应的 value
                            String originHeader = AppFactory.instance().getEnvironment(key);
                            final String haveEncodeheader = android.net.Uri.encode(originHeader);
                            Logger.i(TAG, "get current header is=[" + originHeader + "] haveEncodeheader=[" + haveEncodeheader + "]");
                            //把前缀去掉 这个是我们的业务标识apf_
                            request.addHeader(realSetKey, haveEncodeheader);
                        }
                    }
                }
                return chain.proceed(request);
            }
        });
    }
}
