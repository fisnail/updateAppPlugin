package com.fisnail.updateapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fisnail.updateapp.AutoUpdate;
import com.fisnail.updateapp.UpdateAppPlugin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.json.JSONObject;

/**
 *  自动更新
 * @author Fisnail(fisnail@163.com)
 * 2014-11-26
 *
 */
public class UpdateAppPlugin extends CordovaPlugin {
	private static final String TAG ="UpdateApp";
	private static Gson gson = new GsonBuilder().create();


	/**
	 * NEW_APK名称
	 */
	private String newAPKName;
	/**
	 * NEW_APK 版本名称
	 */
	private String newVersionName;
	/**
	 * NEW_APK 版本号
	 */
	private Integer newVersionCode;
	/**
	 * NEW_APK 下载路径(js)
	 */
	private String newAPKDownLoadPath;
	/**
	 * 本次更新优化内容简介
	 */
	private String newAPKUpdateLogInfo;
	/**
	 * 本地当前APK版本名称
	 */
	private String nativeVersionName;
	/**
	 * 本地当前APK版本号
	 */
	private Integer nativeVersionCode;
	/**
	 * 本地应用包名
	 */
	private String nativePackageName;
	/**
	 * 本地应用名称
	 */
	private String nativeAppName;
	
	/**
	 * 记录进度条数量
	 */
    private int progress;
	/**
	 * 下载中 
	 */
    private static final int DOWNLOAD = 1;
    /**
     * 下载结束
     */
    private static final int DOWNLOAD_FINISH = 2;
    /**
     * 下载NEW_APK保存路径
     */
    private String newAPKSavePath;
    /**
     * 提示下载对话框
     */
    private Dialog dialog;
	/**
	 * 线程停止状态位
	 */
	private boolean stop;
    /**
     * 下载进度对话框
     */
	private Handler handler = new Handler();
	private Context context;
	private ProgressDialog pBar;
	private CallbackContext callbackContextInClass;
	 /**
	     * 取消更新
	     */
	    private boolean cancelUpdate = false;
	
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException{
		callbackContextInClass = callbackContext;
		try {
			if ("update".equals(action)) {
				JSONObject versionJsonPath = args.getJSONObject(0);
				Log.v(TAG, "check js set version.json file path...");
//				String versionJsonFilePath = null;
//				try {
//					versionJsonFilePath = String.valueOf(versionJsonPath.get("versionJsonFilePath"));
//					if (null == versionJsonFilePath || "".equals(versionJsonFilePath.trim())) {
//						throw new JSONException("version file path not set!");
//					}
//				} catch (JSONException e) {
//					Log.e(TAG, e.getMessage());
//					throw new JSONException(e.getMessage());
//				}
				Log.v(TAG,"Ready to check for update...");
				update(versionJsonPath+"");
				return true;
			}
		} catch (JSONException e) {
			stop = true;
			callbackContextInClass.error(e.getMessage());
			callbackContextInClass.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
		}   catch (Exception e){
			stop = true;
			callbackContextInClass.error(e.getMessage());
			callbackContextInClass.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
		}
		return super.execute(action, args, callbackContext);
	}
	
	private boolean update(final String versionJsonFilePath){
		 new AsyncTask<Void, Integer, String>() {
			@Override
			protected String doInBackground(Void... params) {
//				Log.v(TAG, "Ready to read the contents of the remote file(version.json)...");
//				Log.v(TAG, "Remote file address:"+versionJsonFilePath);
//				try {
//					return getRemoteServerVersion(versionJsonFilePath);
//				} catch (IOException e) {
//					Log.e(TAG,e.getMessage());
//					stop  = true;
//					callbackContextInClass.error(e.getMessage());
//					callbackContextInClass.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
//				}
				return "";
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if (stop){
					return;
				}
			try {
//				if ("".equals(result)) {
//					Log.e(TAG, "version.json file is null");
//					throw new Exception("version.json file is null");
//				}
				result = versionJsonFilePath;
				if (result != "") {
					AutoUpdate autoUpdate = gson.fromJson(result, AutoUpdate.class);
					newAPKName = autoUpdate.getApkName();
					Log.v(TAG, "server newAPKName:" + newAPKName);
					newVersionName = autoUpdate.getVerName();
					Log.v(TAG, "server newVersionName:" + newVersionName);
					newVersionCode = autoUpdate.getVerCode();
					Log.v(TAG, "server newVersionCode:" + newVersionCode);
					newAPKDownLoadPath = autoUpdate.setDownLoadPath();
					Log.v(TAG, "server newAPKDownLoadPath:" + newAPKDownLoadPath);
					newAPKUpdateLogInfo = autoUpdate.getLogs();
					Log.v(TAG, "server newAPKUpdateLogInfo:" + newAPKUpdateLogInfo);
				}
				Log.v(TAG, "wait for get native version");
				getNativeVersion();
				Log.v(TAG, "begin checkUpdate...");
				checkUpdate();
			}catch (Exception e){
				Log.e(TAG,e.getMessage());
				stop = true;
				callbackContextInClass.error(e.getMessage());
				callbackContextInClass.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
			}
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
			}
		}.execute();
		return true;
	}
	/**
	 * 检测自动更新
	 */
	public void checkUpdate() throws Exception{

		Log.v(TAG, "newVersionCode:"+newVersionCode);
		Log.v(TAG, "nativeVersionCode" + nativeVersionCode);

		if((newVersionCode>nativeVersionCode)){
			showUpdateAPKDialog();
		}
	}
	
	/**
	 * 获取本地APK版本信息
	 */
	public void getNativeVersion() throws NameNotFoundException {
		context = cordova.getActivity();
        PackageInfo packageInfo;    
        PackageManager pm = this.context.getPackageManager() ;    
		packageInfo = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0);
		nativeVersionName = packageInfo.versionName;
		Log.v(TAG,"nativeVersionName:"+nativeVersionName);
		nativeVersionCode = packageInfo.versionCode;
		Log.v(TAG,"nativeVersionCode:"+nativeVersionCode);
		nativePackageName = packageInfo.packageName;
		Log.v(TAG,"nativePackageName:"+nativePackageName);
		ApplicationInfo applicationInfo = pm.getApplicationInfo(nativePackageName, 0);
		nativeAppName =  applicationInfo.loadLabel(pm).toString();
		Log.v(TAG,"nativeAppName:"+nativeAppName);
	}
	
	/**
	 * 获取远程APK版本信息
	 */
	public String getRemoteServerVersion(String versionJsonPath) throws IOException {
		/**
		 * version.json文件内容
		 */
		StringBuilder versionJsonStr = new StringBuilder();
		URL url = null;
		Log.v(TAG,"Ready for request version.json...");
		url = new URL(versionJsonPath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);
		conn.connect();
		Log.v(TAG,"Ready for read file version.json...");
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"),8192);
		String line = null;
		while((line = reader.readLine()) != null){
			versionJsonStr.append(line);
		}
		reader.close();
		Log.v(TAG,"read file end,version.json info:"+versionJsonStr.toString());
		if (versionJsonStr.length()==0){

			Log.e(TAG,"version.json file length is zero");
			throw new IOException("version.json file length is zero");
		}
		if(versionJsonStr.length()>0){
			return versionJsonStr.toString();
		}
		return "";
	}
	
	/**
	 * 下载NEW_APK
	 */
	public void downLoadNewAPK() throws Exception{

		Log.v(TAG, "show download progressDialog...");
		pBar.setCanceledOnTouchOutside(false);
		pBar.show();
		if (stop){
			return;
		}

		Thread thread =	new Thread() {
				public void run() {

					// 判断SD卡是否存在，并且是否具有读写权限
					try {
						if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
							// 获得存储卡的路径
							newAPKSavePath = Environment.getExternalStorageDirectory() + "/";

							URL url = new URL(newAPKDownLoadPath);
							// 创建连接
							HttpURLConnection conn = (HttpURLConnection) url.openConnection();
							conn.connect();
							// 获取文件大小
							int length = conn.getContentLength();
							// 创建输入流
							InputStream is = conn.getInputStream();

							File file = new File(newAPKSavePath);
							// 判断文件目录是否存在
							if (!file.exists()) {
								Log.v(TAG,"create file dir");
								file.mkdir();
							}
							File apkFile = new File(newAPKSavePath, newAPKName);
							FileOutputStream fos = new FileOutputStream(apkFile);
							int count = 0;
							// 缓存
							byte buf[] = new byte[1024];
							// 写入到文件中
							do {
								int numread = is.read(buf);
								count += numread;
								// 计算进度条位置
								progress = (int) (((float) count / length) * 100);
								// 更新进度
								Log.v(TAG,"downLoading...");
								mHandler.sendEmptyMessage(DOWNLOAD);
								if (numread <= 0) {
									// 下载完成
									Log.v(TAG,"down load finish");
									mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
									break;
								}
								// 写入文件
								fos.write(buf, 0, numread);
							} while (!cancelUpdate);// 点击取消就停止下载.
							fos.close();
							is.close();
						}
					}  catch (Exception e) {
						Log.e(TAG,e.getMessage());
						stop =true;
						pBar.dismiss();
						callbackContextInClass.error(e.getMessage());
						callbackContextInClass.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
					}
				}
			};
		if (!stop){
			thread.start();
		}

	}

	 private Handler mHandler = new Handler()
	    {
	        public void handleMessage(Message msg)
	        {
	            switch (msg.what)
	            {
	            // 正在下载
	            case DOWNLOAD:
	                // 设置进度条位置
	                pBar.setProgress(progress);
	                break;
	            case DOWNLOAD_FINISH:
	            	pBar.dismiss();
	                // 安装文件
					try {
						installNewAPK();
					}catch (Exception e){
						Log.e(TAG,e.getMessage());
						stop = true;
						callbackContextInClass.error(e.getMessage());
						callbackContextInClass.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
					}
	                break;
	            default:
	                break;
	            }
	        };
	    };

	/**
	 * 显示是否下载APK对话框
	 */
	public void showUpdateAPKDialog() throws Exception{

			Log.v(TAG, "show update apk dialog...");
			context = cordova.getActivity();
			StringBuffer sb = new StringBuffer();
			sb.append("当前版本：" + nativeVersionName + "\n");
			sb.append("发现新版本：" + newVersionName + "\n");
			sb.append(newAPKUpdateLogInfo + "\n");// 更新日志
			sb.append("是否更新？");
			dialog = new AlertDialog.Builder(context)
					.setTitle("温馨提示")
					.setMessage(sb.toString())
					.setPositiveButton("立即更新",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									try {
										showUpdatingAPKProcessBar();// 更新当前版本
									} catch (Exception e) {
										Log.e(TAG,e.getMessage());
										stop = true;
										callbackContextInClass.error(e.getMessage());
										callbackContextInClass.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
									}
								}
							})
					.setNegativeButton("暂不更新",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int which) {
									cancelUpdate = true;
									//finish();
								}
							}).create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();

	}
	
	/**
	 * 显示正在下载APK进度条
	 */
	public void showUpdatingAPKProcessBar() throws Exception{
Log.v(TAG,"show update APK processBar...");
		context = cordova.getActivity();
		pBar = new ProgressDialog(context);
		pBar.setTitle("");
		pBar.setMessage("正在下载,请稍后...");
		pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		downLoadNewAPK();
		
	}
	
	/**
	 * NEW_APK安装
	 */
	public void installNewAPK() throws  Exception{
		Log.v(TAG, "install apk...");
		context = cordova.getActivity();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), newAPKName)),
				"application/vnd.android.package-archive");
		this.context.startActivity(intent);
	}
	
}
