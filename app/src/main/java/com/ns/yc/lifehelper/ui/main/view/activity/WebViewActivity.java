package com.ns.yc.lifehelper.ui.main.view.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.ns.yc.lifehelper.R;
import com.ns.yc.lifehelper.base.BaseActivity;
import com.ns.yc.lifehelper.utils.AppUtil;
import com.ns.yc.lifehelper.utils.DoShareUtils;
import com.ns.yc.lifehelper.utils.LogUtils;
import com.ns.yc.ycutilslib.webView.ScrollWebView;

import butterknife.Bind;

/**
 * ================================================
 * 作    者：杨充
 * 版    本：1.0
 * 创建日期：2017/8/28
 * 描    述：外部链接跳转的页面
 * 修订历史：
 * ================================================
 */
public class WebViewActivity extends BaseActivity {

    @Bind(R.id.ll_title_menu)
    FrameLayout llTitleMenu;
    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.webView)
    ScrollWebView mWebView;
    @Bind(R.id.pb)
    ProgressBar pb;
    @Bind(R.id.video_fullView)
    FrameLayout videoFullView;
    @Bind(R.id.ll_web_view)
    LinearLayout llWebView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    private String url;
    private String name;
    private MyWebChromeClient webChromeClient;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            //mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //全屏播放退出全屏
            if (webChromeClient.inCustomView()) {
                hideCustomView();
                return true;
            } else if (mWebView.canGoBack()) {
                //返回上一页面
                mWebView.goBack();
                return true;
            } else {
                //退出网页
                mWebView.loadUrl("about:blank");
                finish();
            }
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_web_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(url==null || url.length()==0){
            url = "about:blank";
        }

        switch (item.getItemId()) {
            case R.id.share:
                DoShareUtils.shareText(this,url,name);
                break;
            case R.id.collect:
                /*if(Constant.isLogin){
                    goToCollect();
                }else {
                    startActivity(MeLoginActivity.class);
                }*/
                ToastUtils.showShortSafe("后期添加");
                break;
            case R.id.cope:
                AppUtil.copy(url);
                ToastUtils.showShortSafe("复制成功");
                break;
            case R.id.open:
                AppUtil.openLink(this, url);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public int getContentView() {
        return R.layout.activity_video_web_view;
    }


    @Override
    public void initView() {
        videoFullView.setVisibility(View.GONE);
        llWebView.setVisibility(View.VISIBLE);
        initToolBar();
        initIntentData();
        initWebView();
    }


    private void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }


    private void initIntentData() {
        Intent intent = getIntent();
        if(intent!=null){
            url = intent.getStringExtra("url");
            name = intent.getStringExtra("name");
        }else {
            url = "https://github.com/yangchong211";
            name = "新闻";
        }
        toolbarTitle.setText(name);
    }


    @Override
    public void initListener() {
        llTitleMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    @Override
    public void initData() {
        //这种情况，log日志会走100次，但是mWebView.loadUrl方法只会执行一次
        /*for(int a=0 ; a<100 ; a++){
            Log.e("url","http://www.jcodecraeer.com/plus/view.php?aid=8895");
            mWebView.loadUrl("http://www.jcodecraeer.com/plus/view.php?aid=8895");
        }*/
        mWebView.loadUrl(url);
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings ws = mWebView.getSettings();
        // 网页内容的宽度是否可大于WebView控件的宽度
        ws.setLoadWithOverviewMode(false);
        // 保存表单数据
        ws.setSaveFormData(true);
        // 是否应该支持使用其屏幕缩放控件和手势缩放
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        // 启动应用缓存
        ws.setAppCacheEnabled(true);
        // 设置缓存模式
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        // setDefaultZoom  api19被弃用
        // 设置此属性，可任意比例缩放。
        ws.setUseWideViewPort(true);
        // 缩放比例 1
        mWebView.setInitialScale(1);
        // 告诉WebView启用JavaScript执行。默认的是false。
        ws.setJavaScriptEnabled(true);
        //如果启用了JavaScript，要做好安全措施，防止远程执行漏洞
        removeJavascriptInterfaces(mWebView);
        //  页面加载好以后，再放开图片
        ws.setBlockNetworkImage(false);
        // 使用localStorage则必须打开
        ws.setDomStorageEnabled(true);
        //自动加载图片
        ws.setLoadsImagesAutomatically(true);
        // 排版适应屏幕
        ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        // WebView是否支持多个窗口。
        ws.setSupportMultipleWindows(true);
        // webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        /** 设置字体默认缩放大小(改变网页字体大小,setTextSize  api14被弃用)*/
        ws.setTextZoom(100);
        mWebView.addJavascriptInterface(new JavascriptInterface(this), "injectedObject");
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(webChromeClient = new MyWebChromeClient());
        mWebView.setScrollWebListener(new ScrollWebView.OnScrollWebListener() {
            @Override
            public void onScroll(int dx, int dy) {
                //WebView的总高度
                float webViewContentHeight = mWebView.getContentHeight() * mWebView.getScale();
                //WebView的现高度
                float webViewCurrentHeight= (mWebView.getHeight() + mWebView.getScrollY());
                LogUtils.e("webViewContentHeight="+webViewContentHeight);
                LogUtils.e("webViewCurrentHeight="+webViewCurrentHeight);
                if ((webViewContentHeight-webViewCurrentHeight) == 0) {
                    LogUtils.e("WebView滑动到了底端");
                    //TODO 处理滑到底部的逻辑
                }
            }
        });
    }


    /**
     * 上传图片之后的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 201) {
            webChromeClient.mUploadMessageForAndroid5(intent, resultCode);
        }
    }


    /**
     * 全屏时按返加键执行退出全屏方法，切换为竖屏
     */
    public void hideCustomView() {
        webChromeClient.onHideCustomView();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    /**
     * 监听网页链接:
     * 优酷视频直接跳到自带浏览器
     * 根据标识:打电话、发短信、发邮件
     * 添加javascript监听
     */
    private class MyWebViewClient extends WebViewClient {
        //url拦截
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogUtils.e("WebViewActivity-----shouldOverrideUrlLoading-------"+url);
            if (url.startsWith("http://v.youku.com/")) {
                //视频跳转浏览器播放
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addCategory("android.intent.category.BROWSABLE");
                Uri content_url = Uri.parse(url);
                intent.setData(content_url);
                WebViewActivity.this.startActivity(intent);
            } else if (url.startsWith(WebView.SCHEME_TEL) || url.startsWith("sms:") || url.startsWith(WebView.SCHEME_MAILTO)) {
                //电话、短信、邮箱
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    WebViewActivity.this.startActivity(intent);
                } catch (ActivityNotFoundException ignored) {

                }
            } else {
                view.loadUrl(url);
            }
            return true;
        }

        //开始加载时
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            LogUtils.e("WebViewActivity-----onPageStarted-------"+url);
        }

        //结束加载时，这个方法有点奇怪。有时候没有完全结束，就调用呢！！
        @Override
        public void onPageFinished(WebView view, String url) {
            addImageClickListener();
            super.onPageFinished(view, url);
            LogUtils.e("WebViewActivity-----onPageFinished-------"+url);
        }


        // 视频全屏播放按返回页面被放大的问题
        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            if (newScale - oldScale > 7) {
                view.setInitialScale((int) (oldScale / newScale * 100));    //异常放大，缩回去。
            }
        }

        // 向主机应用程序报告Web资源加载错误。这些错误通常表明无法连接到服务器。
        // 值得注意的是，不同的是过时的版本的回调，新的版本将被称为任何资源（iframe，图像等）
        // 不仅为主页。因此，建议在回调过程中执行最低要求的工作。
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        // 通知主机应用程序在加载资源时从服务器接收到HTTP错误。
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        // 通知主机应用程序已自动处理用户登录请求。
        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
        }

        // 在加载资源时通知主机应用程序发生SSL错误。
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }
    }


    /**
     * 相关配置
     * 播放网络视频配置
     * 上传图片(兼容)
     */
    private class MyWebChromeClient extends WebChromeClient {

        private View progressVideo;
        private View customView;
        private CustomViewCallback customViewCallback;
        private ValueCallback<Uri[]> mUploadMessageForAndroid5;
        private ValueCallback<Uri> mUploadMessage;

        //监听h5页面的标题
        @Override
        public void onReceivedTitle(WebView view, String title) {
            toolbarTitle.setText(title);
            LogUtils.e("WebViewActivity-----onReceivedTitle-------"+title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            pb.setProgress(newProgress);
            if (newProgress == 100) {
                pb.setVisibility(View.GONE);
            }
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (progressVideo == null) {
                LayoutInflater inflater = LayoutInflater.from(WebViewActivity.this);
                progressVideo = inflater.inflate(R.layout.view_video_loading_progress, null);
            }
            return progressVideo;
        }

        // 播放网络视频时全屏会被调用的方法，播放视频切换为横屏
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            WebViewActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            // 如果一个视图已经存在，那么立刻终止并新建一个
            if (customView != null) {
                callback.onCustomViewHidden();
                return;
            }

            fullViewAddView(view);
            customView = view;
            customViewCallback = callback;

            videoFullView.setVisibility(View.VISIBLE);
            llWebView.setVisibility(View.GONE);
        }

        // 视频播放退出全屏会被调用的
        @Override
        public void onHideCustomView() {
            if (customView == null)// 不是全屏播放状态
                return;
            WebViewActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            customView.setVisibility(View.GONE);
            if (WebViewActivity.this.getVideoFullView() != null) {
                WebViewActivity.this.getVideoFullView().removeView(customView);
            }
            customView = null;
            customViewCallback.onCustomViewHidden();

            videoFullView.setVisibility(View.GONE);
            llWebView.setVisibility(View.VISIBLE);
        }


        // For Android > 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, FileChooserParams fileChooserParams) {
            openFileChooserImplForAndroid5(uploadMsg);
            return true;
        }

        private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
            mUploadMessageForAndroid5 = uploadMsg;
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "图片选择");

            WebViewActivity.this.startActivityForResult(chooserIntent, 201);
        }

        /**
         * 5.0以下 上传图片成功后的回调
         */
        public void mUploadMessage(Intent intent, int resultCode) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }

        /**
         * 5.0以上 上传图片成功后的回调
         */
        void mUploadMessageForAndroid5(Intent intent, int resultCode) {
            if (null == mUploadMessageForAndroid5)
                return;
            Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
            if (result != null) {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
            } else {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
            }
            mUploadMessageForAndroid5 = null;
        }

        /**
         * 判断是否是全屏
         */
        boolean inCustomView() {
            return (customView != null);
        }
    }


    /**
     * 例如，该案例中链接来于喜马拉雅，支付宝，购物网站等等，就需要注意程序漏洞
     * 如果启用了JavaScript，务必做好安全措施，防止远程执行漏洞
     * @param webView               webView控件
     */
    @TargetApi(11)      //支持api11以上
    private void removeJavascriptInterfaces(WebView webView) {
        try {
            if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 17) {
                webView.removeJavascriptInterface("searchBoxJavaBridge_");
                webView.removeJavascriptInterface("accessibility");
                webView.removeJavascriptInterface("accessibilityTraversal");
            }
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
    }



    public FrameLayout getVideoFullView() {
        return videoFullView;
    }


    private void fullViewAddView(View view) {
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        videoFullView = new FullscreenHolder(this);
        videoFullView.addView(view);
        decor.addView(videoFullView);
    }


    private class FullscreenHolder extends FrameLayout {
        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return true;
        }
    }


    private void addImageClickListener() {
        // 这段js函数的功能就是，遍历所有的img节点，并添加onclick函数，函数的功能是在图片点击的时候调用本地java接口并传递url过去
        // 如要点击一张图片在弹出的页面查看所有的图片集合,则获取的值应该是个图片数组
        mWebView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\");" +
                "for(var i=0;i<objs.length;i++)" +
                "{" +
                //  "objs[i].onclick=function(){alert(this.getAttribute(\"has_link\"));}" +
                "objs[i].onclick=function(){window.injectedObject.imageClick(this.getAttribute(\"src\"),this.getAttribute(\"has_link\"));}" +
                "}" +
                "})()");

        // 遍历所有的a节点,将节点里的属性传递过去(属性自定义,用于页面跳转)
        mWebView.loadUrl("javascript:(function(){" +
                "var objs =document.getElementsByTagName(\"a\");" +
                "for(var i=0;i<objs.length;i++)" +
                "{" +
                "objs[i].onclick=function(){" +
                "window.injectedObject.textClick(this.getAttribute(\"type\"),this.getAttribute(\"item_pk\"));}" +
                "}" +
                "})()");
    }


    /***
     * 打开图片js通信接口
     */
    public class JavascriptInterface {
        private Context context;

        JavascriptInterface(Context context) {
            this.context = context;
        }

        //打开图片
        @android.webkit.JavascriptInterface
        public void imageClick(String img) {
            Intent intent = new Intent();
            intent.putExtra("url", img);
            //intent.setClass(context, ImgUrlActivity.class);
            context.startActivity(intent);
            LogUtils.e("WebViewActivity-----js接口返回数据-------图片---"+img);
        }
    }


}
