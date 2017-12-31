package com.example.first;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	
	private static final int FILECHOOSER_RESULTCODE = 1;
	
	private WebView webView;
	WebSettings mWebSettings;
	
	//处理文件上传
	private ValueCallback<Uri[]> mUploadCallbackAboveL;
	private ValueCallback<Uri> mUploadMessage;
	
	private FrameLayout loadingLayout; //提示用户正在加载数据
    private RelativeLayout webParentView; //父视图
    private View mErrorView; //加载错误的视图

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		webView = (WebView)findViewById(R.id.webView1);
		mWebSettings = webView.getSettings();
		mWebSettings.setAllowFileAccess(true);// 设置允许访问文件数据
        
        //设置web视图客户端
		webView.setWebViewClient(new MyWebViewClient());
		webView.setWebChromeClient(new MyWebChromeClient());
		//设置WebView属性，能够执行JavaScript脚本
		mWebSettings.setJavaScriptEnabled(true);
		
		webParentView = (RelativeLayout) webView.getParent(); //获取父容器
		
		//不使用缓存: 
		mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		//设置位置信息存放路径
		mWebSettings.setGeolocationDatabasePath(this.getApplicationContext().getFilesDir().getPath() );
		
		//加载URL内容
		//webView.loadUrl("http://www.baidu.com");
		//webView.loadUrl("file:///android_asset/index.html");
//		webView.loadUrl("http://10.0.2.2:7788/");
		webView.loadUrl("http://192.168.137.1:7788/");
	}
	
	//web视图客户端
	public class MyWebViewClient extends WebViewClient {
		public boolean shouldOverviewUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
	
	
	//web视图客户端 ，处理alert，confirm相关事件
	public class MyWebChromeClient extends WebChromeClient {
		@Override
		public boolean onJsAlert(WebView view, String url, String message,JsResult result) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
			builder.setTitle("对话框").setMessage(message).setPositiveButton("确定", null);
			builder.setCancelable(false);  
	        AlertDialog dialog = builder.create();  
	        dialog.show();  
	        result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内容。  
	        return true;  
			//return super.onJsAlert(view, url, message, result);
		}

		//处理confirm弹出框
		@Override  
		public boolean onJsConfirm(WebView view, String url, String message,JsResult result) {  
			result.confirm();  
			return super.onJsConfirm(view, url, message, result);
		}  

		//处理prompt弹出框 
		@Override  
		public boolean onJsPrompt(WebView view, String url, String message,
				String defaultValue, JsPromptResult result) {  
			result.confirm();  
			return super.onJsPrompt(view, url, message, message, result);
		}
		
		/***************处理文件上传************************/
		//For Android API < 11 (3.0 OS)
		public void openFileChooser(ValueCallback<Uri> uploadMsg) {
			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");
			MainActivity.this.startActivityForResult(
					Intent.createChooser(i, "File Chooser"),
					FILECHOOSER_RESULTCODE);
		}
		//For Android API >= 11 (3.0 OS)
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");
			MainActivity.this.startActivityForResult(
					Intent.createChooser(i, "File Browser"),
					FILECHOOSER_RESULTCODE);
		}
		
		public void openFileChooser(ValueCallback<Uri> uploadMsg,String acceptType, String capture) {
			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");
			MainActivity.this.startActivityForResult(
					Intent.createChooser(i, "File Browser"),
					MainActivity.FILECHOOSER_RESULTCODE);
		}
		
		//For Android API >= 21 (5.0 OS)
		public boolean onShowFileChooser(WebView webView,ValueCallback<Uri[]> filePathCallback,WebChromeClient.FileChooserParams fileChooserParams) {
			mUploadCallbackAboveL = filePathCallback;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");
			MainActivity.this.startActivityForResult(
					Intent.createChooser(i, "File Browser"),
					FILECHOOSER_RESULTCODE);
			return true;
		}
		
		@Override
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			Log.e(TAG,"开始调用");
		    callback.invoke(origin, true, true);
		    super.onGeolocationPermissionsShowPrompt(origin, callback);
		}
		
	}
	
	
	//onActivityResult回调 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage && null == mUploadCallbackAboveL)
				return;
			Uri result = data == null || resultCode != RESULT_OK ? null : data
					.getData();
			if (mUploadCallbackAboveL != null) {
				onActivityResultAboveL(requestCode, resultCode, data);
			} else if (mUploadMessage != null) {
				mUploadMessage.onReceiveValue(result);
				mUploadMessage = null;
			}
		}
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void onActivityResultAboveL(int requestCode, int resultCode,
			Intent data) {
		if (requestCode != FILECHOOSER_RESULTCODE
				|| mUploadCallbackAboveL == null) {
			return;
		}
		Uri[] results = null;
		if (resultCode == Activity.RESULT_OK) {
			if (data == null) {
			} else {
				String dataString = data.getDataString();
				ClipData clipData = data.getClipData();
				if (clipData != null) {
					results = new Uri[clipData.getItemCount()];
					for (int i = 0; i < clipData.getItemCount(); i++) {
						ClipData.Item item = clipData.getItemAt(i);
						results[i] = item.getUri();
					}
				}
				if (dataString != null)
					results = new Uri[] { Uri.parse(dataString) };
			}
		}
		mUploadCallbackAboveL.onReceiveValue(results);
		mUploadCallbackAboveL = null;
		return;
	}
	
	//销毁，防止内存泄漏
	@Override
	protected void onDestroy() {
		if (webView != null) {
			webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
			webView.clearHistory();

			webParentView.removeView(webView);
			webView.destroy();
			webView = null;
		}
		super.onDestroy();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}
	
}
