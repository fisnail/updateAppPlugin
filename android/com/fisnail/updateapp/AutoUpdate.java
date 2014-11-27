package com.fisnail.updateapp;

/**
 * 自动更新
 * @author daichen
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
    private String log;
    
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

	public String getVerName() {
		return verName;
	}

	public void setVerName(String verName) {
		this.verName = verName;
	}

	public Integer getVerCode() {
		return verCode;
	}

	public void setVerCode(Integer verCode) {
		this.verCode = verCode;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public String getDownLoadPath() {
		return downLoadPath;
	}

	public void setDownLoadPath(String downLoadPath) {
		this.downLoadPath = downLoadPath;
	}
    
    
  
    
}