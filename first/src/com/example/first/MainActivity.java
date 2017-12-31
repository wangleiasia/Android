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
	
	//�����ļ��ϴ�
	private ValueCallback<Uri[]> mUploadCallbackAboveL;
	private ValueCallback<Uri> mUploadMessage;
	
	private FrameLayout loadingLayout; //��ʾ�û����ڼ�������
    private RelativeLayout webParentView; //����ͼ
    private View mErrorView; //���ش������ͼ

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		webView = (WebView)findViewById(R.id.webView1);
		mWebSettings = webView.getSettings();
		mWebSettings.setAllowFileAccess(true);// �������������ļ�����
        
        //����web��ͼ�ͻ���
		webView.setWebViewClient(new MyWebViewClient());
		webView.setWebChromeClient(new MyWebChromeClient());
		//����WebView���ԣ��ܹ�ִ��JavaScript�ű�
		mWebSettings.setJavaScriptEnabled(true);
		
		webParentView = (RelativeLayout) webView.getParent(); //��ȡ������
		
		//��ʹ�û���: 
		mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		//����λ����Ϣ���·��
		mWebSettings.setGeolocationDatabasePath(this.getApplicationContext().getFilesDir().getPath() );
		
		//����URL����
		//webView.loadUrl("http://www.baidu.com");
		//webView.loadUrl("file:///android_asset/index.html");
//		webView.loadUrl("http://10.0.2.2:7788/");
		webView.loadUrl("http://192.168.137.1:7788/");
	}
	
	//web��ͼ�ͻ���
	public class MyWebViewClient extends WebViewClient {
		public boolean shouldOverviewUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
	
	
	//web��ͼ�ͻ��� ������alert��confirm����¼�
	public class MyWebChromeClient extends WebChromeClient {
		@Override
		public boolean onJsAlert(WebView view, String url, String message,JsResult result) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
			builder.setTitle("�Ի���").setMessage(message).setPositiveButton("ȷ��", null);
			builder.setCancelable(false);  
	        AlertDialog dialog = builder.create();  
	        dialog.show();  
	        result.confirm();// ��Ϊû�а��¼�����Ҫǿ��confirm,����ҳ�������ʾ�������ݡ�  
	        return true;  
			//return super.onJsAlert(view, url, message, result);
		}

		//����confirm������
		@Override  
		public boolean onJsConfirm(WebView view, String url, String message,JsResult result) {  
			result.confirm();  
			return super.onJsConfirm(view, url, message, result);
		}  

		//����prompt������ 
		@Override  
		public boolean onJsPrompt(WebView view, String url, String message,
				String defaultValue, JsPromptResult result) {  
			result.confirm();  
			return super.onJsPrompt(view, url, message, message, result);
		}
		
		/***************�����ļ��ϴ�************************/
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
			Log.e(TAG,"��ʼ����");
		    callback.invoke(origin, true, true);
		    super.onGeolocationPermissionsShowPrompt(origin, callback);
		}
		
	}
	
	
	//onActivityResult�ص� 
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
	
	//���٣���ֹ�ڴ�й©
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