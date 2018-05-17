package com.nd.smartcan.appfactory.script.webkit;

import com.nd.smartcan.appfactory.script.webkit.impl.WebviewForH5AppManagerImp;
import com.nd.smartcan.appfactory.vm.PageUri;

import junit.framework.Assert;

import org.junit.Test;
import org.robolectric.shadows.ShadowApplication;

/**
 * Created by Administrator on 2018/4/26 0026.
 */

public class WebviewForH5AppManagerImpTest extends BaseTest {
    WebviewForH5AppManagerImp webviewForH5AppManagerImp = new WebviewForH5AppManagerImp();
    @Test
    public void getPage(){
        Assert.assertNull(webviewForH5AppManagerImp.getPage(ShadowApplication.getInstance().getApplicationContext(),null));
        Assert.assertNotNull(webviewForH5AppManagerImp.getPage(ShadowApplication.getInstance().getApplicationContext(),new PageUri("http://www.baidu.com")));
    }
}
