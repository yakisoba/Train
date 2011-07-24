package com.blogspot.yakisobayuki.dtn;

public class Route {
	private String Transportation; // 移動手段
	private String DLocate; // 出発場�?
	private String DTime; // 出発時間
	private String ALocate; // 到�??�?
	private String ATime; // 到�?���?

	public void setRoute(String trans, String dep_time, String dep_locate,
			String arr_time, String arr_locate) {
		this.Transportation = trans;
		this.DLocate = dep_locate;
		this.DTime = dep_time;
		this.ALocate = arr_locate;
		this.ATime = arr_time;
	}

	/**
	 * 移動手段を�?納す�?
	 * 
	 * @param trans
	 *            移動手段
	 */
	public void setTrans(String trans) {
		this.Transportation = trans;
	}

	/**
	 * 出発時間と出発場�?��格納す�?
	 * 
	 * @param dep_time
	 *            出発時間
	 * @param dep_locate
	 *            出発場�?
	 */
	public void setDTime_Locate(String dep_time, String dep_locate) {
		this.DTime = dep_time;
		this.DLocate = dep_locate;
	}

	/**
	 * 到�?��間と到�??�?��格納す�?
	 * 
	 * @param arr_time
	 *            到�?���?
	 * @param arr_locate
	 *            到�??�?
	 */
	public void setATime_Locate(String arr_time, String arr_locate) {
		this.ATime = arr_time;
		this.ALocate = arr_locate;
	}

	/**
	 * 移動手段を取得す�?
	 * 
	 * @return 移動手段
	 */
	public String getTrans() {
		return Transportation;
	}

	/**
	 * 出発場�?��取得す�?
	 * 
	 * @return 出発場�?
	 */
	public String getDLocate() {
		return DLocate;
	}

	/**
	 * 出発時間を取得す�?
	 * 
	 * @return 出発時間
	 */
	public String getDTime() {
		return DTime;
	}

	/**
	 * 到�??�?��取得す�?
	 * 
	 * @return 到�??�?
	 */
	public String getALocate() {
		return ALocate;
	}

	/**
	 * 到�?��間を取得す�?
	 * 
	 * @return 到�?���?
	 */
	public String getATime() {
		return ATime;
	}
}
