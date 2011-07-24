package com.blogspot.yakisobayuki.dtn;

public class Route {
	private String Transportation; // ç§»å‹•æ‰‹æ®µ
	private String DLocate; // å‡ºç™ºå ´æ‰?
	private String DTime; // å‡ºç™ºæ™‚é–“
	private String ALocate; // åˆ°ç??æ‰?
	private String ATime; // åˆ°ç?™‚é–?

	public void setRoute(String trans, String dep_time, String dep_locate,
			String arr_time, String arr_locate) {
		this.Transportation = trans;
		this.DLocate = dep_locate;
		this.DTime = dep_time;
		this.ALocate = arr_locate;
		this.ATime = arr_time;
	}

	/**
	 * ç§»å‹•æ‰‹æ®µã‚’æ?ç´ã™ã‚?
	 * 
	 * @param trans
	 *            ç§»å‹•æ‰‹æ®µ
	 */
	public void setTrans(String trans) {
		this.Transportation = trans;
	}

	/**
	 * å‡ºç™ºæ™‚é–“ã¨å‡ºç™ºå ´æ‰?‚’æ ¼ç´ã™ã‚?
	 * 
	 * @param dep_time
	 *            å‡ºç™ºæ™‚é–“
	 * @param dep_locate
	 *            å‡ºç™ºå ´æ‰?
	 */
	public void setDTime_Locate(String dep_time, String dep_locate) {
		this.DTime = dep_time;
		this.DLocate = dep_locate;
	}

	/**
	 * åˆ°ç?™‚é–“ã¨åˆ°ç??æ‰?‚’æ ¼ç´ã™ã‚?
	 * 
	 * @param arr_time
	 *            åˆ°ç?™‚é–?
	 * @param arr_locate
	 *            åˆ°ç??æ‰?
	 */
	public void setATime_Locate(String arr_time, String arr_locate) {
		this.ATime = arr_time;
		this.ALocate = arr_locate;
	}

	/**
	 * ç§»å‹•æ‰‹æ®µã‚’å–å¾—ã™ã‚?
	 * 
	 * @return ç§»å‹•æ‰‹æ®µ
	 */
	public String getTrans() {
		return Transportation;
	}

	/**
	 * å‡ºç™ºå ´æ‰?‚’å–å¾—ã™ã‚?
	 * 
	 * @return å‡ºç™ºå ´æ‰?
	 */
	public String getDLocate() {
		return DLocate;
	}

	/**
	 * å‡ºç™ºæ™‚é–“ã‚’å–å¾—ã™ã‚?
	 * 
	 * @return å‡ºç™ºæ™‚é–“
	 */
	public String getDTime() {
		return DTime;
	}

	/**
	 * åˆ°ç??æ‰?‚’å–å¾—ã™ã‚?
	 * 
	 * @return åˆ°ç??æ‰?
	 */
	public String getALocate() {
		return ALocate;
	}

	/**
	 * åˆ°ç?™‚é–“ã‚’å–å¾—ã™ã‚?
	 * 
	 * @return åˆ°ç?™‚é–?
	 */
	public String getATime() {
		return ATime;
	}
}
