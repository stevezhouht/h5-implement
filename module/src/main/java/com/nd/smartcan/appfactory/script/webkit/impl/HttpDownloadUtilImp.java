package com.nd.smartcan.appfactory.script.webkit.impl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.nd.android.sdp.dm.DownloadManager;
import com.nd.android.sdp.dm.options.DownloadOptions;
import com.nd.android.sdp.dm.options.DownloadOptionsBuilder;
import com.nd.android.sdp.dm.options.OpenAction;
import com.nd.android.sdp.dm.pojo.BaseDownloadInfo;
import com.nd.android.sdp.dm.pojo.IDownloadInfo;
import com.nd.android.sdp.dm.state.State;
import com.nd.sdp.android.apf.h5.R;
import com.nd.smartcan.appfactory.AppFactory;
import com.nd.smartcan.appfactory.script.webkit.download.DownloadException;
import com.nd.smartcan.appfactory.script.webkit.download.DownloadExceptionCode;
import com.nd.smartcan.appfactory.script.webkit.download.IHttpDownloadUtil;
import com.nd.smartcan.commons.util.logger.Logger;

import java.io.File;

/**
 * Created by Administrator on 2018/2/22 0022.
 */

public class HttpDownloadUtilImp implements IHttpDownloadUtil {
    private final static String TAG = HttpDownloadUtilImp.class.getSimpleName();
    /**
     * 存放sdcard目录名
     */
    private static String sSDCardDir = "maf_webview_down";

    private final static String NORMAL_FILE_TYPE_DIR = "file";

    @Override
    public void startDownload(Context context, String url, String fileName, String session) {
        try {
            android.support.v4.util.ArrayMap<String, IDownloadInfo> downloadInfos = DownloadManager.INSTANCE.getDownloadInfos(context, BaseDownloadInfo.class, url);
            if (null != downloadInfos && downloadInfos.containsKey(url)) {
                IDownloadInfo downloadInfo = downloadInfos.get(url);
                final String filePath = downloadInfo.getFilePath();
                if (null != downloadInfo && downloadInfo.getState() == State.FINISHED && !TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                    //文件已经下载，并且，那就打开，然后结束
                    openFile(context, filePath);
                    return;
                } else if (null != downloadInfo && downloadInfo.getState() == State.DOWNLOADING) {
                    //正在下载的不作处理了
                    Toast.makeText(context.getApplicationContext(), R.string.appfactory_webview_downloading,Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            File file = getDownloadFile(context, fileName, false);

            if(null == file){
                Logger.w(TAG,"getDownloadFile fail");
                return;
            }
            String path = file.getParent();
            String name = fileName;
            DownloadOptionsBuilder builder = null;

            if (!TextUtils.isEmpty(session)) {
                //有session的
                builder = new DownloadOptionsBuilder()
                        .fileName(name) // 保存文件名
                        .needNotificationBar(true)
                        .parentDirPath(path) // 父级目录
                        .urlParam("session", session)
                        .openAction(OpenFileAction.class)
                        .detectNetworkType(true);
            } else {
                builder = new DownloadOptionsBuilder()
                        .fileName(name) // 保存文件名
                        .needNotificationBar(true)
                        .parentDirPath(path) // 父级目录
                        .openAction(OpenFileAction.class)
                        .detectNetworkType(true);
            }

            DownloadOptions downloadOptions = builder.build();
            DownloadManager.INSTANCE.start(context, url, downloadOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param context
     * @param path
     *
     * @hide
     */
    public static void openFile(Context context, String path){
        if(null == context || TextUtils.isEmpty(path)){
            return;
        }
        try {
            File file = new File(path);
            if (!file.exists()){
                Toast.makeText(context, context.getString(R.string.appfactory_webview_file_not_exist), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = getOpenFileIntent(file);
            if (intent != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, context.getString(R.string.appfactory_webview_not_support_file_type), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.appfactory_webview_not_support_file_type), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @hide
     * @param file
     * @return
     */
    public static Intent getOpenFileIntent(File file) {
        if(null == file){
            Logger.w(TAG,"getOpenFileIntent:file is null");
            return null;
        }
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String ext = fileExt(file.getAbsolutePath());
        if(null != myMime && !TextUtils.isEmpty(ext)) {
            String mimeType = myMime.getMimeTypeFromExtension(ext.substring(1));
            if(!TextUtils.isEmpty(mimeType)) {
                newIntent.setDataAndType(Uri.fromFile(file), mimeType);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return newIntent;
            }
        }

        return null;
    }

    /**
     * @hide
     * @param url
     * @return
     */
    public static String fileExt(String url) {
        if(TextUtils.isEmpty(url)) {
            return "";
        } else {
            String newUrl = url;
            if(url.contains("?")) {
                newUrl = url.substring(0, url.indexOf("?"));
            }

            if(newUrl.lastIndexOf(".") == -1) {
                return null;
            } else {
                String ext = newUrl.substring(newUrl.lastIndexOf("."));
                if(ext.contains("%")) {
                    ext = ext.substring(0, ext.indexOf("%"));
                }

                if(ext.contains("/")) {
                    ext = ext.substring(0, ext.indexOf("/"));
                }

                return ext.toLowerCase();
            }
        }
    }

    /**
     * 获取用户文件对象（往来文件、群共享文件），优先取扩展卡基准目录，没有则取系统缓存目录
     *
     * @param context        上下文
     * @param fileName       文件名
     * @param isNeedRealName 重名时是否重命名
     * @return 用户文件对象
     * @hide
     */
    public static File getDownloadFile(Context context, String fileName, boolean isNeedRealName) {
        File file = null;
        try {
            file = getFileInSDCardBase(context, fileName, isNeedRealName);
            if (file == null) {
                file = getFileInSysCache(context, fileName, isNeedRealName);
            }
        } catch (DownloadException e) {
            Logger.w(TAG,e.getMessage());
        }
        return file;
    }

    /**
     * 获取系统内存目录上的文件对象
     *
     * @param context        上下文
     * @param fileName       文件名
     * @param isNeedRealName 当重名时是否需要重新命名
     * @return 系统内存目录上的文件对象
     * @throws DownloadException
     * @hide
     */
    public static File getFileInSysCache(Context context, String fileName, boolean isNeedRealName) throws DownloadException {
        if (context == null) {
            throw new DownloadException(DownloadExceptionCode.EMPTY_CONTEXT, "empty context exception");
        }
        File file = getTheFileByParam(context, "", fileName, context.getCacheDir(), NORMAL_FILE_TYPE_DIR, isNeedRealName);
        return file;
    }

    /**
     * 获取扩张卡根目录上的文件对象
     *
     * @param context        上下文
     * @param fileName       需要存取的文件名
     * @param isNeedRealName 当重名时是否需要重新命名
     * @return 扩张卡上的文件对象
     * @hide
     */
    public static File getFileInSDCardBase(Context context, String fileName, boolean isNeedRealName) throws DownloadException {
        String sdPath = AppFactory.instance().getAppRootSdCardDir(context);
        File file = getTheFileByParam(context, sSDCardDir, fileName, new File(sdPath), NORMAL_FILE_TYPE_DIR, isNeedRealName);
        return file;
    }

    /**
     * 确保末尾的斜杠
     * <p/>
     *
     * @param strDir
     *         路径字符串
     * @return String
     * by HuangYK
     * @hide
     */
    public static String makesureFileSepInTheEnd(String strDir) {
        if (!TextUtils.isEmpty(strDir)) {
            // 确保以斜杠结尾
            if (!strDir.endsWith(File.separator)) {
                strDir += File.separator;
            }
        }

        return strDir;
    }


    /**
     * 根据参数获取文件对象，私有函数，内部使用
     *
     * @param context        上下文
     * @param baseFileDir    根级目录名称，sSDCardDir ，默认99U，目前只有在SD卡根目录上使用，支持配置。如果不是SD卡根目录，这里传空值
     * @param fileName       文件名
     * @param fileDir        基于baseFileDir的文件存放目录，如果不是SD卡根目录，SD卡缓存、系统缓存情况，这个参数则直接作为存储路径
     * @param fileTypeDir    文件类型
     * @param isNeedRealName 当重名时是否需要重新命名
     * @return 文件对象
     * @throws DownloadException
     * @hide
     */
    public static File getTheFileByParam(Context context, String baseFileDir,
                                          String fileName, File fileDir, String fileTypeDir, boolean isNeedRealName) throws DownloadException {
        if (context == null) {
            throw new DownloadException(DownloadExceptionCode.EMPTY_CONTEXT, "empty context exception");
        }
        if (TextUtils.isEmpty(fileName)) {
            throw new DownloadException(DownloadExceptionCode.EMPTY_PATH, "empty path exception");
        }

        // 如果没有获取到正确的SD卡路径，则返回空值
        if (fileDir == null || TextUtils.isEmpty(fileDir.getAbsolutePath())) {
            return null;
        }
        // 根据 sd卡目录/uid/file 的规则存放文件下载的路径
        baseFileDir = baseFileDir == null ? "" : baseFileDir;
        String extFile = getFileDirPath(baseFileDir, fileTypeDir);// baseFileDir + fileTypeDir + "/" + IMSDKGlobalVariable.getCurrentUid();

        // 实例化这个文件对象
        File fileInSDCard = new File(makesureFileSepInTheEnd(fileDir.getAbsolutePath()) + extFile, fileName);

        // 如果需要对重名文件进行重新命名
        if (isNeedRealName) {
            int number = 0;
            String tempName;
            // 如果存在则重命名，直到没有重名为止
            while (fileInSDCard.exists()) {
                number++;
                tempName = renameOnConflict(fileName, number);
                fileInSDCard = new File(makesureFileSepInTheEnd(fileDir.getAbsolutePath()) + extFile, tempName);
            }
        }

        // 判断文件父目录是否存在，如果不存在则尝试创建，创建失败的话抛异常
        if (!fileInSDCard.getParentFile().exists() && !fileInSDCard.getParentFile().mkdirs()) {
            throw new DownloadException(DownloadExceptionCode.FILE_CANNOT_CREATE,
                    String.format("%s cannot be created!", fileInSDCard.toString()));
        }

        return fileInSDCard;
    }


    /**
     * 获取文件存储目录
     *
     * @param baseFileDir 根目录
     * @param fileTypeDir 文件类型
     * @return 文件存储目录
     *
     * @hide
     */
    public static String getFileDirPath(String baseFileDir, String fileTypeDir) {
        StringBuffer sb = new StringBuffer();
        sb.append(baseFileDir).append(File.separator).append(fileTypeDir).append(File.separator).append(AppFactory.instance().getUid());
        return sb.toString();
    }


    /**
     * @param name 文件名
     * @return string 新的文件名
     * 当文件名冲突的时候变更文件名
     * by cb
     * 2015-01-12 05-39-59
     * @hide
     */
    public static String renameOnConflict(String name, int number) {
        if(null == name){
            return null;
        }
        int index = name.lastIndexOf(".");
        if (index > -1) {
            //带扩展名
            StringBuffer newName = new StringBuffer();
            newName.append(name.substring(0, index)).append("(" + number + ")").append(name.substring(index));
            return newName.toString();
        }
        final String ONE = "(1)";
        //不带扩展名，直接在后面加（1）
        return name + ONE;
    }

    /**
     * @hide
     */
    public static class OpenFileAction implements OpenAction {


        @Override
        public void open(Context pContext, String filePath) {

            if (TextUtils.isEmpty(filePath) || pContext == null) {
                Logger.w(TAG,"filePath is empty or context is null");
                return;
            }

            openFile(pContext, filePath);
        }
    }
}
