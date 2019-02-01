package com.dragon.excel.entity;

import com.longlong.excel.field.ExcelField;

@ExcelField(cols = 2)
public class TestParam extends Page<TestEntity> {

	@ExcelField(title = "销售城市：", align = 3)
	private String cityPos;
	@ExcelField(title = "运价文件号：", align = 3)
	private String crrCode;
	@ExcelField(title = "航班号：", align = 3)
	private String flightNum;
	@ExcelField(title = "申请日期范围：")
	private String requestDateStr;
	@ExcelField(title = "航班日期范围：")
	private String flightDateStr;
	@ExcelField(title = "航线：")
	private String segment;

	public String getCityPos() {
		return cityPos;
	}

	public void setCityPos(String cityPos) {
		this.cityPos = cityPos;
	}

	public String getCrrCode() {
		return crrCode;
	}

	public void setCrrCode(String crrCode) {
		this.crrCode = crrCode;
	}

	public String getFlightNum() {
		return flightNum;
	}

	public void setFlightNum(String flightNum) {
		this.flightNum = flightNum;
	}

	public String getRequestDateStr() {
		return requestDateStr;
	}

	public void setRequestDateStr(String requestDateStr) {
		this.requestDateStr = requestDateStr;
	}

	public String getFlightDateStr() {
		return flightDateStr;
	}

	public void setFlightDateStr(String flightDateStr) {
		this.flightDateStr = flightDateStr;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

}
