package com.nd.smartcan.appfactory.script.webkit;

import com.nd.smartcan.appfactory.script.webkit.download.DownloadException;
import com.nd.smartcan.appfactory.script.webkit.impl.HttpDownloadUtilImp;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.shadows.ShadowApplication;

import java.io.File;

/**
 * Created by Administrator on 2018/4/26 0026.
 */

public class HttpDownloadUtilImpTest extends BaseTest{
    HttpDownloadUtilImp httpDownloadUtilImp = new HttpDownloadUtilImp();
    @Test
    public void startDownload(){
//        httpDownloadUtilImp.startDownload(ShadowApplication.getInstance().getApplicationContext(),"abv","","");
    }

    @Test
    public void fileExt(){
        Assert.assertEquals("",HttpDownloadUtilImp.fileExt(null));
        Assert.assertEquals(".png",HttpDownloadUtilImp.fileExt("a.png"));
    }

    @Test
    public void getDownloadFile(){
        Assert.assertNotNull(HttpDownloadUtilImp.getDownloadFile(ShadowApplication.getInstance().getApplicationContext(),"abc.a",true));
    }

    @Test
    public void getFileDirPath(){
        Assert.assertEquals("null\\null\\-1",HttpDownloadUtilImp.getFileDirPath(null,null));
        Assert.assertEquals("a\\b\\-1",HttpDownloadUtilImp.getFileDirPath("a","b"));
    }

    @Test
    public void getFileInSysCache() throws DownloadException {
        Assert.assertNotNull(HttpDownloadUtilImp.getFileInSysCache(ShadowApplication.getInstance().getApplicationContext(),"a",true));
    }

    @Test
    public void getFileInSDCardBase() throws DownloadException {
        Assert.assertNotNull(HttpDownloadUtilImp.getFileInSDCardBase(ShadowApplication.getInstance().getApplicationContext(),"a",true));
    }

    @Test
    public void getOpenFileIntent(){
        Assert.assertNull(HttpDownloadUtilImp.getOpenFileIntent(null));
        Assert.assertNull(HttpDownloadUtilImp.getOpenFileIntent(new File("a")));
    }

    @Test
    public void getTheFileByParam() throws DownloadException {
        Assert.assertNotNull(HttpDownloadUtilImp.getTheFileByParam(ShadowApplication.getInstance().getApplicationContext(),"a","b",new File("c"),null,false));
    }

    @Test
    public void makesureFileSepInTheEnd(){
        Assert.assertNull(HttpDownloadUtilImp.makesureFileSepInTheEnd(null));
        Assert.assertEquals("",HttpDownloadUtilImp.makesureFileSepInTheEnd(""));
        Assert.assertEquals("a\\",HttpDownloadUtilImp.makesureFileSepInTheEnd("a"));
        Assert.assertEquals("a/\\",HttpDownloadUtilImp.makesureFileSepInTheEnd("a/"));
    }

    @Test
    public void openFile(){
        HttpDownloadUtilImp.openFile(null,null);
//        HttpDownloadUtilImp.openFile(ShadowApplication.getInstance().getApplicationContext(),"a");
    }

    @Test
    public void renameOnConflict(){
        Assert.assertNull(HttpDownloadUtilImp.renameOnConflict(null,0));
        Assert.assertNotNull(HttpDownloadUtilImp.renameOnConflict("aaa",0));
    }

}
