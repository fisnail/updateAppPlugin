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
    private String APKNAME;
    
    /**
     * NEW_APK 版本名称
     */
    private String VERNAME;
    
    /**
     * NEW_APK 版本号
     */
    private Integer VERCODE;
    
    /**
     * 本次更新优化内容简介
     */
    private String LOGS;
    
    /**
     * NEW_APK 下载路径
     */
    private String DOWNLOADPATH;

	public String getAPKNAME() {
		return APKNAME;
	}

	public void setAPKNAME(String APKNAME) {
		this.APKNAME = APKNAME;
	}

	public String getVERNAME() {
		return VERNAME;
	}

	public void setVERNAME(String VERNAME) {
		this.VERNAME = VERNAME;
	}

	public Integer getVERCODE() {
		return VERCODE;
	}

	public void setVERCODE(Integer VERCODE) {
		this.VERCODE = VERCODE;
	}

	public String getLOGS() {
		return LOGS;
	}

	public void setLOGS(String LOGS) {
		this.LOGS = LOGS;
	}

	public String getDOWNLOADPATH() {
		return DOWNLOADPATH;
	}

	public void setDOWNLOADPATH(String DOWNLOADPATH) {
		this.DOWNLOADPATH = DOWNLOADPATH;
	}
}