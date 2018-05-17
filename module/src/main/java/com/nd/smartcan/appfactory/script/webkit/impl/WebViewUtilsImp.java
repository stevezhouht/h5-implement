package com.nd.smartcan.appfactory.script.webkit.impl;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;

import com.nd.android.skin.SkinConfig;
import com.nd.sdp.android.apf.h5.R;
import com.nd.sdp.android.common.res.utils.CommonSkinUtils;
import com.nd.smartcan.appfactory.AppFactory;
import com.nd.smartcan.appfactory.Config.DynamicSkinConfig;
import com.nd.smartcan.appfactory.Config.IConfigManager;
import com.nd.smartcan.appfactory.Config.dao.AnnounceJsonBean;
import com.nd.smartcan.appfactory.Config.dao.AnnounceJsonBeanOrmDao;
import com.nd.smartcan.appfactory.businessInterface.IWebViewMenuItem;
import com.nd.smartcan.appfactory.businessInterface.IWebViewMenuItemWithIcon;
import com.nd.smartcan.appfactory.keying.ProtocolConstant;
import com.nd.smartcan.appfactory.nativejs.util.MapScriptable;
import com.nd.smartcan.appfactory.script.common.Path;
import com.nd.smartcan.appfactory.script.hotfix.LightAppFactory;
import com.nd.smartcan.appfactory.script.hotfix.bean.LightComponent;
import com.nd.smartcan.appfactory.script.webkit.WebViewActivity;
import com.nd.smartcan.appfactory.script.webkit.WebViewActivityHelper;
import com.nd.smartcan.appfactory.script.webkit.utils.IWebViewUtils;
import com.nd.smartcan.appfactory.script.webkit.utils.UrlUtils;
import com.nd.smartcan.appfactory.script.webkit.utils.WebViewConst;
import com.nd.smartcan.appfactory.script.webkit.utils.WebViewUtils;
import com.nd.smartcan.appfactory.utils.UrlScheme;
import com.nd.smartcan.appfactory.vm.PageUri;
import com.nd.smartcan.commons.util.helper.DateUtil;
import com.nd.smartcan.commons.util.language.AppFactoryJsClassMap;
import com.nd.smartcan.commons.util.language.StringUtils;
import com.nd.smartcan.commons.util.logger.Logger;
import com.nd.smartcan.commons.util.security.SecurityUtil;
import com.nd.smartcan.webview.global.CommonConsoleMessage;
import com.nd.smartcan.webview.outerInterface.ISslError;
import com.nd.smartcan.webview.outerInterface.IWebView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by Administrator on 2018/2/22 0022.
 */

public class WebViewUtilsImp implements IWebViewUtils {
    public final static String SDCARD_PREFIX = "file://";
    private final static String ASSET_PREFIX = "file:///android_asset";
    private final static String TAG = WebViewUtilsImp.class.getSimpleName();
    protected final static String JSONOBJECT_NULL = "";
    private static String LOCAL = null;
    private final static String LOCAL_TAG = ProtocolConstant.KEY_HTML_PAGE_MANAGER + "://local";
    private final static String PRIVATE_LOCAL_TAG = ProtocolConstant.KEY_HTML_PAGE_MANAGER + "://private";

    private final static String ASSET = "file:///android_asset";
    private final static String ASSET_TAG = ProtocolConstant.KEY_HTML_PAGE_MANAGER + "://bundle";

    private final static String QUERY_SEPARATOR = "?";
    private final static String HASH_TAG = "#";
    // https://developer.chrome.com/multidevice/android/intents
    // browser_fallback_url: When an intent could not be resolved,
    // or an external application could not be launched,
    // then the user will be redirected to the fallback URL if it was given.
    private static final String BROWSER_FALLBACK_URL = "browser_fallback_url";

    @Override
    public boolean doDefaultWebSetting(WebSettings webSettings) {
        if (webSettings == null) {
            return false;
        }
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAppCacheMaxSize(8 * 1024 * 1024);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }

        return true;
    }

    @Override
    public String proxyCall(Context context, String methodName, String params, Object instance, Class annotationClass) {
        String resultMessage = JSONOBJECT_NULL;
        Object result = AppFactoryJsClassMap
                .invokeMethod(context, methodName, params, instance, annotationClass, Context.class,
                        String.class);
        if (result != null) {
            resultMessage = result.toString();// toString的结果
        }
        return resultMessage;
    }

    @Override
    public ArrayList<String> parseJsonArrayStrToArray(String jsonArrayStr) {
        ArrayList<String> stringArrayList = new ArrayList<String>();
        if (TextUtils.isEmpty(jsonArrayStr)) {
            return stringArrayList;
        }
        if (!jsonArrayStr.trim().startsWith("[")) {
            jsonArrayStr = "[" + jsonArrayStr;
        }
        if (!jsonArrayStr.trim().endsWith("]")) {
            jsonArrayStr = jsonArrayStr + "]";
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayStr);
            if (null == jsonArray) {
                return stringArrayList;
            }
            for (int i = 0; i < jsonArray.length(); ++i) {
                stringArrayList.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            Logger.w(TAG, e.getMessage());
        }
        return stringArrayList;
    }

    @Override
    public boolean isNetworkConnected(Context context) {
        if(null == context || null == context.getApplicationContext()){
            Logger.w(TAG,"null == context || null == context.getApplicationContext()");
            return false;
        }
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    @Override
    public String getWebUrl(Context context, String url) {
        if (url == null || url.trim().length() == 0) {
            return null;
        }
        url = url.trim();

        // 如果不是本地文件，不做特殊处理，直接交由WebView处理
        if (!url.startsWith(ProtocolConstant.KEY_LOCAL_HTML_PAGE_MANAGER)) {
            //如果是在线轻应用协议，那么就处理在线的。
            if (url.startsWith(ProtocolConstant.KEY_ONLINE)) {
                //解析转换成真实的地址
                String realUrl = WebViewUtils.parseOnLineUrl(url);
                Logger.w(TAG, "online 协议解析的 真实地址是 " + realUrl + " 原始地址是 " + url);
                return realUrl;
            } else {
                return WebViewUtils.getNoAppWebUrl(context, url); // 兼容旧的协议
            }
            // return url;
        }

        PageUri pageUri = new PageUri(url);
        String componentId = pageUri.getPlugin();
        if (componentId != null) {
            if (context instanceof WebViewActivity) {
                ((WebViewActivity) context).setComponentId(componentId);
            }
        }
        LightComponent lightComponent = LightAppFactory.getInstance()
                .getLightComponentList()
                .getItem(componentId, LightComponent.HTML);
        if (lightComponent != null) {
            String path = Path.getPath(context, componentId, lightComponent.getType(),
                    lightComponent.getLocation(), lightComponent.getCreateTime());
            File cacheFile = new File(path, pageUri.getPageName());
            if (lightComponent.getLocation() == LightComponent.Location.DATA
                    && cacheFile.exists()) {
                Log.i(TAG, "加载最新的版本,路径:" + cacheFile.getPath() + "; 更新时间:" +
                        DateUtil.getDateStringFromMillisecond(lightComponent.getCreateTime(),
                                DateUtil.NOW_TIME_MIN));
                return SDCARD_PREFIX + cacheFile.getPath();
            } else {
                String localStr = "local://";
                String uriPath = url.substring(localStr.length() + componentId.length());
                String host = lightComponent.getHost();
                if (host == null || uriPath == null) {
                    return null;
                } else {
                    return host + uriPath;
                }
            }
        } else {
            Logger.e(TAG, "解析Url失败!");
            // 获取url中的路径部分(去掉协议头和queryString部分), 这部分也会作为web文件在本地的路径的一部分
            String urlPath = UrlUtils.getUrlHostPortPath(url);
            // url路径中带的参数(?query#fragment)，scheme://host:port/path?query#fragment
            String urlParam = UrlUtils.getUrlParam(url);

            // 路径地址中标示文件的部分
            String relativePath = WebViewConst.FILE_STORAGE_RELATIVE_PATH
                    + File.separator
                    + urlPath;

            String sdcardPath = null;
            if (AppFactory.instance().getAppRootSdCardDir(context) != null) {
                sdcardPath = AppFactory.instance().getAppRootSdCardDir(context)
                        + File.separator
                        + relativePath;
            }

//            updateH5ComponentStatus(context, sdcardPath);
            // 先尝试从sdcard读取,如果sdcard不存在指定文件,就假设在asset目录下,否则返回在线地址
            if (!TextUtils.isEmpty(sdcardPath) && new File(sdcardPath).exists()) {
                return SDCARD_PREFIX // 协议
                        + sdcardPath  // 路径
                        + urlParam; // 参数
            } else if (WebViewUtils.isAssetFile(context, relativePath)) {
                // JELLY_BEAN 及 以上版本支持访问asset目录下的html带参数
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    return ASSET_PREFIX // 协议
                            + File.separator
                            + relativePath // 路径(asset目录不需要一个标志此应用的路径，所以比sdcard中少了一部分)
                            + urlParam; // 参数
                } else {
                    return ASSET_PREFIX // 协议
                            + File.separator
                            + relativePath; // 路径(asset目录不需要一个标志此应用的路径，所以比sdcard中少了一部分)
                }
            } else {
                // 传入的路径为： local://com.nd.sdp.component.test4whl.test-4-whl/index.html
                // urlPath 为： com.nd.sdp.component.test4whl.test-4-whl/index.html
                String[] parts = urlPath.split("/");
                if (parts.length > 1) {
                    String componentId0 = parts[0];
                    String localStr = "local://";
                    String path = url.substring(localStr.length() + componentId0.length());
                    String namespace = WebViewUtils.getNameSpaceByComId(componentId0);
                    String name = WebViewUtils.getNameByComId(componentId0);
                    AnnounceJsonBean announceJsonBean = AnnounceJsonBeanOrmDao.getAnnounceJsonBean(namespace, name);
                    if (announceJsonBean == null) {
                        Logger.e(TAG, "数据库中无法找到该组件的host!");
                        return null;
                    }
                    String host = announceJsonBean.getHost();
                    if (TextUtils.isEmpty(host)) {
                        Logger.e(TAG, "host 为空!");
                        return null;
                    }
                    return host + path;
                } else {
                    Logger.e(TAG, "解析Url失败!");
                    return null;
                }

            }
//            return null;
        }
    }

    @Override
    public boolean isAssetFile(Context context, String relativePath) {
        AssetManager assetManager = context.getAssets();
        int separator = relativePath.lastIndexOf("/");
        String dirPath = "" ;
        String filePath = "";
        if (separator == -1) {
            filePath = relativePath;
        } else {
            dirPath = relativePath.substring(0, separator);
            filePath = relativePath.substring(separator + 1, relativePath.length());
        }
        try {
            String[] names = assetManager.list(dirPath);
            for (String name : names) {
                if (filePath.equals(name)) {
                    Logger.i(TAG, "Asset目录下找到该文件: " + relativePath);
                    return true;
                }
            }
        } catch (IOException e) {
            Logger.e(TAG, "读取Asset目录下的文件出错： " + e.getMessage());
            return false;
        }
        return false;
    }

    @Override
    public String parseOnLineUrl(String url) {
        if (url == null || url.trim().length() == 0) {
            return null;
        }
        url = url.trim();
        String pre = ProtocolConstant.KEY_ONLINE + "://";

        if (!url.startsWith(pre)) {
            return null;
        }

        String leftStr_1 = url.replaceFirst(pre, "");
        //说明不是轻应用协议
        if (url.equals(leftStr_1)) {
            return null;
        }
        //这时候数据形式就是 comid/index.html。。。。。
        String[] splitData = leftStr_1.split("/");
        if (splitData.length > 0) {
            String comId = splitData[0];

            String end = null;
            //如果只是  comid 那么就返回host
            if (leftStr_1.equals(comId)) {
                end = null;
            } else {
                //如何还有下一级才进行替换
                String wantAdd = "";
                if (leftStr_1.startsWith(comId + "/")) {
                    wantAdd = "/";
                }

                //得到  index.html。。。。。
                String leftStr_2 = leftStr_1.replaceFirst(comId + wantAdd, "");
                end = leftStr_2;
                //这种情况说明 leftStr_1 没有 /
                if (leftStr_1.equals(leftStr_2)) {
                    end = null;
                }
            }

            IConfigManager con = AppFactory.instance().getConfigManager();
            if (con != null) {
                String host = con.getHostById(comId);
                if (host != null) {
                    host = host.trim();
                    if (end == null) {
                        return host;
                    }
                    if (host.endsWith("/")) {
                        return host + end;
                    } else {
                        return host + "/" + end;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getH5DataDir(String url, Context context) {
        if (url == null || url.trim().length() == 0) {
            Logger.w(TAG, "传入的url为空------");
            return null;
        }
        if (context == null) {
            Logger.w(TAG, "传入的context为空------");
            return null;
        }
        String oldUrl = url.trim();
        String relativePath = null;
        String root = AppFactory.instance().getAppRootSdCardDir(context);
        String preRelativePath = root + File.separator + WebViewConst.APP_FACTORY
                + File.separator + WebViewConst.DATA;
        String defPath = preRelativePath + File.separator + WebViewConst.PUBLIC + File.separator;
        if (root == null) {
            Logger.w(TAG, "获取SD卡出错，当前前设备可能不存在sd卡-------");
            return null;
        }
        if (oldUrl.startsWith(WebViewConst.HTTP)) {
            relativePath = preRelativePath + File.separator + WebViewConst.PUBLIC + File.separator;
        } else if (oldUrl.startsWith(WebViewConst.FILE)) {
            String[] strH5App, strFileSeparator;
            try {
                strH5App = oldUrl.split(WebViewConst.H5_APP);
                if (strH5App.length > 1) {
                    strFileSeparator = strH5App[1].split("/");
                } else {
                    Logger.i(TAG, "传入的路径没有h5_app，默认放在public目录下------");
                    mkdirs(defPath);
                    return defPath;
                }
            } catch (PatternSyntaxException e) {
                Logger.w(TAG, "分割字符串出错------");
                return null;
            }
            String component_id = null;
            if (strFileSeparator.length > 1) {
                component_id = strFileSeparator[0];
            } else {
                Logger.i(TAG, "无法获取component_id，分割后的字符串数组长度小于2----");
                mkdirs(defPath);
                return defPath;
            }
            relativePath = preRelativePath + File.separator + component_id + File.separator;
        } else {
            Logger.w(TAG, "传入的路径并非file: 和 http 开头----");
            relativePath = preRelativePath + File.separator + WebViewConst.PUBLIC + File.separator;
        }
        mkdirs(relativePath);
        return relativePath;
    }

    private static void mkdirs(String path) {
        if (!new File(path).exists()) {
            //if the dir don't exist, create it
            boolean success = new File(path).mkdirs();
            if (!success) {
                Logger.w(TAG, "创建文件夹出错--------");
            }
        }
    }

    @Override
    public String getNoAppWebUrl(Context context, String source) {
        if (TextUtils.isEmpty(source)) {
            return null;
        }
        if (LOCAL == null) {
            if (getSdPath() != null) {
                LOCAL = "file:///" + getSdPath();
            } else {
                LOCAL = "file:///";
            }
        }

        File privateDir = context.getFilesDir();
        String privateTag = "file:///" + privateDir.getPath();

        if (source.startsWith(ASSET_TAG)) {
            return source.replace(ASSET_TAG, ASSET);
        } else if (source.startsWith(LOCAL_TAG)) {
            return source.replace(LOCAL_TAG, LOCAL);
        } else if (source.startsWith(PRIVATE_LOCAL_TAG)) {
            return source.replace(PRIVATE_LOCAL_TAG, privateTag);
        }
        return source;
    }


    @Override
    public String getSdPath() {
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        String path = null;
        if (sdCardExist && Environment.getExternalStorageDirectory() != null) {
            path = Environment.getExternalStorageDirectory().toString();//获取跟目录
        } else {
            Logger.w(TAG, "sd卡不存在或者不可用，请检查------");
        }
        return path;
    }

    @Override
    public boolean setStartIntentExtra(Context context, PageUri pageUri, Intent intent) {
        if (pageUri == null || intent == null) {
            return false;
        }
        Map<String, String> params = pageUri.getParam();
        String originalUrl = pageUri.getPageUrl();
        Logger.w(TAG, "应用工厂webview originalUrl=" + originalUrl);
//        Logger.w(TAG, "应用工厂webview filterUrl=" + filterUrl);
        intent.putExtra(WebViewConst.WANT_LOAD_URL, originalUrl);
        intent.putExtra(WebViewConst.KEY_NOT_REPEAT_ACTIVITY, true);//一个标记，表示当前页面不想复用
        if (null != params) {
            // 第一次打开网页时要显示的标题
            if (params.containsKey(WebViewConst.WEBVIEW_TITLE)) {
                intent.putExtra(WebViewConst.WEBVIEW_TITLE, params.get(WebViewConst.WEBVIEW_TITLE));
            }

            // 第一次打开网页时要显示的标题
            if (params.containsKey(WebViewConst.maf_webview_title)) {
                intent.putExtra(WebViewConst.WEBVIEW_TITLE, params.get(WebViewConst.maf_webview_title));
            }

            // 自定义显示的下拉菜单项
            if (params.containsKey(WebViewConst.MENU_IDS)) {
                String str_menuids = params.get(WebViewConst.MENU_IDS);
                intent.putStringArrayListExtra(WebViewConst.MENU_IDS, parseJsonArrayStrToArray(str_menuids));
            }

            // 自定义显示的下拉菜单项
            if (params.containsKey(WebViewConst.maf_menu_ids)) {
                String str_menuids = params.get(WebViewConst.maf_menu_ids);
                intent.putStringArrayListExtra(WebViewConst.MENU_IDS, parseJsonArrayStrToArray(str_menuids));
            }

            // 导航栏文字颜色
            if (params.containsKey(WebViewConst.MAF_NAVIGATIONBAR_TEXTCOLOR)) {
                intent.putExtra(WebViewConst.MAF_NAVIGATIONBAR_TEXTCOLOR, params.get(WebViewConst.MAF_NAVIGATIONBAR_TEXTCOLOR));
            }

            // 导航栏背景颜色
            if (params.containsKey(WebViewConst.MAF_NAVIGATIONBAR_BACKGROUNDCOLOR)) {
                intent.putExtra(WebViewConst.MAF_NAVIGATIONBAR_BACKGROUNDCOLOR, params.get(WebViewConst.MAF_NAVIGATIONBAR_BACKGROUNDCOLOR));
            }

            // 状态栏文字颜色
            if (params.containsKey(WebViewConst.MAF_STATUSBAR_TEXTCOLOR)) {
                intent.putExtra(WebViewConst.MAF_STATUSBAR_TEXTCOLOR, params.get(WebViewConst.MAF_STATUSBAR_TEXTCOLOR));
            }

            //用来控制left Button的行为close back none
            if (params.containsKey(WebViewConst.LEFT_BUTTON)) {
                intent.putExtra(WebViewConst.LEFT_BUTTON, params.get(WebViewConst.LEFT_BUTTON));
            }

            //用来控制left Button的行为close back none
            if (params.containsKey(WebViewConst.maf_left_button)) {
                intent.putExtra(WebViewConst.LEFT_BUTTON, params.get(WebViewConst.maf_left_button));
            }

            // 网页触发下载时，要发送的事件名称
            if (params.containsKey(WebViewConst.maf_down_start_event_name)) {
                intent.putExtra(WebViewConst.maf_down_start_event_name, params.get(WebViewConst.maf_down_start_event_name));
            }

            // 自定义显示的水平菜单项
            if (params.containsKey(WebViewConst.maf_btn_ids)) {
                String str_menuids = params.get(WebViewConst.maf_btn_ids);
                intent.putStringArrayListExtra(WebViewConst.maf_btn_ids, parseJsonArrayStrToArray(str_menuids));
            }

            // 是否显示加载进度条
            if (params.containsKey(WebViewConst.MAF_SHOW_PROGRESS_BAR)) {
                boolean value = Boolean.valueOf(params.get(WebViewConst.MAF_SHOW_PROGRESS_BAR));
                intent.putExtra(WebViewConst.MAF_SHOW_PROGRESS_BAR, value);
            }

            // 是否显示导航栏
            if (params.containsKey(WebViewConst.MAF_SHOW_NAVIGATIONBAR)) {
                boolean value = Boolean.valueOf(params.get(WebViewConst.MAF_SHOW_NAVIGATIONBAR));
                intent.putExtra(WebViewConst.MAF_SHOW_NAVIGATIONBAR, value);
            }

            // 是否开启WebView缓存 --  参数传递来开启缓存的方式废弃！
//            if (params.containsKey(WebViewConst.CACHE_OPEN)) {
//                boolean value = Boolean.valueOf(params.get(WebViewConst.CACHE_OPEN));
//                intent.putExtra(WebViewConst.CACHE_OPEN, value);
//            }

            // 背景是否透明
            if (params.containsKey(WebViewConst.MAF_BG_TRANSPARENT)) {
                boolean value = Boolean.valueOf(params.get(WebViewConst.MAF_BG_TRANSPARENT));
                intent.putExtra(WebViewConst.MAF_BG_TRANSPARENT, value);
            }

            // 自定义webview loadUrl的头部
            if (params.containsKey(WebViewConst.KEY_ADDITIONAL_HTTP_HEADERS)) {
                // 用户传过来的是 JsonObject
                String value = params.get(WebViewConst.KEY_ADDITIONAL_HTTP_HEADERS);
                intent.putExtra(WebViewConst.KEY_ADDITIONAL_HTTP_HEADERS, value);
            }
        }

        return true;
    }

    @Override
    public String dealWithMoreInfo(String wantUrl) {
        if (wantUrl == null || TextUtils.isEmpty(wantUrl.trim())) {
            Logger.w(TAG, "得到的url为空. " + wantUrl);
            return null;
        }
        String haveDealWithUrl = wantUrl;
        Logger.w(TAG, "通过正则表达式处理--之前数据" + wantUrl);
        //配合任何由以下5个部分组成。
        //          _maf开头
        //          非&=的任意字符
        //          =
        //         非&=任意字符
        //         一个或者零个&
        String regex = "(_maf[^&=]+=[^&=#]*&?)";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(wantUrl);
        haveDealWithUrl = m.replaceAll("");
        if (haveDealWithUrl.endsWith("?")) {
            haveDealWithUrl = haveDealWithUrl.substring(0, haveDealWithUrl.length() - 1);
        }

        if (haveDealWithUrl.endsWith("&")) {
            haveDealWithUrl = haveDealWithUrl.substring(0, haveDealWithUrl.length() - 1);
        }
        Logger.w(TAG, "通过正则表达式处理--之后数据" + haveDealWithUrl);
        return haveDealWithUrl;
    }

    @Override
    public void registerDefaultMenu(Context context) {
        if (null == context) {
            Logger.w(TAG, "registerDefaultMenu context = null");
            return;
        }
        //maf.copyUrl：默认右键菜单》》复制链接
        MapScriptable param1 = new MapScriptable();
        param1.put(WebViewConst.KEY_MENU_ID, WebViewConst.MENU_COPY);
        param1.put(WebViewConst.KEY_MENU_NAME, context.getString(R.string.appfactory_webview_copy_url));
        param1.put(WebViewConst.KEY_MENU_NAME_ID, R.string.appfactory_webview_copy_url);
        param1.put(WebViewConst.KEY_MENU_ICON, context.getResources().getDrawable(R.drawable.webcomponent_menu_copy_url));
        param1.put(WebViewConst.KEY_MENU_ICON_RES_NAME, "webcomponent_menu_copy_url");
        param1.put(WebViewConst.KEY_ONCLICK_EVENT_NAME, WebViewConst.EVENT_MENU_COPY);
        WebViewActivityHelper.monitorEvent(context, WebViewConst.EVENT_REGISTER_APPFACTORY_MENU, param1);

        //maf.openWithBrowser：默认右键菜单》》使用系统浏览器打开
        MapScriptable param2 = new MapScriptable();
        param2.put(WebViewConst.KEY_MENU_ID, WebViewConst.MENU_OPEN_WITH_BROWSER);
        param2.put(WebViewConst.KEY_MENU_NAME, context.getString(R.string.appfactory_webview_open_with_browser));
        param2.put(WebViewConst.KEY_MENU_NAME_ID, R.string.appfactory_webview_open_with_browser);
        param2.put(WebViewConst.KEY_MENU_ICON, context.getResources().getDrawable(R.drawable.webcomponent_menu_browser));
        param2.put(WebViewConst.KEY_MENU_ICON_RES_NAME, "webcomponent_menu_browser");
        param2.put(WebViewConst.KEY_ONCLICK_EVENT_NAME, WebViewConst.EVENT_MENU_OPEN_WITH_BROWSER);
        WebViewActivityHelper.monitorEvent(context, WebViewConst.EVENT_REGISTER_APPFACTORY_MENU, param2);
        //默认右键菜单》》修改文字大小
//        MapScriptable param3 = new MapScriptable();
//        param3.put(WebViewConst.KEY_MENU_ID,WebViewConst.MENU_SET_FONT);
//        param3.put(WebViewConst.KEY_MENU_NAME, context.getString(R.string.webcomponent_set_font));
        // param3.put(WebViewConst.KEY_MENU_NAME_ID, R.string.webcomponent_set_font);
//        param3.put(WebViewConst.KEY_MENU_ICON, context.getResources().getDrawable(R.drawable.webcomponent_menu_set_font));
//        param3.put(WebViewConst.KEY_ONCLICK_EVENT_NAME, WebViewConst.EVENT_MENU_SET_FONT);
//        AppFactory.instance().triggerEvent(context, WebViewConst.EVENT_REGISTER_APPFACTORY_MENU, param3);
        //默认右键菜单》》重新载入
        MapScriptable param4 = new MapScriptable();
        param4.put(WebViewConst.KEY_MENU_ID, WebViewConst.MENU_REFRESH);
        param4.put(WebViewConst.KEY_MENU_NAME, context.getString(R.string.appfactory_webview_refresh));
        param4.put(WebViewConst.KEY_MENU_NAME_ID, R.string.appfactory_webview_refresh);
        param4.put(WebViewConst.KEY_MENU_ICON, context.getResources().getDrawable(R.drawable.webcomponent_menu_refresh));
        param4.put(WebViewConst.KEY_MENU_ICON_RES_NAME, "webcomponent_menu_refresh");
        param4.put(WebViewConst.KEY_ONCLICK_EVENT_NAME, WebViewConst.EVENT_MENU_REFRESH);
        WebViewActivityHelper.monitorEvent(context, WebViewConst.EVENT_REGISTER_APPFACTORY_MENU, param4);
    }

    @Override
    public Drawable getMenuItemSkin(IWebViewMenuItem iMenuItem) {
        if(null == iMenuItem){
            return null;
        }
        if (iMenuItem instanceof IWebViewMenuItemWithIcon) {
            IWebViewMenuItemWithIcon itemWithIcon = (IWebViewMenuItemWithIcon) iMenuItem;
            Drawable drawable = CommonSkinUtils.getDrawable(itemWithIcon.getMenuIconResName());
            if (drawable != null) {
                return drawable;
            }
        }
        return iMenuItem.getMenuIcon();
    }

    @Override
    public String getComponentIdByUrl(String url) {
        if (url == null || url.trim().length() == 0) {
            return null;
        }
        String[] splitStr, compId;
        try {
            splitStr = url.split(WebViewConst.FILE_STORAGE_RELATIVE_PATH + File.separator);
            if (splitStr.length > 1) {
                if (splitStr[1].contains("/")) {
                    compId = splitStr[1].split("/");
                } else {
                    return splitStr[1];
                }
            } else {
                Logger.w(TAG, "url并不包含" + WebViewConst.FILE_STORAGE_RELATIVE_PATH);
                return null;
            }
        } catch (PatternSyntaxException e) {
            Logger.e(TAG, "分割字符串出错");
            return null;
        }
        if (compId.length > 1) {
            return compId[0];
        } else {
            Logger.w(TAG, "url并不包含 / 符号");
            return null;
        }
    }

    @Override
    public String getComponentIdByUrlStartWithLocal(String url) {
        if (url == null || url.trim().length() == 0) {
            return null;
        }
        url = url.substring(ProtocolConstant.KEY_LOCAL_HTML_PAGE_MANAGER.length() + 3); // 跳过 local://
        String[] splitStr;
        splitStr = url.split("/");
        if (splitStr.length > 1) {
            return splitStr[0];
        } else {
            Logger.w(TAG, "url并不包含 / ");
            return null;
        }
    }

    @Override
    public String getNameByComId(String comId) {
        if (comId == null || comId.trim().length() == 0) {
            return null;
        }
        if (!comId.contains(".")) {
            return null;
        }
        return comId.substring(comId.lastIndexOf(".") + 1, comId.length());
    }

    @Override
    public String getNameSpaceByComId(String comId) {
        if (comId == null || comId.trim().length() == 0) {
            return null;
        }
        if (!comId.contains(".")) {
            return null;
        }
        return comId.substring(0, comId.lastIndexOf("."));
    }

    @Override
    public String changeSdPathToOnlinePath(String localPath) {
        if (StringUtils.isEmpty(localPath)) {
            return null;
        }
        String componentId = getComponentIdByUrl(localPath);
        if(null == componentId){
            return null;
        }
        String pagePath = localPath.split(componentId)[1];
        String namespace = getNameSpaceByComId(componentId);
        String name = getNameByComId(componentId);
        AnnounceJsonBean announceJsonBean = AnnounceJsonBeanOrmDao.getAnnounceJsonBean(namespace, name);
        if (announceJsonBean == null) {
            Logger.e(TAG, "数据库中无法找到该组件的host!");
            return null;
        }
        String host = announceJsonBean.getHost();
        if (TextUtils.isEmpty(host)) {
            Logger.e(TAG, "host 为空!");
            return null;
        }
        return host + pagePath;
    }

    @Override
    public boolean isSdCardPath(String host, String rootPath) {
        if (host != null && host.startsWith("file://") && host.contains(rootPath)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getPathByLocalUrl(String path) {
        try {
            URL url = new URL(path);
            return url.getPath();
        } catch (MalformedURLException e) {
            Logger.e(TAG, "不符合URL转换规则，转换出错！" + e.getMessage());
            return null;
        }
    }

    @Override
    public String getUrlParam(String urlPath) {
        if(null == urlPath){
            return "";
        }
        String param;
        if (urlPath.contains(QUERY_SEPARATOR)) {
            param = urlPath.substring(urlPath.indexOf(QUERY_SEPARATOR));
            return param;
        }
        if (urlPath.contains(HASH_TAG)) {
            param = urlPath.substring(urlPath.indexOf(HASH_TAG));
            return param;
        }
        return "";// 此处不能返回null
    }

    @Override
    public File getLocalResource(String urlStr, String componentId) {
        if(componentId == null){
            return null;
        }

        if(urlStr == null){
            return null;
        }

        //截取当前的组件路径 "/style"以后（含）的部分
        int pos = urlStr.indexOf(WebViewConst.INTERCEPT_KEY_DYNAMIC);

        String dynamicUrl = null;
        if (pos >= 0 && pos <= urlStr.length()) {
            dynamicUrl = urlStr.substring(pos + WebViewConst.INTERCEPT_KEY_DYNAMIC.length());
        }

        if(dynamicUrl== null){
            return null;
        }

        //本地皮肤包存储路径
        DynamicSkinConfig dynamicSkinConfig = new DynamicSkinConfig(AppFactory.instance().getApplicationContext());
        String skinPath = dynamicSkinConfig.getCustomSkinPath(AppFactory.instance().getApplicationContext());
        if (skinPath == null || skinPath.equals(SkinConfig.DEFAULT_SKIN_PATH)) {
            Logger.i("getLocalResource", "默认皮肤不支持h5换肤");
            return null;
        }
        // todo 皮肤商场如果添加了一级目录后, 即可去掉这行代码. 暂时手动获取一级目录
        // before
//        String dir = skinPath.substring(0, skinPath.lastIndexOf(".skin"));
//        String localDynamicUrl = dir + File.separator + componentId + "_h5" + dynamicUrl;
        // now
        String dir = skinPath.substring(0, skinPath.lastIndexOf("/"));
        String localDynamicUrl = dir + File.separator + componentId + "_h5" + dynamicUrl;
        Logger.i(TAG, "getLocalResource: current component localDynamicUrl ==  " + localDynamicUrl);

        File fileRet = new File(localDynamicUrl);
        if(!fileRet.exists()){
            Logger.i(TAG, "h5动态换肤资源替换失败, 未找到对应的h5资源!");
            return null;
        } else {
            Logger.i(TAG, "h5动态换肤资源替换成功!");
            return fileRet;
        }
    }

    @Override
    public boolean isSpecialWebViewCore(Context context, ISslError error) {
        // Android 官网介绍：Since Android 4.4 (KitKat), the WebView component is based on the Chromium open source project
        // https://developer.chrome.com/multidevice/webview/overview
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo("com.google.android.webview", 0);
                String certificate = error.getCertificate().toString();
                Logger.w("WebViewCore", "version name: " + pi.versionName);
                Logger.w("WebViewCore", "version code: " + pi.versionCode);
                Logger.w("WebViewCore", "certificate code: " + certificate);
                Logger.w("WebViewCore", "primary error: " + error.getPrimaryError());
                return isSpecialVersion(certificate, pi.versionName) || isSpecialModel();
            } catch (PackageManager.NameNotFoundException e) {
                Logger.w("WebViewCore", "Android System WebView is not found: com.google.android.webview");
                return false;
            }
        } else {
            Logger.w("WebViewCore", "Android System WebView is not found: Android Version below 4.4...");
            return isSpecialModel();
        }
    }

    @Override
    public boolean isSpecialVersion(String certificate, String versionName) {
        if (certificate == null || versionName == null) {
            return false;
        } else {
            if (versionName.trim().startsWith(WebViewConst.CHROMIUM_VERSION_53)
                    || versionName.trim().startsWith(WebViewConst.CHROMIUM_VERSION_54)) {
                if (certificate.trim().contains(WebViewConst.SYMANTEC_CERTIFICATE)
                        || certificate.trim().contains(WebViewConst.GEOTRUST_CERTIFICATE)
                        || certificate.trim().contains(WebViewConst.THAWTE_CERTIFICATE)) {
                    Logger.w("WebViewCore", "检查到该本地的WebViewCore存在官方bug，直接加载sslError的网页");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSpecialModel() {
        if (Build.BRAND.equals("Coolpad") && Build.MODEL.equals("Coolpad 9190_T00")
                && Build.VERSION.RELEASE.equals("4.3")) {
            return true;
        } else if (Build.BRAND.equals("samsung") && Build.MODEL.equals("GT-I9300")
                && Build.VERSION.RELEASE.equals("4.0.4")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String buildSecurityCertificateString(Context context, ISslError iSslError) {
        if(null == context || null == iSslError){
            return "";
        }
        // 颁发给 常用名称
        String issuedToCName = iSslError.getCertificate().getIssuedTo().getCName();
        // 颁发给 组织
        String issuedToOName = iSslError.getCertificate().getIssuedTo().getOName();
        // 颁发给 组织单位
        String issuedToUName = iSslError.getCertificate().getIssuedTo().getUName();
        // 颁发者 常用名称
        String issuedByCName = iSslError.getCertificate().getIssuedBy().getCName();
        // 颁发者 组织
        String issuedByOName = iSslError.getCertificate().getIssuedBy().getOName();
        // 颁发者 组织单位
        String issuedByUName = iSslError.getCertificate().getIssuedBy().getUName();

        // 颁发时间
        Date issuedOnDate = iSslError.getCertificate().getValidNotBeforeDate();
        String issuedOnStr = new SimpleDateFormat("yyyy-MM-dd").format(issuedOnDate);
        // 截止时间
        Date expiresOnDate = iSslError.getCertificate().getValidNotAfterDate();
        String expiresOnStr = new SimpleDateFormat("yyyy-MM-dd").format(expiresOnDate);

        // 序列号
        String serialNumberStr = "";
        // SHA-256 指纹
        String sha256Fingerprints = "";
        // SHA-1 指纹
        String sha1Fingerprints = "";

        Bundle bundle = SslCertificate.saveState(iSslError.getCertificate());
        X509Certificate certificate = null;
        byte[] bytes = bundle.getByteArray("x509-certificate");
        if (bytes == null) {
            certificate = null;
        } else {
            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                Certificate cert = certFactory.generateCertificate(new ByteArrayInputStream(bytes));
                certificate = (X509Certificate) cert;
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] key256 = sha256.digest(cert.getEncoded());
                sha256Fingerprints =  WebViewUtils.splitHexStringByColon(SecurityUtil.toHexString(key256));
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                byte[] key1 = sha1.digest(cert.getEncoded());
                sha1Fingerprints =  WebViewUtils.splitHexStringByColon(SecurityUtil.toHexString(key1));
            } catch (CertificateException e) {
                certificate = null;
                Logger.e(TAG, "certificate is null!");
            } catch (NoSuchAlgorithmException e) {
                Logger.e(TAG, e.getMessage());
            }
        }
        if (certificate != null) {
            serialNumberStr = WebViewUtils.splitHexStringByColon(certificate.getSerialNumber().toString(16));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(context.getResources().getString(R.string.certificate_issued_to)).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_common_name)).append("\n");
        sb.append(issuedToCName).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_organization)).append("\n");
        sb.append(issuedToOName).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_organization_unit)).append("\n");
        sb.append(issuedToUName).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_serial_number)).append("\n");
        sb.append(serialNumberStr).append("\n");

        sb.append("\n");

        sb.append(context.getResources().getString(R.string.certificate_issued_by)).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_common_name)).append("\n");
        sb.append(issuedByCName).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_organization)).append("\n");
        sb.append(issuedByOName).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_organization_unit)).append("\n");
        sb.append(issuedByUName).append("\n");

        sb.append("\n");

        sb.append(context.getResources().getString(R.string.certificate_validity)).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_issued_on)).append("\n");
        sb.append(issuedOnStr).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_expires_on)).append("\n");
        sb.append(expiresOnStr).append("\n");

        sb.append("\n");

        sb.append(context.getResources().getString(R.string.certificate_fingerprints)).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_sha_256)).append("\n");
        sb.append(sha256Fingerprints).append("\n");
        sb.append(context.getResources().getString(R.string.certificate_sha_1)).append("\n");
        sb.append(sha1Fingerprints).append("\n");

        return sb.toString();
    }

    @Override
    public String splitHexStringByColon(String resource) {
        if (resource == null || resource.trim().length() == 0) {
            return "";
        }
        int length = resource.trim().length();
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (i < length) {
            sb.append(resource.charAt(i++));
            if (i < length) {
                sb.append(resource.charAt(i++));
            }
            if (i + 1 <= length) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean shouldOverrideUrlLoading(Context context, IWebView webView, String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri formatUrl = Uri.parse(url);
            String scheme = formatUrl.getScheme();
            if (scheme == null) {
                return false;
            }
            if (scheme.equals(UrlScheme.TEL.toString()) || scheme.equals(UrlScheme.SMS.toString())) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
                return true;
            } else if (scheme.equals(UrlScheme.COMPONENT_MANAGER.toString())) {
                AppFactory.instance().goPage(context, url);
                return true;
            } else if (scheme.equals(UrlScheme.NDAPP.toString())) {
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } catch (ActivityNotFoundException e) {
                    Logger.e("WebViewActivity", "Activity 没有找到：" + e.getMessage());
                    return false;
                }
            } else if (scheme.equals(UrlScheme.INTENT.toString())) {
                try {
                    // 开始处理 "intent://*****" 的情况
                    Logger.i("WebViewActivity", "intent:// 解析开始");
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        webView.stopLoading();

                        PackageManager packageManager = context.getPackageManager();
                        ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        // start activity
                        if (info != null) {
                            // 防止 intent-base 攻击
                            // http://www.mbsd.jp/Whitepaper/IntentScheme.pdf
//                            intent.addCategory("android.intent.category.BROWSABLE");
//                            intent.setComponent(null);
//                            intent.setSelector(null);
                            context.startActivity(intent);
                            return true;
                        }
                        // 如果无法启动intent的Application，把用户重定向到intent中的url网址
                        String fallbackUrl = intent.getStringExtra(BROWSER_FALLBACK_URL);
                        if (fallbackUrl != null) {
                            webView.loadUrl(fallbackUrl);
                            return true;
                        }
                        // 请求用户到应用商店去安装所需的APP
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + intent.getPackage()));
                        if (marketIntent.resolveActivity(packageManager) != null) {
                            context.startActivity(marketIntent);
                            return true;
                        }
                    }
                } catch (URISyntaxException e) {
                    Logger.e("WebViewActivity", "解析" + url + "出错:" + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public void showConsoleMessage(CommonConsoleMessage consoleMessage) {
        if(null == consoleMessage){
            Logger.w(TAG, "showConsoleMessage param is null");
            return;
        }
        switch (consoleMessage.messageLevel()) {
            case TIP:
                // 和LOG 用一样
            case LOG:
                Logger.i("ConsoleMessage level: ", String.valueOf(consoleMessage.messageLevel()));
                Logger.i("ConsoleMessage message: ", consoleMessage.message());
                Logger.i("ConsoleMessage sourceId: ", consoleMessage.sourceId());
                Logger.i("ConsoleMessage lineNumber: ", String.valueOf(consoleMessage.lineNumber()));
                break;
            case WARNING:
                Logger.w("ConsoleMessage level: ", String.valueOf(consoleMessage.messageLevel()));
                Logger.w("ConsoleMessage message: ", consoleMessage.message());
                Logger.w("ConsoleMessage sourceId: ", consoleMessage.sourceId());
                Logger.w("ConsoleMessage lineNumber: ", String.valueOf(consoleMessage.lineNumber()));
                break;
            case ERROR:
                Logger.e("ConsoleMessage level: ", String.valueOf(consoleMessage.messageLevel()));
                Logger.e("ConsoleMessage message: ", consoleMessage.message());
                Logger.e("ConsoleMessage sourceId: ", consoleMessage.sourceId());
                Logger.e("ConsoleMessage lineNumber: ", String.valueOf(consoleMessage.lineNumber()));
                break;
            case DEBUG:
                Logger.d("ConsoleMessage level: ", String.valueOf(consoleMessage.messageLevel()));
                Logger.d("ConsoleMessage message: ", consoleMessage.message());
                Logger.d("ConsoleMessage sourceId: ", consoleMessage.sourceId());
                Logger.d("ConsoleMessage lineNumber: ", String.valueOf(consoleMessage.lineNumber()));
                break;
        }
    }

    @Override
    public boolean isCrushWhenInitX5(String currentModel) {
        if (TextUtils.isEmpty(currentModel) || TextUtils.isEmpty(currentModel.trim())) {
            return false;
        }
        for(String model : WebViewConst.X5_INIT_PREVENT_LIST) {
            if (!TextUtils.isEmpty(model) && TextUtils.equals(model.trim(), currentModel.trim())) {
                Logger.w(TAG, "当前机型为" + currentModel + ", 该机型初始化x5会崩溃, 默认加载系统webkit内核.");
                return true;
            }
        }
        return false;
    }
}
