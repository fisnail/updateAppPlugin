package com.fisnail.updateapp;

/**
 * 自动更新
 * @author Fisnail(fisnail@163.com)
 * 2014-11-26
 *
 */
public class AutoUpdate {

	/**
	 * NEW_APK 名称
	 */
	private String apkName;

	/**
	 * NEW_APK 版本名称
	 */
	private String verName;

	/**
	 * NEW_APK 版本号
	 */
	private Integer verCode;

	/**
	 * 本次更新优化内容简介
	 */
	private String logs;

	/**
	 * NEW_APK 下载路径
	 */
	private String downLoadPath;

	public String getApkName() {
		return apkName;
	}

	public void setApkName(String apkName) {
		this.apkName = apkName;
	}

	public String getDownLoadPath() {
		return downLoadPath;
	}

	public void setDownLoadPath(String downLoadPath) {
		this.downLoadPath = downLoadPath;
	}

	public String getLogs() {
		return logs;
	}

	public void setLogs(String logs) {
		this.logs = logs;
	}

	public Integer getVerCode() {
		return verCode;
	}

	public void setVerCode(Integer verCode) {
		this.verCode = verCode;
	}

	public String getVerName() {
		return verName;
	}

	public void setVerName(String verName) {
		this.verName = verName;
	}
}