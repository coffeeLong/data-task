package com.dragon.excel.entity;

import java.io.Serializable;

import com.longlong.excel.field.ExcelField;

@ExcelField(align = 2)
public class TestEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@ExcelField(title = "ID")
	private int id;
	@ExcelField(title = "销售城市")
	private String cityPos;
	@ExcelField(title = "申请团队")
	private String countRequests;
	@ExcelField(title = "证实团队")
	private String countPassedRequests;
	@ExcelField(title = "撤回团队")
	private String countCanceledRequests;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCountPassedRequests() {
		return countPassedRequests;
	}

	public void setCountPassedRequests(String countPassedRequests) {
		this.countPassedRequests = countPassedRequests;
	}

	public String getCityPos() {
		return cityPos;
	}

	public void setCityPos(String cityPos) {
		this.cityPos = cityPos;
	}

	public String getCountRequests() {
		return countRequests;
	}

	public void setCountRequests(String countRequests) {
		this.countRequests = countRequests;
	}

	public String getCountCanceledRequests() {
		return countCanceledRequests;
	}

	public void setCountCanceledRequests(String countCanceledRequests) {
		this.countCanceledRequests = countCanceledRequests;
	}

	@Override
	public String toString() {
		return "TestEntity [id=" + id + ", cityPos=" + cityPos + ", countRequests=" + countRequests + ", countPassedRequests=" + countPassedRequests + ", countCanceledRequests="
				+ countCanceledRequests + "]";
	}

}
