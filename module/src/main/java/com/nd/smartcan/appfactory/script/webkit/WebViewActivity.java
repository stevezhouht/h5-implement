package com.nd.smartcan.appfactory.script.webkit;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Message;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.sdp.android.apf.h5.R;
import com.nd.sdp.android.common.res.CommonBaseCompatActivity;
import com.nd.sdp.android.common.res.StyleUtils;
import com.nd.sdp.android.common.res.utils.CommonSkinUtils;
import com.nd.smartcan.appfactory.AppFactory;
import com.nd.smartcan.appfactory.Config.IConfigManager;
import com.nd.smartcan.appfactory.Config.dao.AnnounceJsonBean;
import com.nd.smartcan.appfactory.Config.dao.AnnounceJsonBeanOrmDao;
import com.nd.smartcan.appfactory.businessInterface.IContainInterface;
import com.nd.smartcan.appfactory.businessInterface.IExternalWebsiteHandler;
import com.nd.smartcan.appfactory.businessInterface.IMaincomInjectInterface;
import com.nd.smartcan.appfactory.businessInterface.INavigationBarInterface;
import com.nd.smartcan.appfactory.businessInterface.IWebViewMenuItem;
import com.nd.smartcan.appfactory.component.IWebviewBackObserver;
import com.nd.smartcan.appfactory.component.WebviewObserver;
import com.nd.smartcan.appfactory.js.AppFactoryJsInterfaceImp;
import com.nd.smartcan.appfactory.keying.ProtocolConstant;
import com.nd.smartcan.appfactory.nativejs.util.MapScriptable;
import com.nd.smartcan.appfactory.script.common.AdapterJsModule;
import com.nd.smartcan.appfactory.script.common.JsBridgeManager;
import com.nd.smartcan.appfactory.script.common.MenuBean;
import com.nd.smartcan.appfactory.script.hotfix.ILightAppUpdate;
import com.nd.smartcan.appfactory.script.hotfix.Utils.LightUpdateHelper;
import com.nd.smartcan.appfactory.script.hotfix.bean.LightComponent;
import com.nd.smartcan.appfactory.script.webkit.download.HttpDownloadUtil;
import com.nd.smartcan.appfactory.script.webkit.protocolParse.ProtocolParser;
import com.nd.smartcan.appfactory.script.webkit.utils.UrlUtils;
import com.nd.smartcan.appfactory.script.webkit.utils.WebViewConst;
import com.nd.smartcan.appfactory.script.webkit.utils.WebViewUtils;
import com.nd.smartcan.appfactory.utils.ProtocolUtils;
import com.nd.smartcan.appfactory.vm.PageUri;
import com.nd.smartcan.commons.util.logger.Logger;
import com.nd.smartcan.frame.js.IJsModule;
import com.nd.smartcan.frame.js.IMenuRegisterListener;
import com.nd.smartcan.frame.js.INativeContext;
import com.nd.smartcan.frame.js.InvokeDelegate;
import com.nd.smartcan.webview.JsEventCenterManager;
import com.nd.smartcan.webview.WebContainerDelegate;
import com.nd.smartcan.webview.WebViewContainer;
import com.nd.smartcan.webview.WebViewUtil;
import com.nd.smartcan.webview.global.CommonConsoleMessage;
import com.nd.smartcan.webview.outerInterface.IAppFactoryInjectInterface;
import com.nd.smartcan.webview.outerInterface.IHttpRequestInterceptionBean;
import com.nd.smartcan.webview.outerInterface.IMessageFromJsToNative;
import com.nd.smartcan.webview.outerInterface.ISslError;
import com.nd.smartcan.webview.outerInterface.ISslErrorHandler;
import com.nd.smartcan.webview.outerInterface.IWebView;
import com.nd.smartcan.webview.outerInterface.IWebViewExt;
import com.nd.smartcan.webview.outerInterface.IWebViewSettingListener;
import com.nd.smartcan.webview.webinterface.IWebviewBusinessInterface;
import com.nd.smartcan.webview.webinterface.WebviewJsInject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nd.smartcan.appfactory.keying.ProtocolConstant.EXTERNAL_WEBSITE.BLACK_LIST;
import static com.nd.smartcan.appfactory.keying.ProtocolConstant.EXTERNAL_WEBSITE.NORMAL;
import static com.nd.smartcan.appfactory.keying.ProtocolConstant.EXTERNAL_WEBSITE.NOT_EXISTS;


/**
 * 共通的WebView Fragment，需要Activity作为载体来使用
 * Created by cq on 2015/1/29.O
 * <p>
 * 改用作WebView的容器 by @caimk
 *
 * @hide
 */
public final class WebViewActivity extends CommonBaseCompatActivity implements INavigationBarInterface, InvokeDelegate.InvokeHandler, IMenuRegisterListener, IWebViewSettingListener, IAppFactoryInjectInterface, IMessageFromJsToNative, IWebviewBusinessInterface, ILightAppUpdate {

    private static final String TAG = "AppFactoryWebViewActivity";

    private Context mContext;

    private ProgressBar mPb;

    private RelativeLayout mRlVisitException;

    private Button mBtnRetry;

    private IWebView.IWebClient mWebViewCallback;

    private boolean mIsError = false;

    private boolean mIsLoading = false;

    private boolean mIsFirst = true;

    //为了防止webview在全屏时用户重复点击返回键导致js代码重复注入的情况设置的标志位
    private boolean mIsFirstBack = true;

    private TextView mTvVisitError;

    private ImageView mIvVsitException;

    private final int PAGE_NOT_FOUND = 404;

    private String mWantedTitle;

    private RelativeLayout mainContainer;

    private Toolbar mToolbar;
    private View mDivider; // 公共toolbar中有一个1px的分割线。
    private ArrayList<String> mMafMenuIds;
    private ArrayList<String> mBtnMenuIds;
    private List<String> mActivityResultCallbackList;
    private String mProtocolUrl;  // 协议地址: 即带协议的url, 包括cmp, local, online(可能携带参数)
    private String wantLoadUrl;   // 目标地址, 协议地址需要通过转换后才能得到目标地址.
    // 如果该mProtocolUrl可以提取出componentId，保存之
    private String mComponentId;  //
    private int mNavColorText; // 导航栏文字颜
    private int mNavColorBackground; // 导航栏背景色
    private int mNavColorDivider; // 导航栏分割线颜色

    private String mCurrentTitle;

    private String leftButtonStatus = WebViewConst.LEFT_BUTTON_CLOSE;

    private String maf_down_start_event_name_value = null;
    private WebContainerDelegate mDelegate;
    /**
     * “更多”菜单项
     */
    private MenuItem mMoreMenuItem; // 该MenuItem所对应的menuId为_maf_menu
    private boolean isMoreMenuItemVisible = true;

    /**
     * 水平显示的菜单项
     */
    private Map<String, MenuItem> mActionMenus;

    /**
     * 弹出的菜单项
     */
    private Map<String, MenuItem> mPopupMenus;

    private MyHandler mHandler;

    /**
     * 是否显示进度条
     */
    private boolean mIsShowProgressBar;

    /**
     * 当前Activity是否已经进入全屏状态
     */
    private boolean fullScreenFlag = false;

    private static final int IS_NAVIGATION_BAR_VISIBLE = 0;

    private static final int IS_MENU_VISIBLE = 1;
    // 注册菜单项（显示在‘更多’中）
    private static final int REGISTER_MENU_ITEM_VERTICAL = 2;
    // 注册菜单项（显示在‘水平’中）
    private static final int REGISTER_MENU_ITEM_HORIZONTAL = 3;
    // 注册菜单项(显示在‘更多’ 和 ‘水平’ 中)
//    private static final int REGISTER_MENU_ITEM_ALL = 4;
    // 注销菜单项
    private static final int UNREGISTER_MENU = 5;

    private Menu mMenu = null;
    private List<Map<String, String>> regeisterMenuList = new ArrayList<>();

    /**
     * 打开webview时自定义的headers
     */
    private Map<String, String> mCustomHeaders;
    /**
     * 标识是否遇到ssl安全证书错误
     */
    private boolean mIsReceivedSslError = false;

    /**
     * ssl安全证书错误时访问的url
     */
    private String mSslErrorUrl;

    //当前WebViewActivity的回调监听,如果有注册当前URL的WebviewObserver，则toolbar都交由外部控制
    private WebviewObserver mWebviewObserver;

    /**
     * 当前加载的url地址
     */
    private String mCurrentLoadUrl;

    private final int SET_TITLE_ON_RECEIVED_TITLE = 0X01;
    private final int SET_TITLE_ON_LOAD_START = 0X02;

    private final String KEY_CODE = "appFactory.menuItemClickEvent";
    /**
     * 外站警示uuid
     */
    private String mUuid = null;
    // 用来判断当前界面是否是警告页面, 如果是在警告页面按返回键, 则直接退出Activity
    private boolean isRedirect = false;

    private KeyEvent mKeyBackEvent = null;
    private final String KEY_BACK_JS_CODE = "javascript:Bridge.__isJsTakeKeyBack();";

    /**
     * 背景颜色是否透明
     */
    private boolean mIsBgTransparent = false;

    public static final String TEXT_COLOR = "text_color";
    public static final String BACKGROUND_COLOR = "background_color";
    public static final String DIVIDER_COLOR = "divider_color";

    @Override
    protected void initTheme() {
        Intent intent = getIntent();
        if (intent != null) {
            mIsBgTransparent = intent.getBooleanExtra(WebViewConst.MAF_BG_TRANSPARENT, false);
            if (mIsBgTransparent) {
                setTheme(R.style.Theme_AppCompat_Translucent_WebView);
                return;
            }
        }
        super.initTheme();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);//（这个对宿主没什么影响，建议声明）
        mContext = this;
        // begin 为了适配7.0 webview 语言问题，webview初始化前先记住语言信息
        LocaleList localeList = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            localeList = getResources().getConfiguration().getLocales();
        }
        // end

//        mDelegate = new WebContainerDelegate(this); // 只用DefWebView
//        mDelegate = new WebContainerDelegate(this, true); // 优先使用X5
        mDelegate = new WebContainerDelegate(this, WebViewConst.isUseX5);//该值可供QA配置,默认为true,即使用x5
        if (mIsBgTransparent) {
            mDelegate.getWebView().setBackgroundColor(0);
        }

        // 为了适配7.0 webview 语言问题，这段处理必须在webview初始化后，setContentView之前
        // 7.0 中webview初始化后locale会变为系统全局设置，需还原回来
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !getResources().getConfiguration().getLocales().equals(localeList)) {
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocales(localeList);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
        // end
        // 判断当前Activity是否在Tab页面
        if (getParent() instanceof IContainInterface) {
            mDelegate.setActivityInOtherContainer(true);
        }
        // 防止x5闪屏
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        // 下面setContentView使用view注入，不用layoutResID注入。
        // 如果用layout直接注入，在三星的9300和S6上会出现弹出菜单过大的情况。原因不明
        final LayoutInflater themeInflater = StyleUtils.getThemeInflater(this, R.style.CommonBase_Theme);
        final View inflate = themeInflater.inflate(R.layout.webcomponent_webview_activity, null);
        setContentView(inflate);
        mActivityResultCallbackList = new ArrayList<>();
        initInvokeDelegate();
        initWebviewObserver();
        initView();
        initData();
        initEvent();
        mHandler = new MyHandler(this);
        // 需要对轻应用更新管理器进行计数，用于处理轻应用的更新逻辑
        if (mComponentId != null) {
            LightUpdateHelper.onCreate(mComponentId, LightComponent.HTML);
        }
    }

    /**
     * 初始化Webview观察者
     */
    private void initWebviewObserver() {
        if (!initUrl()) {
            return;
        }
        String strUrl = mProtocolUrl;
        int i = mProtocolUrl.indexOf("?");
        if (i > 0) {//只取网址，不取属性
            strUrl = mProtocolUrl.substring(0, i);
        }

        mWebviewObserver = JsBridgeManager.getInstance().getWebviewObserver(strUrl);
        if (mWebviewObserver != null) {
            if (!userDefaultBack(wantLoadUrl)) {
                mToolbar.setContentInsetsRelative(0, 0);//将toolbar边距置空
                mToolbar.setContentInsetsAbsolute(0, 0);
            }
            mWebviewObserver.onCreate(this, mToolbar, wantLoadUrl);
        }
    }

    //mToolbar初始化、Url的获取从initData()抽离
    private boolean initUrl() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDivider = findViewById(R.id.toolbar_divider);
        Intent intent = getIntent();
        if (null != intent) {
            mProtocolUrl = getIntent().getStringExtra(WebViewConst.WANT_LOAD_URL);

            if (TextUtils.isEmpty(mProtocolUrl)) {
                Toast.makeText(this, R.string.appfactory_webview_webpage_not_available, Toast.LENGTH_SHORT).show();
                return false;
            }

            // 提取componentId : 只有local打头的才能提取componentId
            PageUri pageUri = new PageUri(mProtocolUrl);
            mComponentId = pageUri.getPlugin();
            wantLoadUrl = getRealUrl(mProtocolUrl);
        }
        return true;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        // 重写该方法是为了换肤时去掉toolbar 上1px 的divider。在父类中处理了
        super.onPostCreate(savedInstanceState);
    }

    private void initInvokeDelegate() {
        InvokeDelegate.getInstance().addInvokeHandler(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDelegate.onActivityResult(requestCode, resultCode, data);
        for (String eventName : mActivityResultCallbackList) {
            MapScriptable param = new MapScriptable();
            param.put(WebViewConst.DATA, data);
            param.put(WebViewConst.REQUEST_CODE, requestCode);
            param.put(WebViewConst.RESULT_CODE, resultCode);
            AppFactory.instance().triggerEvent(this, eventName, param);
        }
    }

    @Override
    public void onPause() {
        mDelegate.onActivityPause();
        if (mWebviewObserver != null) {
            mWebviewObserver.onPause(this, mToolbar, wantLoadUrl);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        //X5在进入全屏时会自动将屏幕切换为竖屏,为了防止这时候跳转到其他页面,依然处于横屏状态,添加这行代码
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 如果该Activity在Tab页面,除非应用重启，否则没有机会执行到onCreate和onDestroy
        if (mComponentId != null) {
            LightUpdateHelper.onResume(this, this, mComponentId, LightComponent.HTML);
        }

        if (mIsFirst) {
            mIsFirst = false;
        }
        mDelegate.onActivityResume();
        if (mWebviewObserver != null) {
            mWebviewObserver.onResume(this, mToolbar, wantLoadUrl);
        }
        super.onResume();
    }

    @Override
    public String getWantLoadLightUrl() {
        return getRealUrl(mProtocolUrl);
    }

    @Override
    public String getCurrentLightUrl() {
        return wantLoadUrl;
    }

    @Override
    public void reload(String url) {
        wantLoadUrl = url;
        mDelegate.getWebView().loadUrl(url);
    }

    @Override
    public void onDestroy() {
        try {
            Logger.i(TAG, "WebViewActivity  onDestroy 被调用--------------------------------");
            JsEventCenterManager.getInstance().unRegisterEventCenter(mDelegate.getJsEventCenter());
            InvokeDelegate.getInstance().removeInvokeHandler(this);
            mWebViewCallback = null;
            mRlVisitException = null;
            mBtnRetry = null;
            mPb = null;
            mTvVisitError = null;
            mIvVsitException = null;
            mMoreMenuItem = null;
            mActionMenus = null;
            mPopupMenus = null;
            mToolbar = null;
            mDivider = null;
            // 销毁注销的菜单项
            for (Map<String, String> map : regeisterMenuList) {
                JsBridgeManager.getInstance().unRegiesterMenu("WebViewActivity", map.get(WebViewConst.KEY_MENU_ID));
            }

            if (mActivityResultCallbackList != null) {
                mActivityResultCallbackList.clear();
            }
            if (mWebviewObserver != null) {
                mWebviewObserver.onDestory(this, mToolbar, wantLoadUrl);
            }
            // 需要对轻应用更新管理器进行计数，用于处理轻应用的更新逻辑
            if (mComponentId != null) {
                LightUpdateHelper.onDestroy(mComponentId, LightComponent.HTML);
            }
            // 清空外站警告的无用内存
            IExternalWebsiteHandler handler = AppFactory.instance().getExternalWebsiteHandler();
            if (handler != null) {
                handler.destroy();
            }
            // 注销webView实例, 需要先从父节点remove掉webView的view
            mainContainer.removeView(mDelegate.getView());
            mainContainer = null;
            mDelegate.onActivityDestory();
            mDelegate = null;
            mContext = null;
            mKeyBackEvent = null;
            Logger.i(TAG, "WebViewActivity destroy resource completely.");
        } catch (Exception e) {
            Logger.w(TAG, "onDestroy() 错误，不影响使用" + e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // BugFix: 解决x5在Tab页竖屏的全屏播放, 然后切换tab引起footer移位的bug
        // 分析: 该问题是由x5的播放View为SurfaceView, 全屏后statusBar被隐藏. 切换tab后又出现statusBar
        //         整个内容被往下挤了statusBar的高度, 导致footer文字标签不可见
        // 解决:  在一级页面的时候不支持横屏
        if (getParent() instanceof IContainInterface) {
            if (this.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                    this.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                Logger.i(TAG, "不在一级界面显示x5横屏播放视频");
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Toast.makeText(this, R.string.appfactory_webview_not_allow_full_screen, Toast.LENGTH_SHORT).show();
                // 模拟框架返回按键事件
                this.getParent().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                this.getParent().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            }
        }
    }

    /**
     * 开启观察者时，是否要使用默认的返回键功能
     *
     * @return
     */
    private boolean userDefaultBack(String url) {
        return mWebviewObserver != null &&
                (mWebviewObserver instanceof IWebviewBackObserver &&
                        ((IWebviewBackObserver) mWebviewObserver).isUserDefaultBack(url));
    }

    private void initData() {
        if (mWebviewObserver == null) {
            IWebViewMenuItem defaultMenu = JsBridgeManager.getInstance().getExtendMenu(WebViewConst.MENU_COPY);
            if (defaultMenu == null) {
                WebViewUtils.registerDefaultMenu(this);
            }
        }
        //getIntent()要非空判断
        Intent intent = getIntent();
        if (null != intent) {
//            mProtocolUrl = getIntent().getStringExtra(WebViewConst.WANT_LOAD_URL);
//            wantLoadUrl = getRealUrl(mProtocolUrl);
            // 解析 自定义webview url 请求头部  JsonObject --> map
            mCustomHeaders = WebViewActivityHelper.getCustomHeaders(
                    getIntent().getStringExtra(WebViewConst.KEY_ADDITIONAL_HTTP_HEADERS));
            List<IJsModule> jsbridges = JsBridgeManager.getInstance().getJsBridge();
            mIsShowProgressBar = getIntent().getBooleanExtra(WebViewConst.MAF_SHOW_PROGRESS_BAR, true);
            maf_down_start_event_name_value = getIntent().getStringExtra(WebViewConst.maf_down_start_event_name);
            leftButtonStatus = getIntent().getStringExtra(WebViewConst.LEFT_BUTTON);
            if (mWebviewObserver == null) {
                mMafMenuIds = getIntent().getStringArrayListExtra(WebViewConst.MENU_IDS);
                mBtnMenuIds = getIntent().getStringArrayListExtra(WebViewConst.maf_btn_ids);
                mWantedTitle = getIntent().getStringExtra(WebViewConst.WEBVIEW_TITLE);
                String navTextcolor = getIntent().getStringExtra(WebViewConst.MAF_NAVIGATIONBAR_TEXTCOLOR);
                String navBackgroundcolor = getIntent().getStringExtra(WebViewConst.MAF_NAVIGATIONBAR_BACKGROUNDCOLOR);
                String navStatusbarTextcolor = getIntent().getStringExtra(WebViewConst.MAF_STATUSBAR_TEXTCOLOR);
                boolean isShowNavigationBar = getIntent().getBooleanExtra(WebViewConst.MAF_SHOW_NAVIGATIONBAR, true);
                mToolbar.setVisibility(isShowNavigationBar ? View.VISIBLE : View.GONE);
                setNavigationBarBack(leftButtonStatus);
                //设置页面的标题
                if (!TextUtils.isEmpty(mWantedTitle)) {
                    getSupportActionBar().setTitle(mWantedTitle);
                    mCurrentTitle = mWantedTitle;
                } else {
                    getSupportActionBar().setTitle("");
                    mCurrentTitle = "";
                }
                getColorforNavigationBar(navTextcolor, navBackgroundcolor, navStatusbarTextcolor);

            } else {
                if (userDefaultBack(wantLoadUrl)) {
                    setNavigationBarBack(leftButtonStatus);
                } else {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                }

            }
            //设置js调用方法
            if (null != jsbridges) {
                AdapterJsModule adapterJsModule = null;
                for (IJsModule element : jsbridges) {
                    if (element == null) {
                        continue;
                    }
                    //为了兼容旧的机制。
                    if (element instanceof AdapterJsModule) {
                        adapterJsModule = (AdapterJsModule) element;
                        mDelegate.injectToJs(adapterJsModule.getEntryName(), adapterJsModule.getWantRegisterModule());
                    } else {
                        mDelegate.injectToJs(element.getEntryName(), element);
                    }
                }
            }

            // 注入类的处理
            Set<String> set = JsBridgeManager.getInstance().getRegisteredJsClassKeys();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String myName = it.next();
                String myClassName = JsBridgeManager.getInstance().getRegisteredJsClassName(myName);
                mDelegate.injectToJs(myName, myClassName);
            }
            //加载网页
            if (!TextUtils.isEmpty(wantLoadUrl)) {
                Logger.i(TAG, "WebViewActivity加载的网页url: " + wantLoadUrl);
                if (mCustomHeaders == null || mCustomHeaders.isEmpty()) {
                    loadUrl(wantLoadUrl);
                } else {
                    loadUrl(wantLoadUrl, mCustomHeaders);
                }
            }
        }
    }

    private void setNavigationBarBack(String leftButtonStatus) {
        if (TextUtils.isEmpty(leftButtonStatus)) {
            // 默认是 close
            leftButtonStatus = WebViewConst.LEFT_BUTTON_CLOSE;
        }
        if (getSupportActionBar() != null) {
            if (WebViewConst.LEFT_BUTTON_CLOSE.equals(leftButtonStatus)) {
                // close 始终显示左上角返回键
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            if (WebViewConst.LEFT_BUTTON_NONE.equals(leftButtonStatus)) {
                // none 始终不显示左上角返回键
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            if (WebViewConst.LEFT_BUTTON_BACK.equals(leftButtonStatus)) {
                // back 只有在Tab页且无历史记录的时候不显示返回键, 其余情况都显示
                setActionBarBackButtonVisible(true, true);
            }
        }
    }

    /**
     * 获取真正的地址. (因为传入的url可能是 local/online 等框架提供的协议地址, 需要转换成真正在http或者本地的路径)
     *
     * @param protocolUrl 协议地址
     * @return 目标地址
     */
    private String getRealUrl(String protocolUrl) {
        return WebViewUtils.dealWithMoreInfo(new ProtocolParser(this, protocolUrl).getUrl());
    }

    private void initView() {
        // 动态换肤支持(标题栏右上角菜单项较为特殊，需代码设置)
        if (mWebviewObserver == null) {
            Drawable drawable = CommonSkinUtils.getDrawable(this, R.drawable.general_top_icon_back_android);
            mToolbar.setNavigationIcon(drawable);
            setSupportActionBar(mToolbar);
        } else {
            setSupportActionBar(mToolbar);
        }

        mainContainer = (RelativeLayout) findViewById(R.id.wb_content);
        // 给wrapper设置消息派发器
        mDelegate.setDispatcher(AppFactory.instance().getAppFactoryEventManger());
        mainContainer.addView(mDelegate.getView(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // js 中一个对象一定要在mDelegate.getView() 才会初始化
        // 所以要把这个事件初始化放到 mDelegate.getView()后面
        JsEventCenterManager.getInstance().registerEventeCenter(mDelegate.getJsEventCenter());

        mPb = (ProgressBar) findViewById(R.id.pb);
        mRlVisitException = (RelativeLayout) findViewById(R.id.rl_exception);
        mTvVisitError = (TextView) findViewById(R.id.tv_visit_error);
        mBtnRetry = (Button) findViewById(R.id.btn_retry);
        mIvVsitException = (ImageView) findViewById(R.id.iv_visitException);
        mWebViewCallback = new AppFactoryWebViewCallback();
        mDelegate.setWebClient(mWebViewCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mWebviewObserver != null) {
            return super.onCreateOptionsMenu(menu);
        }
        mMenu = menu;
        createHorBtnMenu(menu);
        if (null == mMafMenuIds) {//不传任何_maf_menu_ids时会显示默认右键菜单
            mMafMenuIds = new ArrayList<>();
            mMafMenuIds.add(WebViewConst.MENU_COPY);
            mMafMenuIds.add(WebViewConst.MENU_OPEN_WITH_BROWSER);
            //mMafMenuIds.add(WebViewConst.MENU_SET_FONT);
            mMafMenuIds.add(WebViewConst.MENU_REFRESH);
            createMenus(menu, mMafMenuIds);
        } else {
            int size = mMafMenuIds.size();
            if (1 == size) {//在goPage一个html页面的时候，在最后的参数列表里加上参数对_maf_menu_ids=none即可
                if (TextUtils.equals(mMafMenuIds.get(0), WebViewConst.LEFT_BUTTON_NONE)) {
                    return super.onCreateOptionsMenu(menu);
                }
                //显示所有：在goPage一个html页面的时候，在最后的参数列表里加上参数对_maf_menu_ids=all即可（不支持排列顺序）
                if (TextUtils.equals(mMafMenuIds.get(0), WebViewConst.MENU_ALL)) {
                    mMafMenuIds = JsBridgeManager.getInstance().getAllExtendMenuIds();
                }
            }
            createMenus(menu, mMafMenuIds);
        }
        setNavMenuColor();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<String> menuIds = new ArrayList<>();
        if (null != mMafMenuIds) {
            menuIds.addAll(mMafMenuIds);
        }
        if (null != mBtnMenuIds) {
            menuIds.addAll(mBtnMenuIds);
        }
        processMenuSelected(item, menuIds);
        return super.onOptionsItemSelected(item);
    }

    private void initEvent() {
        mBtnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFinishing()) {
                    return;
                }
                if (TextUtils.equals(mBtnRetry.getText(), getString(R.string.appfactory_webview_retry))) {
                    if (!TextUtils.isEmpty(mDelegate.getWebView().getUrl())) {
                        mIsError = false;
                        mIsLoading = true;
                        String urlFromDelegate = mDelegate.getWebView().getUrl();
                        // 失败重试时 mCurrentLoadUrl 与 webview 获取的url相同时 ， 添加header
                        if (TextUtils.equals(mCurrentLoadUrl, urlFromDelegate) &&
                                mCustomHeaders != null && mCustomHeaders.isEmpty()) {
                            loadUrl(urlFromDelegate, mCustomHeaders);
                        } else {
                            loadUrl(urlFromDelegate);
                        }
                    }
                } else {
                    finish();
                }

            }
        });

    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Logger.d(TAG,"onKeyUp：Webview返回键被按下");
            if(mDelegate.getWebView().hasInjectBridge()) {//如果使用JsBridge.js才有询问的可能
                Logger.d(TAG,"onKeyUp：当前页面包含JsBridge.js,可以询问");
                mKeyBackEvent = event;//保存KeyEvent用于异步执行
                mDelegate.getWebView().evaluateJavascript(KEY_BACK_JS_CODE, null);
                return true;
            }else{
                Logger.d(TAG,"onKeyUp：当前页面不包含JsBridge.js,直接处理返回键");
                if(processNativeKeyBack()){
                    return true;
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 原生处理返回键
     * @return
     */
    private boolean processNativeKeyBack(){
        //使用 js 代码进入全屏的网页，不支持点击back键退出，所以我们在用户点击back键的时候，让webview去执行一段js代码
        // 用来模拟用户点击了网页上的退出全屏按键。
        if (fullScreenFlag && mIsFirstBack) {
            mIsFirstBack = false;
            String currentUrl = mDelegate.getWebView().getUrl();
            String jsCode = WebviewJsInject.getJsCode(currentUrl, WebviewJsInject.EXIT_FULLSCREEN);
            if (jsCode != null) {
                Logger.i(TAG, "inject js code ==== " + jsCode);
                mDelegate.getWebView().evaluateJavascript(jsCode, null);
                //这里返回ture实际上截断了back按键的调用链，退出全屏以后不会接着退回上一级web页面
                return true;
            }
        }

        return goBack();
    }

    private boolean goBack() {
        if (mDelegate.getWebView() != null && mDelegate.getWebView().canGoBack() && !isRedirect) {
            mDelegate.getWebView().goBack();
            Logger.w(TAG, "-------------goBack just go back");
            return true;
        } else {
            // 如果是在Tab页的话，这里就不直接finish
            Activity parent = this.getParent();
            if (null != parent && parent instanceof IContainInterface) {
                // 该Activity被放在tab页中展示
                Logger.w(TAG, "-------------goBack detected is in TAB");
                return false;
            } else {
                // 该Activity在二级页面，直接finish
                Logger.w(TAG, "-------------goBack finish");
                finish();
                return true;
            }
        }
    }

    private void setLeftButtonVisible() {
        if (mWebviewObserver != null &&!userDefaultBack(wantLoadUrl) && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            return;
        }
        if (WebViewConst.LEFT_BUTTON_BACK.equals(leftButtonStatus)) {
            boolean canGoBack = mDelegate.getWebView().canGoBack();
            setActionBarBackButtonVisible(false, canGoBack);
        }
    }

    /**
     * 当左边上角按钮(ActionBar的回退键) 风格设置为back时, 根据传入的参数决定是否显示回退键.
     *
     * @param isInit  是否是初始化过程中调用的.
     *                如果true, 则在首页无历史时,不显示回退键,
     *                如果false, 则交给isShown控制
     * @param isShown 是否显示回退键
     */
    private void setActionBarBackButtonVisible(boolean isInit, boolean isShown) {
        Activity parent = this.getParent();
        if (getSupportActionBar() != null) {
            if (null != parent && parent instanceof IContainInterface) {
                if (isInit) {
                    // 如果是初始化过程中, 且出现在首页,则不显示回退键
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(isShown);
                }
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void setNavigationBar(boolean visible) {
        if (mToolbar != null) {
            Message msg = new Message();
            msg.what = IS_NAVIGATION_BAR_VISIBLE;
            msg.obj = visible;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void setMenuVisible(JSONObject param) {
        if (param != null) {
            Message msg = new Message();
            msg.what = IS_MENU_VISIBLE;
            msg.obj = param;
            mHandler.sendMessage(msg);
        }
    }

    /**
     * js调用方法设置NavigationBar子控件颜色
     */
    public void setNavigationBarAppearance(JSONObject param) {
        //导航栏上正常态文字的颜色
        mNavColorText = param.optInt(TEXT_COLOR);
        mNavColorBackground = param.optInt(BACKGROUND_COLOR);
        mNavColorDivider = param.optInt(DIVIDER_COLOR);
        setNavigationBarAppearance();
        setNavMenuColor();
    }

    /**
     * 获取字符串的颜色值并做转换
     *
     * @param textColor          标题颜色
     * @param backgroundColor    背景颜色
     * @param statusbarTextColor 状态栏
     */
    public void getColorforNavigationBar(String textColor, String backgroundColor, String statusbarTextColor) {
        String colorHead = "#";

        //导航栏上正常态文字的颜色
        try {
            if (!TextUtils.isEmpty(textColor)) {
                mNavColorText = Color.parseColor(colorHead + textColor);
            }
        } catch (IllegalArgumentException e) {
            Logger.w(TAG, e.getMessage());
        }

        //导航栏背景颜色
        try {
            if (!TextUtils.isEmpty(backgroundColor)) {
                mNavColorBackground = Color.parseColor(colorHead + backgroundColor);
            }
        } catch (IllegalArgumentException e) {
            Logger.w(TAG, e.getMessage());
        }
        //状态栏（信号、时间条）文本颜色
        if (!TextUtils.isEmpty(statusbarTextColor)) {
            //只对iOS设备有效且,暂且保留
        }
        setNavigationBarAppearance();

    }

    /**
     * 设置NavigationBar子控件颜色
     */
    private void setNavigationBarAppearance() {
        if (mNavColorText != 0 && mToolbar != null) {
            //设置标题颜色
            mToolbar.setTitleTextColor(mNavColorText);
            //设置导航栏图片颜色
            if (mToolbar.getNavigationIcon() != null) {
                Drawable navDrawable = DrawableCompat.wrap(mToolbar.getNavigationIcon());
                if (navDrawable != null) {
                    DrawableCompat.setTint(navDrawable, mNavColorText);
                    mToolbar.setNavigationIcon(navDrawable);
                    //ActionMenu图片颜色需要在onCreateOptionsMenu方法中设置
                }
            }
        }

        //导航栏背景颜色
        if (mNavColorBackground != 0 && mToolbar != null) {
            mToolbar.setBackgroundColor(mNavColorBackground);
        }

        // 设置divider颜色
        if (mNavColorDivider != 0 && mDivider != null) {
            mDivider.setBackgroundColor(mNavColorDivider);
        }

    }

    /**
     * 设置ActionMenu图片颜色
     */
    private void setNavMenuColor() {
        if (mNavColorText != 0 && mToolbar != null) {
            Menu menu = mToolbar.getMenu();
            if (menu != null && menu.size() > 0) {
                int size = menu.size();
                for (int i = 0; i < size; i++) {
                    MenuItem item = menu.getItem(i);
                    Drawable itemDrawable = DrawableCompat.wrap(item.getIcon());
                    if (itemDrawable != null) {
                        DrawableCompat.setTint(itemDrawable, mNavColorText);
                        item.setIcon(itemDrawable);
                    }
                }
            }
        }
    }

    @Override
    public String getNavigationBarMenus() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        // 水平菜单项
        if (mActionMenus != null && mActionMenus.size() > 0) {
            for (Map.Entry<String, MenuItem> entry : mActionMenus.entrySet()) {
                String menuId = entry.getKey();
                MenuItem menuItem = entry.getValue();
                sb.append("\"" + menuId + "\"" + ":" + menuItem.isVisible() + ",");
            }
        }
        // 弹出菜单项
        if (mPopupMenus != null && mPopupMenus.size() > 0) {
            for (Map.Entry<String, MenuItem> entry : mPopupMenus.entrySet()) {
                String menuId = entry.getKey();
                MenuItem menuItem = entry.getValue();
                sb.append("\"" + menuId + "\"" + ":" + menuItem.isVisible() + ",");
            }
        }
        // 更多菜单项
        if (mMoreMenuItem != null) {
            sb.append("\"" + WebViewConst.MENU_MORE + "\"" + ":" + mMoreMenuItem.isVisible() + ",");
        }

        if (sb.length() > 2) {
            // 去掉逗号
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void handleInvoke(INativeContext iNativeContext) {
        if (iNativeContext.getContext().equals(this)) {
            String h5DataPath = mDelegate.getWebView().getUrl();
            iNativeContext.putContextObject(WebViewConst.DATA_PATH, WebViewUtils.getH5DataDir(h5DataPath, mContext));
            // 因为JsModule方法的调用者可能为WebViewActivity，也可能是ReactActivity，此处设置一个type，用来区分是react还是H5
            iNativeContext.putContextObject(WebViewConst.CONTAINER_TYPE, WebViewConst.CONTAINER_H5);
            IConfigManager configManager = AppFactory.instance().getConfigManager();
            if (configManager != null) {
                String id = configManager.getComIdByHost(wantLoadUrl);
                iNativeContext.putContextObject(WebViewConst.COMPONENT_ID, id);
            } else {
                Logger.w(TAG, "发现 AppFactory.instance().getConfigManager() 返回是 null");
            }
        }
    }

    @Override
    public boolean registerMenu(Map<String, String> map) {
        if (map != null) {
            String menuId = map.get(WebViewConst.KEY_MENU_ID);
            // 加入list，退出时便于销毁。
            regeisterMenuList.add(map);

            // 判断是注册到‘更多’菜单中，还是注册到‘更多’和水平菜单中。
            // 默认注册到更多菜单中，只有当参数为true时，才会同时注册到水平菜单中。
            String location = map.get("location");
            Set<String> set = AppFactoryJsInterfaceImp.analyzeLocationArr(location);
            for (String temp : set) {
                switch (temp.trim()) {
                    case AppFactoryJsInterfaceImp.IN:
                        triggerHandler(REGISTER_MENU_ITEM_VERTICAL, menuId);
                        break;
                    case AppFactoryJsInterfaceImp.OUT:
                        triggerHandler(REGISTER_MENU_ITEM_HORIZONTAL, menuId);
                        break;
                    default:
                        Logger.w(TAG, "注册菜单传入的位置信息为空");
                        break;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    private void triggerHandler(int type, String menuId) {
        Message msg = new Message();
        msg.what = type;
        msg.obj = menuId;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean unRegisterMenu(String id) {
        if (null == id || id.trim().length() == 0) {
            return false;
        } else {
            Message msg = new Message();
            msg.what = UNREGISTER_MENU;
            msg.obj = id;
            mHandler.sendMessage(msg);
            return true;
        }
    }

    @Override
    public void setWebViewSetting(boolean isVerticalScrollBarEnabled) {
        IWebView iWebView = mDelegate.getWebView();
        iWebView.setWebViewSetting(isVerticalScrollBarEnabled);
    }

    @Override
    public boolean getWebViewSetting() {
        IWebView iWebView = mDelegate.getWebView();
        return iWebView.getWebViewSetting();
    }

    @Override
    public String goPage(INativeContext context, JSONObject param) {
        return new AppFactoryJsInterfaceImp().goPage(context, param);
    }

    @Override
    public void goPageForResult(INativeContext context, JSONObject param) {
        new AppFactoryJsInterfaceImp().goPageForResult(context, param);
    }

    @Override
    public View getWebViewInstance() {
        return mDelegate.getWebView().getView();
    }

    @Override
    public void onInjectSuccess() {
        JsBridgeManager.getInstance().onInjectSuccess(
                AppFactory.instance().getApplicationContext(),
                mComponentId,
                LightComponent.HTML,
                mProtocolUrl,
                new JsBridgeManager.OnBridgeReady() {
                    @Override
                    public void ready() {
                        // 此处注入Bridge , 当NativeInterface注入方式改变的时候，uncomment the two lines below
//                String injectBridge = WebViewConst.INJECT_BRIDGE;
//                mDelegate.getWebView().evaluateJavascript(injectBridge);
                    }
                });
    }

    public void setComponentId(String componentId) {
        this.mComponentId = componentId;
    }

    @Override
    public Activity getActivity() {
        return WebViewActivity.this;
    }

    @Override
    public void enterFullScreen() {
        mIsFirstBack = true;
        Activity activity = WebViewActivity.this;
        Logger.i(TAG, "enterFullScreen:======================== 进入全屏 ");
        if (activity == null) {
            return;
        }
        //设置屏幕方向为横屏
//        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //从AppFactory中获取toolBar和toolBarDivider，在进入全屏时手动隐藏
        fullScreenFlag = true;
        if (mToolbar != null && mToolbar.getVisibility() == View.VISIBLE) {
            WebViewUtil.setViewVisibility(activity, mToolbar, View.GONE);
        }

        if (mDivider != null && mDivider.getVisibility() == View.VISIBLE) {
            WebViewUtil.setViewVisibility(activity, mDivider, View.GONE);
        }

        //从Maincomponent中获取tabBar（底部页签栏），在进入全屏时手动隐藏
        Activity parent = activity.getParent();
        if (parent == null) {
            return;
        }
        if (parent instanceof IMaincomInjectInterface) {
            final View tabBar = ((IMaincomInjectInterface) parent).getTabBar();
            if (tabBar != null && tabBar.getVisibility() == View.VISIBLE) {
                WebViewUtil.setViewVisibility(activity, tabBar, View.GONE);
            }
        }
    }

    @Override
    public void exitFullScreen() {
        Activity activity = WebViewActivity.this;
        Logger.i(TAG, "enterFullScreen:======================== 进入全屏 ");
        if (activity == null) {
            return;
        }
        //设置屏幕方向为竖屏
//        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //从AppFactory中获取toolBar和toolBarDivider，在进入全屏时手动隐藏

        if (mToolbar != null && mToolbar.getVisibility() == View.GONE) {
            WebViewUtil.setViewVisibility(activity, mToolbar, View.VISIBLE);
        }

        if (mDivider != null && mDivider.getVisibility() == View.GONE) {
            WebViewUtil.setViewVisibility(activity, mDivider, View.VISIBLE);
        }
        //从Maincomponent中获取tabBar（底部页签栏），在进入全屏时手动隐藏
        Activity parent = activity.getParent();
        if (parent == null) {
            return;
        }
        if (parent instanceof IMaincomInjectInterface) {
            View tabBar = ((IMaincomInjectInterface) parent).getTabBar();
            if (tabBar != null && tabBar.getVisibility() == View.GONE) {
                WebViewUtil.setViewVisibility(activity, tabBar, View.VISIBLE);
            }
        }
        fullScreenFlag = false;
    }

    @Override
    public boolean isFullScreen() {
        return fullScreenFlag;
    }



    class AppFactoryWebViewCallback implements IWebView.IWebClient {

        private static final String CBTAG = "WebViewActivityCB";
        private String openUrl = null;

        @Override
        public void onReceivedTitle(String title) {
            Logger.v(CBTAG, "onReceivedTitle");

            //  应该抽出一个函数，如果在onLoadstart中调用，会不会执行两次，是否有问题。
            if (mWebviewObserver != null) {
                return;
            }
            setWebviewTitle(title, SET_TITLE_ON_RECEIVED_TITLE);
        }

        @Override
        public void onReceivedFavicon(Bitmap icon) {
            Logger.v(CBTAG, "onReceivedFavicon");
            if (mWebviewObserver != null) {
                return;
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(String url) {
            Logger.v(CBTAG, "shouldOverrideUrlLoading");
            if (!TextUtils.isEmpty(url)) {
                openUrl = url;
            }
            return WebViewUtils.shouldOverrideUrlLoading(mContext, mDelegate.getWebView(), url);
        }

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                    String mimetype, long contentLength) {
            Logger.v(CBTAG, "onDownloadStart");
            boolean isshouldsend = !TextUtils.isEmpty(maf_down_start_event_name_value);
            Logger.w(TAG,
                    "onDownloadStart the url is " + url + " should send result  " + isshouldsend
                            + " event name is " + maf_down_start_event_name_value);

            if (isshouldsend) {//使用其他组件实现的下载
                MapScriptable param = new MapScriptable();
                param.put(WebViewConst.key_down_start_url, url);
                param.put(WebViewConst.key_down_start_user_agent, userAgent);
                param.put(WebViewConst.key_down_start_content_disposition, contentDisposition);
                param.put(WebViewConst.key_down_start_mimetype, mimetype);
                param.put(WebViewConst.key_down_start_content_length, contentLength);
                AppFactory.instance().triggerEventAsync(mContext.getApplicationContext(), maf_down_start_event_name_value, param);
            } else {//使用应用工厂webview提供的http下载
                Logger.w(TAG, "use appfactory download method");
                HttpDownloadUtil.download(mContext.getApplicationContext(), url, contentDisposition, mimetype);
            }
        }

        @Override
        public void onLoadStared(String newUrl) {
            Logger.i(TAG, "webviewactivit onLoadStared newUrl == " + newUrl);
            //保存当前加载页面的Url地址
            mCurrentLoadUrl = newUrl;

            //第一次打开页面，和打开页面回退的时候 onReciverTitle在4.4以下机型不会被调用所以再这边调用。
            if (mWebviewObserver == null && newUrl != null && newUrl.equals(wantLoadUrl)) {
                setWebviewTitle(null, SET_TITLE_ON_LOAD_START);
            }

            Logger.v(CBTAG, "onDownloadStart");
            if (mPb != null && mIsShowProgressBar) {
                if (mPb.getVisibility() != View.VISIBLE) {
                    mPb.setVisibility(View.VISIBLE);
                }
                mPb.setProgress(0);
            }
            if (mWebviewObserver == null) {
                showMoreMenuItem(0);
//                showMenuItem(mMoreMenuItem, false);
                showMenus(mActionMenus, false);
            }
            if (mWebviewObserver != null) {
                String oldurl = mDelegate.getWebView().getUrl();
                if (oldurl != null && !oldurl.equalsIgnoreCase(newUrl)) {
                    mWebviewObserver.onUrlChange(WebViewActivity.this, oldurl, newUrl);
                }

            }

            // 另起其他页面时，将该标识位重置为false
            mIsReceivedSslError = false;
            mIsError = false;

            /********* 外站警示 **************/
            handleExternalWebsite(newUrl);
            /******************************/
        }

        @Override
        public boolean onLoadSuccess() {
            Logger.v(CBTAG, "onLoadSuccess");

            if (openUrl != null && !openUrl.equals(wantLoadUrl)) {
                setLeftButtonVisible();
            }
            if (mWebviewObserver == null) {
                showMoreMenuItem();
//                showMenuItem(mMoreMenuItem, true);
                showMenus(mActionMenus, true);
            }


            mIsLoading = false;
            if (!mIsError) {
                loadSuccess();
                return true;
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                mIsError = false; // 适配问题，4.0 - 4.4 出错后，onPageFinished是其最后一个回调函数
            }
            return false;
        }

        @Override
        public void onLoadFail(String url, int errorCode) {
            Logger.v(CBTAG, "onLoadFail");

            if (openUrl != null && !openUrl.equals(wantLoadUrl)) {
                setLeftButtonVisible();
            }
            if (mWebviewObserver == null) {
                showMoreMenuItem();
//                showMenuItem(mMoreMenuItem, true);
                showMenus(mActionMenus, true);
            }

            if (url == null || !url.equals(wantLoadUrl)) {
                // 只打印日志，不做任何处理
                Logger.w(TAG, "onLoadFail, wantLoadUrl : " + wantLoadUrl);
                Logger.w(TAG, "onLoadFail, url : " + url);
                return;
            }

            loadFail(errorCode);


        }

        @Override
        public void isDoLoading(int progress) {
            Logger.v(CBTAG, "isDoLoading progress " + progress);
            if (isFinishing()) {
                Logger.v(CBTAG, "isFinishing ...");
                if (mWebviewObserver == null) {
                    showMoreMenuItem();
//                    showMenuItem(mMoreMenuItem, true);
                    showMenus(mActionMenus, true);
                }
                return;
            }
            if (mPb != null && mIsShowProgressBar) {
                if (progress < 100) {
                    if (mPb.getVisibility() != View.VISIBLE) {
                        mPb.setVisibility(View.VISIBLE);
                    }
                    mPb.setProgress(progress);
                } else {
                    mPb.setVisibility(View.GONE);
                }
            }
            if (mWebviewObserver == null) {
                boolean visible = progress >= 100;
                showMoreMenuItem(progress);
//                showMenuItem(mMoreMenuItem, visible);
                showMenus(mActionMenus, visible);
            }
        }

        @Override
        public void onLoadResource(String resourceUrl) {
            // 旧的逻辑加载离线文件的时候需要判断合法性。新的轻应用逻辑只在下载时做合法性判断
//            handleLightAppRun(resourceUrl);
        }


        @Override
        public void onReceivedSslError(String url, final ISslErrorHandler handler, ISslError iSslError) {
            mIsReceivedSslError = true;
            mSslErrorUrl = url;
            boolean isSpecial = WebViewUtils.isSpecialWebViewCore(WebViewActivity.this, iSslError);
            if (isSpecial) {
                // 如果检测到是Chromium 存在bug的版本，则直接执行
                handleError(handler);
                mIsError = false;
                mIsReceivedSslError = false;
            } else {
                // 安全证书错误，通过弹dialog的方式来让用户决定是否加载当前网页
                // Activity和内部js运行销毁是有一个过程，当当前activity处于销毁过程，如果这时候回调，就不要处理了（以前有出现类似webview和js方法异步调用）
                if (!isFinishing()) {
                    showSecurityWarningDialog(url, handler, iSslError);
                }
            }
        }

        /**
         * 封装handler.proceed()方法，目的避免静态扫描，
         * 实际我们对错误有做处理，但是特殊内核有缺陷，无法处理只能先这样，参考Chromium
         * @param handler
         */
        private void handleError(ISslErrorHandler handler) {
            if(null != handler) {
                handler.proceed();
            }
        }

        @Override
        public void doUpdateVisitedHistory(String url, boolean isReload) {
            if (mProtocolUrl.startsWith("local")) {
                if (mComponentId == null) {
                    Logger.w(TAG, "找不到该componentId：" + mProtocolUrl);
                    return;
                }
                String namespace = WebViewUtils.getNameSpaceByComId(mComponentId);
                String name = WebViewUtils.getNameByComId(mComponentId);
                if (namespace == null || name == null) {
                    return;
                }
                AnnounceJsonBean announceJsonBean = AnnounceJsonBeanOrmDao.getAnnounceJsonBean(namespace, name);
                if (announceJsonBean == null) {
                    Logger.w(TAG, "数据库中没有该离线组件:" + mComponentId);
                    return;
                }
                // 去掉在线url的host，留相对路径
                String relativePath = "";
                try {
                    if (url.startsWith("http")) {
                        relativePath = url.substring(announceJsonBean.getHost().length());
                    }
                    if (url.startsWith("file")) {
                        relativePath = url.substring(url.indexOf(mComponentId) + mComponentId.length() + Long.toString(System.currentTimeMillis()).length() + 1);
                    }
                } catch (IndexOutOfBoundsException e) {
                    Logger.w(TAG, "找不到relativePath:" + url);
                    return;
                }

                mProtocolUrl = "local://" + mComponentId + relativePath;
            }
            wantLoadUrl = url;
            setLeftButtonVisible();
        }

        /**
         * 用于动态换肤时拦截包含关键字的 url，并替换为本地资源
         *
         * @param view 当前的webview对象
         * @param url  当前访问的 url
         * @return
         */
        @Override
        public IHttpRequestInterceptionBean shouldInterceptRequest(View view, String url) {
            if (view == null) {
                return null;
            }

            if (url == null) {
                return null;
            }

            //定义一个初始值，默认为不拦截
            HttpRequestInterceptBeanImp beanImp = new HttpRequestInterceptBeanImp(false, null);
            if (url.contains(File.separator + WebViewConst.INTERCEPT_KEY_DYNAMIC)) {
                //根据当前的url host获取当前组件id
                IConfigManager configManager = AppFactory.instance().getConfigManager();
                String id = null;
                if (configManager != null) {
                    id = configManager.getComIdByHost(url);
                    Logger.i(TAG, "shouldInterceptRequest:current component id ==  " + id);
                } else {
                    Logger.w(TAG, "发现 AppFactory.instance().getConfigManager() 返回是 null");
                }

                File file = WebViewUtils.getLocalResource(url, id);

                //若返回的文件对象为null，则说明不需要拦截
                if (file == null) {
                    Logger.w(TAG, "shouldInterceptRequest: 本地未找到可替换的资源");
                    return beanImp;
                }

                InputStream is = null;
                if (file.exists()) {
                    try {
                        is = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        Logger.w(TAG, e.getMessage());
                    }
                }
                beanImp = new HttpRequestInterceptBeanImp(true, is);
            }
            return beanImp;
        }

        @Override
        public boolean onConsoleMessage(CommonConsoleMessage consoleMessage) {
            WebViewUtils.showConsoleMessage(consoleMessage);
            return false;
        }


        public void setWebviewTitle(String title, int invoker) {
            if (!mIsError) {
                //以下是加载网页没有出错，设置webview标题的业务逻辑

                //由于4.4以下（含）的机型有不执行onReceivedTitle的时机，这时我们在onLoadStarted中修改
                if (invoker == SET_TITLE_ON_LOAD_START && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    getSupportActionBar().setTitle(mWantedTitle);
                    mCurrentTitle = mWantedTitle;
                }

                //当onReceivedTitle中收到title时，设置webview标题的业务逻辑
                if (invoker == SET_TITLE_ON_RECEIVED_TITLE) {
                    if (mCurrentLoadUrl != null && mCurrentLoadUrl.equals(wantLoadUrl) && !TextUtils.isEmpty(mWantedTitle)) {
                        getSupportActionBar().setTitle(mWantedTitle);
                        mCurrentTitle = mWantedTitle;
                    } else {
                        //将标题设置为当前页面的默认标题
                        getSupportActionBar().setTitle(title);
                        mCurrentTitle = title;
                    }
                }
            } else {
                //以下是加载网页出错，设置webview标题的业务逻辑
                String tempTitle = getString(R.string.appfactory_webview_webpage_not_available);
                getSupportActionBar().setTitle(tempTitle);
                mCurrentTitle = tempTitle;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    mIsError = false; // 适配问题，5.0 - 6.0 出错后，onReceivedTitle是其最后一个回调函数
                }
            }
        }


    }

    /**
     * 弹出安全警告框告知用户当前访问的网址有安全证书错误。
     * 用户选择“返回“，则不加载当前的网址；
     * 用户选择“继续“， 则继续加载当前网址；
     * 用户选择“查看证书”， 可以查看证书的相关信息.
     */
    private void showSecurityWarningDialog(final String url, final ISslErrorHandler handler, final ISslError iSslError) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.appfactory_webview_security_warning);
        dialog.setMessage(R.string.appfactory_webview_warning_content);
        // 继续
        dialog.setPositiveButton(R.string.appfactory_webview_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
                mIsError = false;
                mIsReceivedSslError = false;
            }
        });
        // 返回
        dialog.setNegativeButton(R.string.appfactory_webview_return, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
                showErrorImage(R.string.appfactory_webview_ssl_error, R.drawable.webcomponent_ssl_error);
                mBtnRetry.setVisibility(View.GONE);
            }
        });
        // 查看证书
        dialog.setNeutralButton(R.string.appfactory_webview_view_certification, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showCertificationInfoDialog(url, handler, iSslError);
            }
        });
        dialog.setCancelable(false); // 点击dialog外部，不使其失去焦点而消失
        dialog.create();
        dialog.show();
    }

    /**
     * 查看证书信息的Dialog
     */
    private void showCertificationInfoDialog(final String url, final ISslErrorHandler handler, final ISslError iSslError) {
        String certificateInfo = WebViewUtils.buildSecurityCertificateString(mContext, iSslError);
        Logger.e(TAG, certificateInfo);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.appfactory_webview_security_certificate);
        dialog.setMessage(certificateInfo);
        dialog.setPositiveButton(R.string.appfactory_webview_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showSecurityWarningDialog(url, handler, iSslError);
            }
        });
        dialog.setNeutralButton(R.string.appfactory_webview_view_page_info, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showPageInfoDialog(url, handler, iSslError);
            }
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    /**
     * 查看网页详细信息
     */
    private void showPageInfoDialog(final String url, final ISslErrorHandler handler, final ISslError iSslError) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.appfactory_webview_page_info);
        StringBuilder sb = new StringBuilder();
        sb.append(mContext.getResources().getString(R.string.appfactory_webview_address)).append("\n");
        sb.append(url);
        dialog.setMessage(sb.toString());
        dialog.setPositiveButton(R.string.appfactory_webview_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showCertificationInfoDialog(url, handler, iSslError);
            }
        });
        dialog.setCancelable(false);
        dialog.create();
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // 返回之前, 都需要判断一下, 当前的软件盘是否是激活状态, 如果是, 则需要先关闭软件盘
        View view = this.getCurrentFocus();
        if (view != null) {
            Logger.i(TAG, "focused view is not null, try to close soft-input");
            InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else {
            Logger.i(TAG, "focused view is null, no need to close soft-input");
        }

        if(WebViewConst.LEFT_BUTTON_NONE.equals(leftButtonStatus)){
        }else if(mDelegate.getWebView().hasInjectBridge()){
            Logger.d(TAG,"onSupportNavigateUp(左上角返回键):当前页面包含JsBridge.js,可以询问");
            mDelegate.getWebView().evaluateJavascript(KEY_BACK_JS_CODE, null);
        }else{
            Logger.d(TAG,"onSupportNavigateUp(左上角返回键):当前页面不包含JsBridge.js，原样处理");
            processLeftButtonBackOrClose();
        }
        return true;
    }

    /**
     * 处理左上角返回键
     */
    private void processLeftButtonBackOrClose(){
        if (WebViewConst.LEFT_BUTTON_BACK.equals(leftButtonStatus)) {
            // back 模式, 在二级界面, 无历史的情况下,仍旧显示返回键
            goBack();
            setLeftButtonVisible();
        } else {
            // 默认为close，会进入该分支
            finishWebviewActivity();
        }
    }

    private void finishWebviewActivity(){
        // 如果是在Tab页的话，这里就不直接finish,而是回到桌面
        Activity parent = this.getParent();
        if (null != parent && parent instanceof IContainInterface) {
            // 该Activity被放在tab页中展示
            Logger.i(TAG, "detected is in TAB");
            parent.moveTaskToBack(true);
        } else {
            // 该Activity在二级页面，直接finish
            finish();
        }
    }

    private void loadSuccess() {
        if (!isFinishing()) {
            if (mainContainer != null) {
                mainContainer.setVisibility(View.VISIBLE);
            }

            if (this.mRlVisitException != null) {
                this.mRlVisitException.setVisibility(View.GONE);
            }
        }
    }

    private void loadFail(int errorcode) {
        Logger.w("loadFail", "errorcode=" + errorcode);
        if (!WebViewUtils.isNetworkConnected(this)) {
            showErrorImage(R.string.appfactory_webview_network_is_useless, R.drawable.webcomponent_no_network);
        } else {
            if (errorcode == WebViewClient.ERROR_FILE_NOT_FOUND) {
                showErrorImage(R.string.appfactory_webview_load_page_fail, R.drawable.webcomponent_visit_404);
                mBtnRetry.setText(R.string.appfactory_webview_return);
            } else {
                showErrorImage(R.string.appfactory_webview_load_page_fail, R.drawable.webcomponent_visit_fail);
            }
        }
    }


    /**
     * 提供内部webView实例直接加载url
     *
     * @param url : 目标地址.
     */
    private void loadUrl(final String url) {
        mIsError = false;
        mIsLoading = true;
        mDelegate.getWebView().loadUrl(url);
    }

    /**
     * 提供内部webView实例直接加载url
     *
     * @param url : 目标地址.
     * @param additionalHttpHeaders : 自定义webview url 请求头部
     */
    private void loadUrl(final String url, final Map<String, String> additionalHttpHeaders) {
        // 只有在  mDelegate.getWebView() instanceof IWebViewExt 时 ， 才支持 自定义webview url的header
        // 否则 使用原有的loadUrl
        if (mDelegate.getWebView() != null && mDelegate.getWebView() instanceof IWebViewExt) {
            mIsError = false;
            mIsLoading = true;
            ((IWebViewExt) mDelegate.getWebView()).loadUrl(url, additionalHttpHeaders);
        } else {
            loadUrl(url);
        }
    }


    /**
     * Note: 提供外部调用!!
     * 外部通过js-sdk的方式来调用, 通过当前的WebViewActivity实例来加载某个url
     *
     * @param uuid : 本地生成的用于两端验证的uuid
     */
    public void loadUrlFromJsSdk(final String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            Logger.w(TAG, "传入的uuid为空.");
            return;
        }
        Logger.i(TAG, "js-sdk发起加载url的请求: uuid = " + uuid);
        /******* 外站警示 验证uuid *****/
        IExternalWebsiteHandler handler = AppFactory.instance().getExternalWebsiteHandler();
        if (handler != null) {
            // 解码url
            final String destinationUrl = handler.getUrlByUUID(uuid);
            if (destinationUrl != null) {
                // 保存全局uuid
                mUuid = uuid;
                // 主线程load该url
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isRedirect = false; // 用户主动点击了加载, 这个flag要重置
                        loadUrl(destinationUrl);
                    }
                });
            }
        } else {
            Logger.w(TAG, "该应用没有集成外站警示功能!!");
        }
        /****************************/
    }

    /**
     * Note: 提供外部调用!!
     * 外部通过js-sdk的方式来调用, 告知Js端是否接管返回键
     *
     * @param isJsTakeKeyBack : Js端是否接管返回键,true:接管，false:不接管
     */
    public void isJsTakeKeyBack(boolean isJsTakeKeyBack) {
        if(!isJsTakeKeyBack){
            //mKeyBackEvent不空，那么是按了系统返回键
            if(null != mKeyBackEvent){
                if(!processNativeKeyBack()){
                    //一级界面这里是失效的，只能自己手动finish
                    //WebViewActivity.super.onKeyUp(KeyEvent.KEYCODE_BACK,mKeyBackEvent);
                    finishWebviewActivity();
                }
            } else {//否则认为是按了左上角返回键
                processLeftButtonBackOrClose();
            }
        }
        //不管如何，处理完置空
        mKeyBackEvent = null;
    }

    private void stopLoading() {
        if (mDelegate != null && mDelegate.getWebView() != null) {
            Logger.i(TAG, "停止loading...");
            mDelegate.getWebView().stopLoading();
        }
    }

    /**
     * 创建水平显示的菜单
     *
     * @param menu Menu
     */
    private void createHorBtnMenu(Menu menu) {
        createHorBtnMenu(menu, null);
    }

    private void createHorBtnMenu(Menu menu, ArrayList<String> menuIds) {
        if (null == mBtnMenuIds || mBtnMenuIds.isEmpty()) {
            return;
        }
        Map<String, IWebViewMenuItem> menus = null;
        if (null == menuIds) {
            menus = JsBridgeManager.getInstance().getExtendMenu(mBtnMenuIds);
        } else {
            menus = JsBridgeManager.getInstance().getExtendMenu(menuIds);
        }
        if (menus != null && menus.size() > 0) {
            if (mActionMenus == null) {
                mActionMenus = new HashMap<>();
            }
            String showName = null;
            for (String key : mBtnMenuIds) {
                IWebViewMenuItem iMenuItem = menus.get(key);
                if (iMenuItem != null) {
                    String menuId = iMenuItem.getMenuId();
                    showName = getMenuName(iMenuItem, WebViewActivity.this);
                    if (TextUtils.isEmpty(menuId)) {
                        Logger.w(WebViewActivity.class, "menu id is null" + showName);
                        continue;
                    }
                    int itemId = menuId.hashCode();
                    MenuItem menuItem = menu.add(0, itemId, 0, showName);
                    // 动态换肤支持
                    Drawable itemDrawable = WebViewUtils.getMenuItemSkin(iMenuItem);
                    menuItem.setIcon(itemDrawable);

                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

                    if (iMenuItem.getCallbackEventName() != null) {
                        mActivityResultCallbackList.add(iMenuItem.getCallbackEventName());
                    }

                    mActionMenus.put(menuId, menuItem);
                }
            }
        }
    }

    /**
     * 创建菜单项
     *
     * @param menu
     * @param menuIds
     */
    private void createMenus(Menu menu, ArrayList<String> menuIds) {
        if (null == menu || null == menuIds || menuIds.isEmpty()) {
            return;
        }
        Map<String, IWebViewMenuItem> menus = JsBridgeManager.getInstance().getExtendMenu(menuIds);
        if (menus != null && menus.size() > 0) {
            if (menu.findItem(R.id.action_menu) == null) {
                getMenuInflater().inflate(R.menu.webcomponent_main, menu);
            }
            MenuItem item = menu.findItem(R.id.action_menu);
            // 动态换肤支持(标题栏右上角菜单项较为特殊，需代码设置)
            Drawable drawable = CommonSkinUtils.getDrawable(this, R.drawable.general_top_icon_more);
            if (drawable != null) {
                item.setIcon(drawable);
            } else {
                Logger.w(WebViewActivity.class, "标题栏右上角菜单项动态换肤获取的drawable为空");
            }


            mMoreMenuItem = item;
            if (mPopupMenus == null) {
                mPopupMenus = new HashMap<>();
            }

            SubMenu subMenu = item.getSubMenu();
            IWebViewMenuItem iMenuItem = null;
            String showName = "";
            for (String key : menuIds) {
                iMenuItem = menus.get(key);
                if (iMenuItem != null) {
                    String menuId = iMenuItem.getMenuId();
                    showName = getMenuName(iMenuItem, WebViewActivity.this);
                    if (TextUtils.isEmpty(menuId)) {
                        Logger.w(WebViewActivity.class, "menu id is null" + showName);
                        continue;
                    }
                    int itemId = menuId.hashCode();
                    MenuItem menuItem = subMenu.add(0, itemId, 0, showName);
                    // 动态换肤支持.（暂时竖直菜单中不要求换肤，如果要求，只要把下面两行反注释即可！）
//                    Drawable itemDrawable = WebViewUtils.getMenuItemSkin(iMenuItem);
//                    menuItem.setIcon(itemDrawable);
                    menuItem.setIcon(iMenuItem.getMenuIcon());

                    if (iMenuItem.getCallbackEventName() != null) {
                        mActivityResultCallbackList.add(iMenuItem.getCallbackEventName());
                    }
                    mPopupMenus.put(menuId, menuItem);
                }
            }
        }
    }

    /**
     * 删除菜单项。水平和‘更多’都应删掉指定的菜单项。
     *
     * @param menu   菜单
     * @param menuId 菜单项id
     */
    private void deleteMenu(Menu menu, String menuId) {
        if (null == menu || null == menuId || menuId.isEmpty()) {
            return;
        }
        IWebViewMenuItem iMenuItem = JsBridgeManager.getInstance().getExtendMenu(menuId);
        MenuItem item = menu.findItem(R.id.action_menu);
        if (iMenuItem != null) {
            String id = iMenuItem.getMenuId();
            int itemId = id.hashCode();
            // 删 水平 中的菜单项
            if (null != mActionMenus && mActionMenus.containsKey(menuId)) {
                menu.removeItem(itemId);
                mActionMenus.remove(menuId);
            }
            // 删 ‘更多’ 中的菜单项
            if (item != null) {
                SubMenu subMenu = item.getSubMenu();
                if (subMenu != null) {
                    subMenu.removeItem(itemId);
                }
                if (iMenuItem.getCallbackEventName() != null) {
                    mActivityResultCallbackList.remove(iMenuItem.getCallbackEventName());
                }
                if (mPopupMenus != null && mPopupMenus.size() > 0) {
                    mPopupMenus.remove(menuId);
                    if (mPopupMenus.isEmpty()) {
                        // 没有子项目时，需要把“更多”的右上角按钮也删除
                        Logger.i(TAG, "更多菜单里的所有子项目都被反注册, 此时也将更多菜单移除掉...");
                        menu.removeItem(item.getItemId());
                        mMoreMenuItem = null;
                    }
                }
            }
        }
        JsBridgeManager.getInstance().unRegiesterMenu("WebViewActivity", menuId);
    }

    /**
     * 获取当前菜单的名称。
     * 如果有传入id优先读取id的值（国际化），如果传入的没有那么就直接使用传入的固定名称。
     *
     * @param bean
     * @param context
     * @return
     */
    private String getMenuName(IWebViewMenuItem bean, Context context) {
        if (bean != null) {
            String id = bean.getMenuNameResourceId();
            int idInt = 0;
            if (id != null && id.trim().length() != 0) {
                try {
                    idInt = Integer.valueOf(id);
                } catch (NumberFormatException N) {
                    Logger.w(TAG, "菜单名称id转换int类型错误名称id是 " + id + " 菜单key是" + bean.getMenuId());
                }
            }
            if (idInt > 0) {
                return context.getString(idInt);
            }
            if (bean instanceof MenuBean) {
                return ((MenuBean) bean).getMenuName();
            }
        }
        return "";
    }

    private void processMenuSelected(MenuItem item, ArrayList<String> menuIds) {
        if (null == item || null == menuIds || menuIds.isEmpty()) {
            return;
        }
        int itemId = item.getItemId();
        Map<String, IWebViewMenuItem> menus = JsBridgeManager.getInstance().getExtendMenu(menuIds);
        IWebViewMenuItem iMenuItem = null;
        MapScriptable param = null;
        if (menus != null && menus.size() > 0) {
            for (String key : menus.keySet()) {
                iMenuItem = menus.get(key);
                String menuId = iMenuItem.getMenuId();
                if (menuId.hashCode() == itemId) {
                    processMenuEvent(iMenuItem);
                }
            }
        }
    }

    private void processMenuEvent(IWebViewMenuItem iMenuItem) {
        if (null == iMenuItem) {
            return;
        }

        // 如果是默认的menu事件：复制、浏览器中打开、刷新，则直接处理
        if (dealDefaultMenuEvent(iMenuItem)) {
            return;
        }

        WebViewConst.MenuType type = iMenuItem.getType();
        Map<String, String> map = new HashMap<>();
        map.put(WebViewContainer.KEY_EXTEND_MSG, iMenuItem.getExtendMsg());
        map.put(WebViewContainer.KEY_CURRENT_TITLE, mCurrentTitle);
        map.put(WebViewContainer.KEY_ONCLICK_EVENT_NAME, iMenuItem.getClickEventName());
        // ssl安全证书错误时，通过webview获取的url为空，故做此处理以保证如"在浏览器中打开"、"复制链接"等菜单项可用
        if (mIsReceivedSslError && mSslErrorUrl != null) {
            map.put(WebViewContainer.KEY_EXTEND_URL, mSslErrorUrl);
        } else {
            map.put(WebViewContainer.KEY_EXTEND_URL, mDelegate.getWebView().getUrl());
        }
        if (WebViewConst.MenuType.FULL.equals(type)) {
            map.put(WebViewContainer.KEY_MENU_TYPE, WebViewConst.MenuType.FULL.toString());
        } else {
            map.put(WebViewContainer.KEY_MENU_TYPE, WebViewConst.MenuType.BASIC.toString());
        }
        map.put(WebViewContainer.KEY_CODE, KEY_CODE);

        /**
         * 下面交给webViewContainer处理。webViewContainer会扫描HTML，将扫描的结果通过
         * Bridge回传到Native的{@linkplain com.nd.smartcan.webview.DefJsBridge#sendMessageToNative}接口，
         * 将构造好的参数再传到{@linkplain com.nd.smartcan.appfactory.script.webkit.WebViewActivity#sendMessageToNative}处理相关的业务逻辑。
         */
        mDelegate.getCurrentPageInfo(map);
        Logger.i(TAG, "转到webView-wrapper，构造需要自定义的参数。");
    }

    /**
     * 获取最近的url。
     *
     * @return
     */
    private String getCurrentUrl() {
        String currentUrl = mDelegate.getWebView().getUrl();//在有些机型这个值是获取不到
        if (null == currentUrl || currentUrl.trim().length() == 0) {
            Logger.w(TAG, "mDelegate.getWebView().getUrl()  is null or empty  " + currentUrl);
            currentUrl = mCurrentLoadUrl;
            Logger.w(TAG, " after onLoadStared url is  " + currentUrl);
        }

        if (null == currentUrl || currentUrl.trim().length() == 0) {
            currentUrl = wantLoadUrl;
        }
        return currentUrl;
    }


    /**
     * 处理默认的Menu事件，包括：刷新、复制、浏览器中打开
     *
     * @return 如果是默认的事件，则返回true，否则返回false
     */
    private boolean dealDefaultMenuEvent(IWebViewMenuItem iMenuItem) {
        String url = getCurrentUrl();
        if (WebViewConst.KEY_RELOAD_EVENT_NAME.equals(iMenuItem.getClickEventName()) ||
                WebViewConst.EVENT_MENU_REFRESH.equals(iMenuItem.getClickEventName())) {
            Logger.w(TAG, "刷新，current url = " + url);
            if (mIsReceivedSslError) {
                mDelegate.getWebView().loadUrl(mSslErrorUrl);
            } else {
                //reload在某些机型上不起作用，比如三星 5.0 6.0  酷派 4.3 等，原因未知， 此时mDelegate.getWebView().getUrl()=null
                //mDelegate.getWebView().reload();
                // fix 6.0 7.0 reload 进度条显示不全
                wantLoadUrl = url;
                mDelegate.getWebView().reload();
            }
            return true;
        } else if (WebViewConst.EVENT_MENU_COPY.equals(iMenuItem.getClickEventName())
                || WebViewConst.EVENT_MENU_OPEN_WITH_BROWSER.equals(iMenuItem.getClickEventName())) {
            MapScriptable<String, String> mapScriptable = new MapScriptable<>();
            if (mIsReceivedSslError) {
                mapScriptable.put(WebViewConst.KEY_EXTEND_URL, mSslErrorUrl);
            } else {
                //d在某些机型上不起作用，比如三星 5.0 6.0  酷派 4.3 等，原因未知， 此时mDelegate.getWebView().getUrl()=null
                mapScriptable.put(WebViewConst.KEY_EXTEND_URL, url);
            }
            AppFactory.instance().triggerEventSync(this, iMenuItem.getClickEventName(), mapScriptable);
            return true;
        } else if (WebViewConst.EVENT_MENU_SET_FONT.equals(iMenuItem.getClickEventName())) {
            Toast.makeText(this, "功能待开发", Toast.LENGTH_SHORT).show();
            // 注释的代码暂时删除 - LinKun
            return true;
        } else {
            return false;
        }
    }

    /**
     * message 转发中心
     *
     * @param code  业务逻辑码，根据该code决定执行不同的业务
     * @param param js端传来的参数
     */
    @Override
    public void sendMessageToNative(String code, String param) {
        if (ProtocolUtils.isEmpty(code) || ProtocolUtils.isEmpty(param)) {
            Logger.w(TAG, "sendMessageToNative：code 或 param 为空！");
            return;
        }
        // 根据业务码 code 进行分发
        if (code.equals(KEY_CODE)) {
            dealWithMenuItemClickEvent(param);
        }
    }

    /**
     * 点击MenuItem 最后triggerEvent的执行入口
     */
    private void dealWithMenuItemClickEvent(String param) {
        if (ProtocolUtils.isEmpty(param)) {
            Logger.w(TAG, "dealWithMenuItemClickEvent：param 为空！");
            return;
        }
        // 解析param
        JSONObject paramJson = null;
        try {
            paramJson = new JSONObject(param);
        } catch (JSONException e) {
            Logger.e(TAG, "param转JSON 失败: " + e.getMessage() + ", param content: " + param);
            return;
        }

        MapScriptable paramMap = new MapScriptable();
        // 遍历paramJson，将其存为MapScriptable
        Iterator<String> iterator = paramJson.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = null;
            try {
                value = paramJson.getString(key);
            } catch (JSONException e) {
                Logger.e(TAG, "在paramJson中取值失败: " + e.getMessage());
            }
            paramMap.put(key, value);
        }

        String eventName = null;
        try {
            eventName = paramJson.getString(WebViewContainer.KEY_ONCLICK_EVENT_NAME);
        } catch (JSONException e) {
            Logger.e(TAG, "在paramJson中取eventName失败: " + e.getMessage());
        }
        AppFactory.instance().triggerEventSync(this, eventName, paramMap);
    }

    /**
     * 该函数主要是用于 _maf_menu 菜单项的显示
     * 网页内锚点跳转时，_maf_menu 菜单项需要和 js 设置的值保持一致。
     */
    private void showMoreMenuItem() {
        showMenuItem(mMoreMenuItem, isMoreMenuItemVisible);
    }

    /**
     * 该函数主要是用于 _maf_menu 菜单项的显示
     * 当网页加载进度 < 100 时，和以前一样，_maf_menu 是隐藏的
     * 当网页加载进度 >= 100 时，_maf_menu 的显示状态要和 js 端设置的保持一致。
     *
     * @param nLoadProgress 网页的加载进度
     */
    private void showMoreMenuItem(int nLoadProgress) {
        if (nLoadProgress >= 100) {
            showMenuItem(mMoreMenuItem, isMoreMenuItemVisible);
        } else {
            showMenuItem(mMoreMenuItem, false);
        }
    }

    /**
     * 控制单个菜单项是否显示
     */
    private void showMenuItem(MenuItem menuItem, boolean visible) {
        if (menuItem != null) {
            menuItem.setVisible(visible);
        }
    }

    /**
     * 控制多个菜单项是否显示
     */
    private void showMenus(Map<String, MenuItem> menus, boolean visible) {
        if (menus != null && menus.size() > 0) {
            for (Map.Entry<String, MenuItem> entry : menus.entrySet()) {
                MenuItem menuItem = entry.getValue();
                showMenuItem(menuItem, visible);
            }
        }
    }

    /**
     * 该接口废弃。外部不应该使用{@link WebViewContainer}直接对wrapper进行操作。
     * 所有对底层wrapper的操作请使用{@link WebContainerDelegate}
     * <p>
     * 对外提供一个获取WebViewContainer的接口
     */
    @Deprecated
    public WebViewContainer getWebViewContainer() {
        return (WebViewContainer) mDelegate.getWebContainer();
    }

    /**
     * 主线程刷新UI（目前用于导航栏）
     */
    static class MyHandler extends Handler {

        private WeakReference<WebViewActivity> mActivity;

        MyHandler(WebViewActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WebViewActivity activity = mActivity.get();

            // fix by 363576
            // 此处 activity 可能为空
            if (activity == null) {
                Logger.i(TAG, "[MyHandler.handleMessage] activity is null, ignore message");
                return;
            }

            boolean visible;
            if (activity.mWebviewObserver != null) {
                return;
            }
            switch (msg.what) {
                case IS_NAVIGATION_BAR_VISIBLE:
                    visible = (boolean) msg.obj;

                    // fix by 363576
                    // Activity.onDestroy 时，相关的控件被重置为 null
                    // 所以需要添加 null 的判断
                    int visibility = View.GONE;
                    if (visible) {
                        visibility = View.VISIBLE;
                    }

                    if (activity.mToolbar != null) {
                        activity.mToolbar.setVisibility(visibility);
                    }

                    if (activity.mDivider != null) {
                        activity.mDivider.setVisibility(visibility);
                    }

                    break;

                case IS_MENU_VISIBLE:
                    JSONObject param = (JSONObject) msg.obj;
                    Iterator it = param.keys();
                    while (it.hasNext()) {
                        String menuId = (String) it.next();
                        visible = param.optBoolean(menuId);

                        if (activity.mActionMenus != null && activity.mActionMenus.size() > 0) {
                            MenuItem menuItem = activity.mActionMenus.get(menuId);
                            if (menuItem != null) {
                                activity.showMenuItem(menuItem, visible);
                            }
                        }

                        if (activity.mPopupMenus != null && activity.mPopupMenus.size() > 0) {
                            MenuItem menuItem = activity.mPopupMenus.get(menuId);
                            if (menuItem != null) {
                                activity.showMenuItem(menuItem, visible);
                            }
                        }

                        //如果弹出式菜单里的菜单项全部设为不可见，则“更多”菜单项也设为不可见
                        shouldShowMoreMenuItemOrNot();

                        // add by LinKun start : 可单独对更多菜单进行设置显示或隐藏
                        if (activity.mMoreMenuItem != null && menuId != null && menuId.equals(WebViewConst.MENU_MORE)) {
                            // 用户在设置弹出菜单是否显示时，我们会自动去设置moreMenuItem是否显示
                            // 用户在此处主动设置是否显示更多菜单，需要用一个变量来保存，防止被自动设置moreMenuItem的逻辑给冲走
                            activity.isMoreMenuItemVisible = visible;
                            activity.mMoreMenuItem.setVisible(visible);
                        }
                        // add by LinKun end
                    }
                    break;
                case REGISTER_MENU_ITEM_VERTICAL:
                    String menuId0 = (String) msg.obj;
                    // 更多
                    registerMenuType(activity, menuId0, REGISTER_MENU_ITEM_VERTICAL);
                    shouldShowMoreMenuItemOrNot();
                    break;
                case REGISTER_MENU_ITEM_HORIZONTAL:
                    String menuId1 = (String) msg.obj;
                    // 水平
                    registerMenuType(activity, menuId1, REGISTER_MENU_ITEM_HORIZONTAL);
                    shouldShowMoreMenuItemOrNot();
                    break;
                case UNREGISTER_MENU:
                    String menuId = (String) msg.obj;
                    if (null != activity.mBtnMenuIds && activity.mBtnMenuIds.contains(menuId)) {
                        activity.mBtnMenuIds.remove(menuId);
                    }
                    if (null != activity.mMafMenuIds && activity.mMafMenuIds.contains(menuId)) {
                        activity.mMafMenuIds.remove(menuId);
                    }
                    activity.deleteMenu(activity.mMenu, menuId);
                    shouldShowMoreMenuItemOrNot();
                    break;
                default:
                    break;
            }
        }

        private void registerMenuType(WebViewActivity activity, String menuId, int type) {
            ArrayList<String> list = new ArrayList<>();
            list.add(menuId);
            if (!ProtocolUtils.isEmpty(menuId)) {
                if (type == REGISTER_MENU_ITEM_HORIZONTAL) {
                    if (null == activity.mBtnMenuIds) {
                        activity.mBtnMenuIds = new ArrayList<>();
                    }
                    activity.mBtnMenuIds.add(menuId);
                    activity.createHorBtnMenu(activity.mMenu, list);
                } else if (type == REGISTER_MENU_ITEM_VERTICAL) {
                    if (null == activity.mMafMenuIds) {
                        activity.mMafMenuIds = new ArrayList<>();
                    }
                    activity.mMafMenuIds.add(menuId);
                    activity.createMenus(activity.mMenu, list);
                }
            } else {
                Logger.w(TAG, "传入的menuId为空！");
            }
        }

        /**
         * 是否需要显示‘更多’按钮
         */
        public void shouldShowMoreMenuItemOrNot() {
            WebViewActivity activity = mActivity.get();
            if (activity.isMoreMenuItemVisible) { // 一旦用户设置更多菜单隐藏，都不会主动开启
                if (activity.mPopupMenus != null && activity.mPopupMenus.size() > 0) {
                    boolean isMoreMenuItemVisible = false;
                    for (Map.Entry<String, MenuItem> entry : activity.mPopupMenus.entrySet()) {
                        MenuItem menuItem = entry.getValue();
                        if (menuItem.isVisible()) {
                            isMoreMenuItemVisible = true;
                            break;
                        }
                    }
                    activity.mMoreMenuItem.setVisible(isMoreMenuItemVisible);
                }
            }
        }

    }

    private void showErrorImage(int errStrId, int imageResId) {
        if (!isFinishing()) {
            mIsError = true;
            if (mainContainer != null) {
                mainContainer.setVisibility(View.GONE);
            }
            if (this.mRlVisitException != null) {
                this.mRlVisitException.setVisibility(View.VISIBLE);
                mBtnRetry.setVisibility(View.VISIBLE);
            }

            if (errStrId > 0) {
                mTvVisitError.setText(getString(errStrId));
            }
            if (imageResId > 0) {
                mIvVsitException.setImageResource(imageResId);
            }
        }
    }

    /**
     * 处理外站警示
     */
    private void handleExternalWebsite(String newUrl) {
        // 当成功加载页面后, 开始后台外站警示逻辑
        IExternalWebsiteHandler handler = AppFactory.instance().getExternalWebsiteHandler();
        if (handler != null && newUrl != null) {
            // 判断是否需要对传入的url进行转换, 只有http/https打头的才需要转换
            if (!handler.isNeedConvert(newUrl)) {
                // 无需转换的, 直接跳过后面的外站警示逻辑.
                return;
            }

            // 验证uuid:
            // 1. 如果该uuid是仿造的(即和native保存的不一致), 则仍需进一步验证
            // 2. 如果该uuid和本地存储的一致, 则直接return, 下一步就是直接打开页面(说明是用户主动选择打开的)
            if (!TextUtils.isEmpty(mUuid)) {
                String savedUrl = handler.getUrlByUUID(mUuid);
                String savedUrlHost = UrlUtils.getUrlHost(savedUrl);
                if (savedUrlHost != null) {
                    // 当前加载的url的host和保存的url的host是一样的
                    // 两个host一样, 代表当前页面是用户上次通过警示页点击"确认打开"而开启的页面
                    if (savedUrlHost.equals(UrlUtils.getUrlHost(newUrl))) {
                        Logger.i(TAG, "警示网页, 同一个host, 用户允许继续加载...");
                        return;
                    }
                }
            }

            String urlType = handler.getUrlTypeByCache(newUrl);
            if (BLACK_LIST.equals(urlType)) {
                // 该网址为恶意的网址, 阻止用户继续访问, 重定向到nd提供的警示页面
                Logger.w(TAG, "缓存表中该地址为恶意网址, 需要重定向. url = " + newUrl);
                // 外站警示服务器重定向
                redirectExternalWebsite(handler, newUrl, BLACK_LIST);
            } else if (NORMAL.equals(urlType)) {
                // do nothing
                Logger.d(TAG, "缓存表中该地址为正常的地址. url = " + newUrl);
            } else {
                // 缓存中没有找到, 需要网络请求
                redirectExternalWebsite(handler, newUrl, NOT_EXISTS);
                Logger.d(TAG, "缓存表中该地址未记录, 需要异步从服务端获取. url = " + newUrl);
            }
        }
    }

    /**
     * 重定向黑名单上的网址
     */
    private void redirectExternalWebsite(final IExternalWebsiteHandler handler,
                                         final String destinationUrl, final String urlType) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    // 经过查询服务器后, 得到重定向的url
                    return handler.filter(destinationUrl, urlType);
                } catch (Exception e) {
                    Logger.w(TAG, "外站警示的组件过滤url出现异常, " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String redirectUrl) {
                // 只有当重定向的url以local打头时, 才需要重新loadUrl, 定向到nd的警示页
                if (redirectUrl != null
                        && redirectUrl.startsWith(ProtocolConstant.KEY_LOCAL_HTML_PAGE_MANAGER)) {
                    wantLoadUrl = getRealUrl(redirectUrl);
                    Logger.i(TAG, "监测有问题的url = " + destinationUrl + ", 需要重定向的url = "
                            + redirectUrl + ", 跳转到外站警示页面 url = " + wantLoadUrl);
                    stopLoading();
                    isRedirect = true;
                    loadUrl(wantLoadUrl);
                }
            }
        }.execute();
    }
}
