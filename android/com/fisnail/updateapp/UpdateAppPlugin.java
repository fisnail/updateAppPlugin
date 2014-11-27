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
	 * 服务器端version.json文件名(js传入)
	 */
	private String versionJsonName ="version.json";
	/**
	 * 服务器端IP地址(js传入)
	 */
	private String server = "192.168.1.65";
	/**
	 * 服务器端服务端口号(js传入)
	 */
	private String port = "8080";
	/**
	 * 服务器端项目名称(js传入)
	 */
	private String projectName = "exam";
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
     * 下载进度对话框
     */
	private Handler handler = new Handler();
	private Context context;
	 private ProgressDialog pBar;
	 /**
	     * 取消更新
	     */
	    private boolean cancelUpdate = false;
	
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		if("update".equals(action)){
			Log.v(TAG, "wait for update...");
			update();
			return true;
		}
		return super.execute(action, args, callbackContext);
	}
	
	private boolean update(){
		new AsyncTask<Void, Integer, String>() {

			@Override
			protected String doInBackground(Void... params) {
				Log.v(TAG, "wait for get remote version.json");
				return getRemoteServerVersion();
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if(result!=""){
					AutoUpdate autoUpdate = gson.fromJson(result, AutoUpdate.class);
					newAPKName = autoUpdate.getApkName();
					Log.v(TAG, "server newAPKName:"+newAPKName);
					newVersionName = autoUpdate.getVerName();
					Log.v(TAG, "server newVersionName:"+newVersionName);
					newVersionCode = autoUpdate.getVerCode();
					Log.v(TAG, "server newVersionCode:"+newVersionCode);
					newAPKDownLoadPath = autoUpdate.getDownLoadPath();
					Log.v(TAG, "server newAPKDownLoadPath:"+newAPKDownLoadPath);
					newAPKUpdateLogInfo = autoUpdate.getLog();
					Log.v(TAG, "server newAPKUpdateLogInfo:"+newAPKUpdateLogInfo);
				}
				Log.v(TAG, "wait for get native version");
				getNativeVersion();
				Log.v(TAG, "begin checkUpdate...");
				checkUpdate();
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
	public void checkUpdate(){
		if((newVersionCode>nativeVersionCode)){
			
			showUpdateAPKDialog();
		}
	}
	
	/**
	 * 获取本地APK版本信息
	 */
	public void getNativeVersion(){
		context = cordova.getActivity();
        PackageInfo packageInfo;    
        PackageManager pm = this.context.getPackageManager() ;    
        try {    
        	packageInfo = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0);    
        	nativeVersionName = packageInfo.versionName; 
            nativeVersionCode = packageInfo.versionCode;  
            nativePackageName = packageInfo.packageName;    
            ApplicationInfo applicationInfo = pm.getApplicationInfo(nativePackageName, 0);     
            nativeAppName =  applicationInfo.loadLabel(pm).toString();    
        } catch (NameNotFoundException e) {    
            e.printStackTrace();    
        }    
	}
	
	/**
	 * 获取远程APK版本信息
	 */
	public String getRemoteServerVersion(){
		try {
			/**
			 * version.json文件路径
			 */
			String versionJsonPath = "http://"+server+":"+port+"/"+projectName+"/"+versionJsonName;
			/**
			 * version.json文件内容
			 */
			StringBuilder versionJsonStr = new StringBuilder();
			URL url = new URL(versionJsonPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"),8192);
			String line = null;
			while((line = reader.readLine()) != null){
				versionJsonStr.append(line);
			}
			reader.close();
			if(versionJsonStr.length()>0){
				return versionJsonStr.toString();
//				
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 下载NEW_APK
	 */
	public void downLoadNewAPK(){
		Log.v(TAG, "show download progressDialog...");
		pBar.setCanceledOnTouchOutside(false);
		pBar.show();
		
		new Thread() {
			public void run() {
				// 判断SD卡是否存在，并且是否具有读写权限
				try {
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						// 获得存储卡的路径
						newAPKSavePath = Environment.getExternalStorageDirectory()+ "/";
						
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
							mHandler.sendEmptyMessage(DOWNLOAD);
							if (numread <= 0) {
								// 下载完成
								mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
								break;
							}
							// 写入文件
							fos.write(buf, 0, numread);
						} while (!cancelUpdate);// 点击取消就停止下载.
						fos.close();
						is.close();
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
		
	}



	protected void haveDownLoad() {
		Log.v(TAG, "show download end dialog...");
		context = cordova.getActivity();
		handler.post(new Runnable() {
			public void run() {
				pBar.cancel();
				// 弹出警告框 提示是否安装新的版本
				Dialog installDialog = new AlertDialog.Builder(
						context)
						.setTitle("下载完成")
						.setMessage("是否安装新的应用")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										installNewAPK();
										//finish();
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										//finish();
									}
								}).create();
				installDialog.setCanceledOnTouchOutside(false);
				installDialog.show();
			}
		});
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
	             installNewAPK();
	                break;
	            default:
	                break;
	            }
	        };
	    };

	/**
	 * 显示是否下载APK对话框
	 */
	public void showUpdateAPKDialog(){
		Log.v(TAG, "show update apk dialog...");
		context = cordova.getActivity();
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本："+nativeVersionName+"\n");
		sb.append("发现新版本："+newVersionName+"\n");
		sb.append(newAPKUpdateLogInfo+"\n");// 更新日志
		sb.append("是否更新？");
		dialog = new AlertDialog.Builder(context)
				.setTitle("温馨提示")
				.setMessage(sb.toString())
				.setPositiveButton("立即更新",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								showUpdatingAPKProcessBar();// 更新当前版本
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
	public void showUpdatingAPKProcessBar(){
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
	public void installNewAPK(){
		Log.v(TAG, "install apk");
		context = cordova.getActivity();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), newAPKName)),
				"application/vnd.android.package-archive");
		this.context.startActivity(intent);
	}
	
}
