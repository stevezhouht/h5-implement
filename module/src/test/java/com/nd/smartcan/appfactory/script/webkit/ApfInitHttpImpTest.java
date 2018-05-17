package com.nd.smartcan.appfactory.script.webkit;

import com.nd.smartcan.appfactory.script.webkit.impl.ApfInitHttpImp;

import org.junit.Test;

/**
 * Created by Administrator on 2018/4/25 0025.
 */

public class ApfInitHttpImpTest extends BaseTest {
    ApfInitHttpImp apfInitHttpImp = new ApfInitHttpImp();
    @Test
    public void initialPkgId(){
        apfInitHttpImp.initialPkgId();
    }

    @Test
    public void initialSDPHeader(){
        apfInitHttpImp.initialSDPHeader();
    }
}
